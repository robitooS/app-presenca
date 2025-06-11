package com.example.presenteapp.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.presenteapp.R
import com.example.presenteapp.RetrofitInstance
import com.example.presenteapp.network.model.AttendanceRequest
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class activity_qr_scanner : AppCompatActivity() {

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private var barcodeScanner: BarcodeScanner? = null
    private lateinit var auth: FirebaseAuth

    @Volatile
    private var isProcessing = false

    private val CAMERA_REQUEST_CODE = 101
    private val TAG = "QR_SCANNER_APP"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        previewView = findViewById(R.id.cameraPreviewView)
        cameraExecutor = Executors.newSingleThreadExecutor()
        auth = FirebaseAuth.getInstance()

        if (hasCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    // ... (as funções hasCameraPermission, requestCameraPermission e onRequestPermissionsResult permanecem as mesmas) ...
    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissão da câmera é necessária.", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }


    private fun startCamera() {
        isProcessing = false
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        barcodeScanner = BarcodeScanning.getClient(options)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            processImageProxy(barcodeScanner!!, imageProxy)
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageAnalysis
            )
        } catch (exc: Exception) {
            Log.e(TAG, "Falha ao vincular os casos de uso da câmera", exc)
        }
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processImageProxy(barcodeScanner: BarcodeScanner, imageProxy: ImageProxy) {
        if (isProcessing) { // Se já estiver a processar um código, ignora os outros
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        isProcessing = true // Bloqueia novas leituras
                        cameraProviderFuture.get().unbindAll() // Pausa a câmera

                        val qrCodeValue = barcodes.first()?.rawValue
                        if (qrCodeValue != null) {
                            Log.d(TAG, "QR Code detectado: $qrCodeValue")
                            sendAttendanceToServer(qrCodeValue)
                        } else {
                            restartScanner("Código QR inválido.")
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG, "Falha na leitura do barcode", it)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }

    private fun sendAttendanceToServer(classSessionId: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Erro: Nenhum aluno logado.", Toast.LENGTH_LONG).show()
            return
        }
        val studentUid = currentUser.uid

        currentUser.getIdToken(true).addOnSuccessListener { result ->
            val token = "Bearer ${result.token}"
            val request = AttendanceRequest(studentUid = studentUid, classSessionId = classSessionId)

            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.api.registerAttendance(token, request)
                    if (response.isSuccessful) {
                        Toast.makeText(this@activity_qr_scanner, "Presença registrada com sucesso!", Toast.LENGTH_LONG).show()
                        finish() // Fecha a tela de scanner após o sucesso
                    } else {
                        // 2. Tratamento de erros mais detalhado
                        val errorMsg = when(response.code()) {
                            409 -> "Presença já foi registrada para esta sessão." // 409 Conflict
                            404 -> "Sessão de aula não encontrada ou expirada." // 404 Not Found
                            else -> "Falha ao registar presença. Erro: ${response.code()}"
                        }
                        restartScanner(errorMsg)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Erro de conexão ao registar presença", e)
                    restartScanner("Erro de conexão com o servidor.")
                }
            }
        }.addOnFailureListener {
            Log.e(TAG, "Falha ao obter token de autenticação", it)
            restartScanner("Sessão expirada. Tente novamente.")
        }
    }

    // 3. Função para reiniciar o scanner em caso de erro
    private fun restartScanner(errorMessage: String) {
        runOnUiThread {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
            // Reinicia a câmara para permitir uma nova leitura
            startCamera()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
