package com.example.presenteapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.presenteapp.databinding.ActivityTeacherDashboardBinding // Importar o binding
import com.google.firebase.auth.FirebaseAuth

class activity_teacher_dashboard : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherDashboardBinding // Declarar o binding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupButtons()
        setupLogoutButton() // Vamos adicionar um botão de logout aqui também
    }

    private fun setupButtons() {
        binding.createQrButton.setOnClickListener {
            // No futuro, isso abrirá uma tela com o QR Code gerado.
            // Por agora, apenas um Toast.
            Toast.makeText(this, "Funcionalidade de criar QR Code em breve!", Toast.LENGTH_SHORT).show()
        }

        binding.attendanceReportButton.setOnClickListener {
            Toast.makeText(this, "Relatórios de falta em breve!", Toast.LENGTH_SHORT).show()
        }

        binding.approveStudentsButton.setOnClickListener {
            Toast.makeText(this, "Aprovação de alunos em breve!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLogoutButton() {
        // Supondo que você adicione um botão com id `logoutButton` no seu XML.
        // binding.logoutButton.setOnClickListener {
        //     auth.signOut()
        //     val intent = Intent(this, MainActivity::class.java).apply {
        //         flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        //     }
        //     startActivity(intent)
        //     finish()
        // }
    }
}