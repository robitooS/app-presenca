package com.example.presenteapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.presenteapp.databinding.ActivityTeacherDashboardBinding
import com.google.firebase.auth.FirebaseAuth

class activity_teacher_dashboard : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherDashboardBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupButtons()
    }

    private fun setupButtons() {
        binding.createQrButton.setOnClickListener {
            Toast.makeText(this, "Funcionalidade de criar QR Code em breve!", Toast.LENGTH_SHORT).show()
        }

        binding.attendanceReportButton.setOnClickListener {
            Toast.makeText(this, "Relatórios de falta em breve!", Toast.LENGTH_SHORT).show()
        }

        // MODIFICAÇÃO AQUI: Usando o nome de classe correto 'activity_pending_students'
        binding.approveStudentsButton.setOnClickListener {
            val intent = Intent(this, activity_pending_students::class.java)
            startActivity(intent)
        }
    }
}