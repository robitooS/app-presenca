package com.example.presenteapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.presenteapp.RetrofitInstance
import com.example.presenteapp.databinding.ActivityStudentRegistrationBinding
import com.example.presenteapp.network.model.UserRegistration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class activity_student_registration : AppCompatActivity() {

    private lateinit var binding: ActivityStudentRegistrationBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.nextButton.setOnClickListener {
            registerStudent()
        }
    }

    private fun registerStudent() {
        val name = binding.nameEditText.text.toString().trim()
        val course = binding.courseEditText.text.toString().trim()
        val ra = binding.raEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()
        val confirmPassword = binding.confirmPasswordEditText.text.toString().trim()

        if (name.isEmpty() || course.isEmpty() || ra.isEmpty() || email.isEmpty() || password.isEmpty()) {
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
                    val firebaseUid = task.result?.user?.uid
                    if (firebaseUid != null) {
                        saveStudentToBackend(firebaseUid, name, email, course, ra)
                    }
                } else {
                    Toast.makeText(baseContext, "Falha no cadastro: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveStudentToBackend(uid: String, name: String, email: String, course: String, ra: String) {
        val studentData = UserRegistration(
            firebaseUid = uid,
            nome = name,
            email = email,
            tipoUsuario = "ALUNO",
            curso = course,
            ra = ra,
            status = "PENDENTE"
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.registerUser(studentData)
                if (response.isSuccessful) {
                    Toast.makeText(this@activity_student_registration, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@activity_student_registration, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else {
                    Log.e("API_ERROR", "Erro no cadastro de aluno via API: ${response.code()} - ${response.errorBody()?.string()}")
                    Toast.makeText(this@activity_student_registration, "Erro ao salvar dados no servidor.", Toast.LENGTH_SHORT).show()
                    auth.currentUser?.delete()
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Exceção na chamada da API de aluno", e)
                Toast.makeText(this@activity_student_registration, "Erro de conexão: ${e.message}", Toast.LENGTH_SHORT).show()
                auth.currentUser?.delete()
            }
        }
    }
}