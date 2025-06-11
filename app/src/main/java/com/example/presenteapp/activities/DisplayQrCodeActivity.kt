package com.example.presenteapp.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.presenteapp.R
import com.example.presenteapp.RetrofitInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DisplayQrCodeActivity : AppCompatActivity() {

    private lateinit var qrCodeImageView: ImageView
    private lateinit var timerTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var auth: FirebaseAuth

    private var countDownTimer: CountDownTimer? = null
    private val TAG = "DisplayQrCodeActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_qrcode)

        qrCodeImageView = findViewById(R.id.qrCodeImageView)
        timerTextView = findViewById(R.id.timerTextView)
        progressBar = findViewById(R.id.progressBar)
        auth = FirebaseAuth.getInstance()

        createSessionAndGenerateQrCode()
    }

    private fun createSessionAndGenerateQrCode() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Erro: Professor não está logado.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE

        currentUser.getIdToken(true).addOnSuccessListener { result ->
            val token = "Bearer ${result.token}"

            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.api.createClassSession(token)
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful && response.body() != null) {
                        // Com o import adicionado, esta linha agora funcionará
                        val classSessionId = response.body()?.classSessionId
                        generateQrCode(classSessionId.toString())
                        startTimer()
                    } else {
                        Log.e(TAG, "Falha ao criar sessão: ${response.code()} - ${response.errorBody()?.string()}")
                        Toast.makeText(this@DisplayQrCodeActivity, "Erro ao criar a sessão no servidor.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                } catch (e: Exception) {
                    progressBar.visibility = View.GONE
                    Log.e(TAG, "Exceção ao criar sessão", e)
                    Toast.makeText(this@DisplayQrCodeActivity, "Erro de conexão.", Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            Log.e(TAG, "Falha ao obter token do Firebase", it)
            Toast.makeText(this, "Não foi possível autenticar. Tente novamente.", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun generateQrCode(text: String) {
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE, 600, 600)
            val barcodeEncoder = BarcodeEncoder()
            val bitmap: Bitmap = barcodeEncoder.createBitmap(bitMatrix)
            qrCodeImageView.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    private fun startTimer() {
        val totalTimeInMillis = TimeUnit.MINUTES.toMillis(5)
        countDownTimer = object : CountDownTimer(totalTimeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(minutes)
                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                timerTextView.text = "00:00"
                Toast.makeText(this@DisplayQrCodeActivity, "O tempo esgotou!", Toast.LENGTH_LONG).show()
                qrCodeImageView.setImageBitmap(null)
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}