
package com.example.presenteapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.presenteapp.RetrofitInstance

import com.example.presenteapp.databinding.ActivityMainBinding
import com.example.presenteapp.network.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            signInUser(email, password)
        }

        binding.registerButton.setOnClickListener {
            val intent = Intent(this, activity_role_selection::class.java)
            startActivity(intent)
        }
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        currentUser?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
            if (tokenTask.isSuccessful) {
                val idToken = tokenTask.result?.token
                val firebaseUid = currentUser.uid
                if (idToken != null) {
                    fetchUserProfile("Bearer $idToken", firebaseUid)
                } else {
                    onLoginFailure("Não foi possível obter o token de autenticação.")
                }
            } else {
                onLoginFailure("Erro ao renovar token: ${tokenTask.exception?.message}")
            }
        }
    }

    private fun signInUser(email: String, password: String) {
        binding.progressBar.visibility = View.VISIBLE
        binding.loginButton.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result.user
                    user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val idToken = tokenTask.result?.token
                            val firebaseUid = user.uid
                            if (idToken != null) {
                                fetchUserProfile("Bearer $idToken", firebaseUid)
                            } else {
                                onLoginFailure("Não foi possível obter o token de autenticação.")
                            }
                        } else {
                            onLoginFailure("Erro ao buscar token: ${tokenTask.exception?.message}")
                        }
                    }
                } else {
                    onLoginFailure("Falha na autenticação. Verifique e-mail e senha.")
                }
            }
    }

    private fun fetchUserProfile(authHeader: String, firebaseUid: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getMyProfile(authHeader, firebaseUid)

                if (response.isSuccessful && response.body() != null) {
                    val userProfile = response.body()!!
                    Log.d("API_CALL", "Perfil recebido do backend: $userProfile")
                    navigateToDashboard(userProfile)
                } else {
                    // Erro do servidor (ex: 401 Unauthorized, 404 Usuário não encontrado no BD)
                    onLoginFailure("Erro no servidor: ${response.code()} - Usuário não cadastrado ou sem permissão.")
                }
            } catch (e: Exception) {
                // Erro de rede (sem conexão, API offline, etc)
                Log.e("API_ERROR", "Erro de conexão", e)
                onLoginFailure("Erro de conexão com o servidor. Verifique sua internet.")
            }
        }
    }

    private fun navigateToDashboard(userProfile: UserProfile) {
        if (userProfile.status.equals("PENDENTE", ignoreCase = true)) {
            // Se o usuário está pendente, mostramos um aviso e o deslogamos.
            Toast.makeText(this, "Seu cadastro ainda está pendente de aprovação.", Toast.LENGTH_LONG).show()
            auth.signOut() // Desloga o usuário do Firebase
            binding.progressBar.visibility = View.GONE
            binding.loginButton.isEnabled = true
            return // Impede que o resto do código da função seja executado
        }

        // Se o status NÃO for "PENDENTE", a lógica continua como antes.
        val intent = when (userProfile.role.uppercase()) {
            "PROFESSOR" -> Intent(this, activity_teacher_dashboard::class.java)
            "ALUNO" -> Intent(this, activity_qr_scanner::class.java)
            "ADMIN_MASTER" -> Intent(this, activity_admin_dashboard::class.java)
            else -> {
                Toast.makeText(this, "Perfil desconhecido: ${userProfile.role}", Toast.LENGTH_LONG).show()
                null
            }
        }

        if (intent != null) {
            startActivity(intent)
            finish() // Fecha a MainActivity para que o usuário não possa voltar para ela
        } else {
            onLoginFailure("Seu perfil de usuário (${userProfile.role}) não é reconhecido.")
        }
    }

    private fun onLoginFailure(errorMessage: String) {
        Log.w("LOGIN_FAILURE", errorMessage)
        Toast.makeText(baseContext, errorMessage, Toast.LENGTH_LONG).show()
        binding.progressBar.visibility = View.GONE
        binding.loginButton.isEnabled = true
    }
}