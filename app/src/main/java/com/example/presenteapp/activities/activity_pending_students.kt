package com.example.presenteapp.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.presenteapp.RetrofitInstance
import com.example.presenteapp.databinding.ActivityPendingStudentsBinding
import com.example.presenteapp.network.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import retrofit2.Response

class activity_pending_students : AppCompatActivity(), PendingStudentsAdapter.OnActionClickListener {

    private lateinit var binding: ActivityPendingStudentsBinding
    private lateinit var adapter: PendingStudentsAdapter
    private lateinit var auth: FirebaseAuth
    private val pendingStudentsList = mutableListOf<UserProfile>()

    private enum class StudentAction { APPROVE, REJECT }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // O nome do binding é derivado do nome do layout: activity_pending_students.xml -> ActivityPendingStudentsBinding
        binding = ActivityPendingStudentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupRecyclerView()
        fetchPendingStudents()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = PendingStudentsAdapter(pendingStudentsList, this)
        binding.pendingStudentsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.pendingStudentsRecyclerView.adapter = adapter
    }

    private fun fetchPendingStudents() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE

        auth.currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val token = "Bearer ${result.token}"
            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.api.getPendingStudents(token)
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val students = response.body()
                        if (students.isNullOrEmpty()) {
                            binding.emptyView.visibility = View.VISIBLE
                        } else {
                            pendingStudentsList.clear()
                            pendingStudentsList.addAll(students)
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        handleApiError("Falha ao buscar alunos: ${response.code()}")
                    }
                } catch (e: Exception) {
                    handleApiError("Erro de conexão: ${e.message}")
                }
            }
        }?.addOnFailureListener {
            handleApiError("Falha ao obter token de autenticação.")
        }
    }

    override fun onApproveClick(student: UserProfile, position: Int) {
        performStudentAction(student, position, StudentAction.APPROVE)
    }

    override fun onRejectClick(student: UserProfile, position: Int) {
        performStudentAction(student, position, StudentAction.REJECT)
    }

    private fun performStudentAction(student: UserProfile, position: Int, action: StudentAction) {
        val studentUid = student.firebaseUid
        if (studentUid.isNullOrBlank()) {
            Toast.makeText(this, "Erro: UID do aluno inválido.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE

        auth.currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val token = "Bearer ${result.token}"

            lifecycleScope.launch {
                try {
                    val response: Response<*> = when (action) {
                        StudentAction.APPROVE -> RetrofitInstance.api.approveStudent(token, studentUid)
                        StudentAction.REJECT -> RetrofitInstance.api.rejectStudent(token, studentUid)
                    }

                    if (response.isSuccessful) {
                        val successMessage = if (action == StudentAction.APPROVE) "Aluno aprovado!" else "Aluno rejeitado!"
                        Toast.makeText(this@activity_pending_students, successMessage, Toast.LENGTH_SHORT).show()
                        adapter.removeItem(position)
                        checkEmptyState()
                    } else {
                        val errorMessage = if (action == StudentAction.APPROVE) "Falha ao aprovar" else "Falha ao rejeitar"
                        handleApiError("$errorMessage: ${response.code()}")
                    }
                } catch (e: Exception) {
                    handleApiError("Erro de conexão: ${e.message}")
                } finally {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }?.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            handleApiError("Falha ao autenticar para realizar a ação.")
        }
    }

    private fun checkEmptyState() {
        if (pendingStudentsList.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
        }
    }

    private fun handleApiError(message: String) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e("activity_pending_students", message) // Log tag corrigida
    }
}
