package com.example.presenteapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.presenteapp.databinding.ActivityAdminDashboardBinding // IMPORTANTE: Importar a classe de Binding
import com.google.firebase.auth.FirebaseAuth

class activity_admin_dashboard : AppCompatActivity() {

    // 1. Declarar a variável para o ViewBinding
    private lateinit var binding: ActivityAdminDashboardBinding

    // 2. Declarar a variável para o Firebase Auth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupLogoutButton()
        setupPendingTeachersButton()
    }

    private fun setupPendingTeachersButton() {
        binding.pendingTeachersButton.setOnClickListener {
            val intent = Intent(this, activity_pending_teachers::class.java)
            startActivity(intent)
        }
    }

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            // Executa a função de logout
            signOut()
        }
    }

    private fun signOut() {
        // Desloga o usuário do Firebase
        auth.signOut()

        // Cria a intenção para voltar para a tela de login (MainActivity)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)

        // Finaliza a activity atual (Admin Dashboard) para removê-la da memória
        finish()
    }
}
