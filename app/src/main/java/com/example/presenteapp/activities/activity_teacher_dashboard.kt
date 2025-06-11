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
        setupLogoutButton()
    }

    private fun setupButtons() {
        binding.createQrButton.setOnClickListener {
            val intent = Intent(this, DisplayQrCodeActivity::class.java)
            startActivity(intent)
        }

        binding.attendanceReportButton.setOnClickListener {
            Toast.makeText(this, "Relatórios de falta em breve!", Toast.LENGTH_SHORT).show()
        }

        binding.approveStudentsButton.setOnClickListener {
            val intent = Intent(this, activity_pending_students::class.java)
            startActivity(intent)
        }
    }

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}