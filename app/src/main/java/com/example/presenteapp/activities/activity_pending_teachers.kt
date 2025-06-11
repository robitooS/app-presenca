package com.example.presenteapp.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.presenteapp.RetrofitInstance
import com.example.presenteapp.databinding.ActivityPendingTeachersBinding
import com.example.presenteapp.network.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class activity_pending_teachers : AppCompatActivity(), PendingTeachersAdapter.OnActionClickListener {

    private lateinit var binding: ActivityPendingTeachersBinding
    private lateinit var adapter: PendingTeachersAdapter
    private lateinit var auth: FirebaseAuth
    private val pendingTeachersList = mutableListOf<UserProfile>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingTeachersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupRecyclerView()
        fetchPendingTeachers()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        adapter = PendingTeachersAdapter(pendingTeachersList, this)
        binding.pendingTeachersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.pendingTeachersRecyclerView.adapter = adapter
    }

    private fun fetchPendingTeachers() {
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyView.visibility = View.GONE

        auth.currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val token = "Bearer ${result.token}"
            lifecycleScope.launch {
                try {
                    // O parâmetro "role" não é mais necessário aqui
                    val response = RetrofitInstance.api.getPendingProf(token)
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val teachers = response.body()
                        if (teachers.isNullOrEmpty()) {
                            binding.emptyView.visibility = View.VISIBLE
                        } else {
                            pendingTeachersList.clear()
                            pendingTeachersList.addAll(teachers)
                            adapter.notifyDataSetChanged()
                        }
                    } else {
                        handleApiError("Falha ao buscar professores: ${response.code()}")
                    }
                } catch (e: Exception) {
                    handleApiError("Erro de conexão: ${e.message}")
                }
            }
        }?.addOnFailureListener {
            handleApiError("Falha ao obter token de autenticação.")
        }
    }


    // --- MÉTODOS DE AÇÃO CORRIGIDOS ---

    override fun onApproveClick(userProfile: UserProfile, position: Int) {
        // Assumindo que seu UserProfile agora tem o campo 'firebaseUid' do tipo String
        val professorUid = userProfile.firebaseUid ?: run {
            Toast.makeText(this, "Erro: UID do professor não encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        auth.currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val token = "Bearer ${result.token}"
            lifecycleScope.launch {
                try {
                    // ALTERADO: Chamando o novo método com o UID
                    val response = RetrofitInstance.api.approveProfessor(token, professorUid)
                    if (response.isSuccessful) {
                        Toast.makeText(this@activity_pending_teachers, "Professor aprovado!", Toast.LENGTH_SHORT).show()
                        adapter.removeItem(position)
                        checkEmptyState()
                    } else {
                        handleApiError("Falha ao aprovar: ${response.code()}")
                    }
                } catch (e: Exception) {
                    handleApiError("Erro de conexão: ${e.message}")
                } finally {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    override fun onRejectClick(userProfile: UserProfile, position: Int) {
        // Assumindo que seu UserProfile agora tem o campo 'firebaseUid' do tipo String
        val professorUid = userProfile.firebaseUid ?: run {
            Toast.makeText(this, "Erro: UID do professor não encontrado", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        auth.currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val token = "Bearer ${result.token}"
            lifecycleScope.launch {
                try {
                    // ALTERADO: Chamando o novo método com o UID
                    val response = RetrofitInstance.api.rejectProfessor(token, professorUid)
                    if (response.isSuccessful) {
                        Toast.makeText(this@activity_pending_teachers, "Professor rejeitado!", Toast.LENGTH_SHORT).show()
                        adapter.removeItem(position)
                        checkEmptyState()
                    } else {
                        handleApiError("Falha ao rejeitar: ${response.code()}")
                    }
                } catch (e: Exception) {
                    handleApiError("Erro de conexão: ${e.message}")
                } finally {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }


    private fun checkEmptyState() {
        if (pendingTeachersList.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
        }
    }

    private fun handleApiError(message: String) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        Log.e("PendingTeachersActivity", message)
    }
}