package com.example.presenteapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.presenteapp.RetrofitInstance
import com.example.presenteapp.databinding.ActivityTeacherRegistrationBinding
import com.example.presenteapp.network.model.UserRegistration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class activity_teacher_registration : AppCompatActivity() {

    private lateinit var binding: ActivityTeacherRegistrationBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTeacherRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.nextButton.setOnClickListener {
            registerTeacher()
        }
    }

    private fun registerTeacher() {
        val name = binding.nameEditText.text.toString().trim()
        val course = binding.courseEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

        if (name.isEmpty() || course.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = task.result?.user
                    val firebaseUid = firebaseUser?.uid

                    if (firebaseUid != null) {
                        saveTeacherToBackend(firebaseUid, name, email, course)
                    } else {
                        Toast.makeText(this, "Falha ao obter o ID do usuário.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(baseContext, "Falha na criação do usuário: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveTeacherToBackend(uid: String, name: String, email: String, course: String) {
        val teacherData = UserRegistration(
            firebaseUid = uid,
            nome = name,
            email = email,
            tipoUsuario = "PROFESSOR",
            curso = course, // Adicionando curso
            status = "PENDENTE" // Professores começam como pendentes
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.registerUser(teacherData)

                if (response.isSuccessful) {
                    Toast.makeText(this@activity_teacher_registration, "Cadastro realizado! Aguardando aprovação do admin.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@activity_teacher_registration, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Log.e("API_ERROR", "Erro no cadastro via API: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@activity_teacher_registration, "Erro ao salvar dados no servidor.", Toast.LENGTH_SHORT).show()
                    auth.currentUser?.delete()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Exceção na chamada da API", e)
                Toast.makeText(this@activity_teacher_registration, "Erro de conexão: ${e.message}", Toast.LENGTH_SHORT).show()
                auth.currentUser?.delete()
            }
        }
    }
}