package com.example.presenteapp.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.presenteapp.R
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class activity_qr_scanner : AppCompatActivity() {

    // Executor para correr as tarefas da câmera em segundo plano
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView

    private val CAMERA_REQUEST_CODE = 101
    private val TAG = "QR_SCANNER_APP" // Tag para logs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        // Inicializa o PreviewView e o executor da câmera
        previewView = findViewById(R.id.cameraPreviewView)
        cameraExecutor = Executors.newSingleThreadExecutor()

        // Verifica e solicita a permissão da câmera
        if (hasCameraPermission()) {
            startCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida, inicia a câmera
                startCamera()
            } else {
                Toast.makeText(this, "Permissão da câmera é necessária para usar esta funcionalidade", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    /**
     * Inicia e configura a câmera usando o CameraX.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Obtém o provedor da câmera. Usado para vincular o ciclo de vida da câmera ao ciclo de vida da Activity.
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Configura o caso de uso de Preview (pré-visualização)
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // Seleciona a câmera traseira como padrão
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Desvincula todos os casos de uso antes de vincular novamente
                cameraProvider.unbindAll()

                // Vincula os casos de uso (neste caso, apenas o preview) à câmera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview)

            } catch(exc: Exception) {
                Log.e(TAG, "Falha ao vincular os casos de uso da câmera", exc)
                Toast.makeText(this, "Não foi possível iniciar a câmera.", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Liberta os recursos da câmera quando a activity for destruída.
     */
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}