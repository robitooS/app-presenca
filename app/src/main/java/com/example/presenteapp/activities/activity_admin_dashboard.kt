// Em: app/src/main/java/com/example/presenteapp/activities/activity_admin_dashboard.kt

package com.example.presenteapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog // Importação necessária para o alerta
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.presenteapp.RetrofitInstance
import com.example.presenteapp.databinding.ActivityAdminDashboardBinding
import com.example.presenteapp.network.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

// 1. FAÇA A ACTIVITY IMPLEMENTAR A INTERFACE DO ADAPTER
class activity_admin_dashboard : AppCompatActivity(), ActiveTeachersAdapter.OnTeacherRemoveClickListener {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var activeTeachersAdapter: ActiveTeachersAdapter
    private val activeTeachersList = mutableListOf<UserProfile>()

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

        setupRecyclerView()
        setupLogoutButton()
        setupPendingTeachersButton()
        fetchActiveTeachers()
    }

    private fun setupRecyclerView() {
        // 2. PASSE A ACTIVITY (this) COMO O LISTENER AO CRIAR O ADAPTER
        activeTeachersAdapter = ActiveTeachersAdapter(activeTeachersList, this)
        binding.teachersRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.teachersRecyclerView.adapter = activeTeachersAdapter
    }

    private fun fetchActiveTeachers() {
        auth.currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val token = "Bearer ${result.token}"
            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.api.getProfessoresAtivos(token)

                    if (response.isSuccessful) {
                        val teachers = response.body()
                        activeTeachersList.clear() // Limpa a lista antes de adicionar novos itens
                        if (!teachers.isNullOrEmpty()) {
                            activeTeachersList.addAll(teachers)
                        } else {
                            Toast.makeText(this@activity_admin_dashboard, "Nenhum professor ativo encontrado.", Toast.LENGTH_SHORT).show()
                        }
                        activeTeachersAdapter.notifyDataSetChanged() // Notifica o adapter fora do if/else
                    } else {
                        Log.e("AdminDashboard", "Erro ao buscar professores ativos: ${response.code()} - ${response.message()}")
                        Toast.makeText(this@activity_admin_dashboard, "Falha ao carregar lista. Código: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("AdminDashboard", "Erro de conexão ou parsing ao buscar professores", e)
                    Toast.makeText(this@activity_admin_dashboard, "Erro de conexão com o servidor.", Toast.LENGTH_SHORT).show()
                }
            }
        }?.addOnFailureListener { exception ->
            Log.e("AdminDashboard", "Falha ao obter token de autenticação.", exception)
            Toast.makeText(this, "Sessão expirada. Faça o login novamente.", Toast.LENGTH_LONG).show()
            signOut()
        }
    }

    // 3. ADICIONE AS FUNÇÕES PARA LIDAR COM A REMOÇÃO
    override fun onRemoveClick(userProfile: UserProfile, position: Int) {
        // ---- INÍCIO DA VERIFICAÇÃO DE SEGURANÇA ----
        val uid = userProfile.firebaseUid
        if (uid == null) {
            // Se o ID for nulo, mostramos um aviso e impedimos a ação.
            Toast.makeText(this, "ERRO: Não foi possível remover, pois o ID do professor não foi encontrado.", Toast.LENGTH_LONG).show()
            return // Impede que o resto do código seja executado
        }
        // ---- FIM DA VERIFICAÇÃO DE SEGURANÇA ----

        // Se o ID existir, o código continua normalmente.
        AlertDialog.Builder(this)
            .setTitle("Confirmar Remoção")
            .setMessage("Tem certeza de que deseja remover o professor '${userProfile.nome}'?\n\nEsta ação não pode ser desfeita.")
            .setPositiveButton("Remover") { _, _ ->
                // Chamamos a função com o ID que já sabemos que não é nulo.
                removeTeacher(uid, position)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun removeTeacher(firebaseUid: String, position: Int) {
        auth.currentUser?.getIdToken(true)?.addOnSuccessListener { result ->
            val token = "Bearer ${result.token}"
            lifecycleScope.launch {
                try {
                    val response = RetrofitInstance.api.rejectProfessor(token, firebaseUid)
                    if (response.isSuccessful) {
                        Toast.makeText(this@activity_admin_dashboard, "Professor removido com sucesso!", Toast.LENGTH_SHORT).show()
                        activeTeachersAdapter.removeItem(position)
                    } else {
                        Log.e("AdminDashboard", "Falha ao remover professor: ${response.code()}")
                        Toast.makeText(this@activity_admin_dashboard, "Erro ao remover professor. Código: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("AdminDashboard", "Exceção ao remover professor", e)
                    Toast.makeText(this@activity_admin_dashboard, "Erro de conexão.", Toast.LENGTH_SHORT).show()
                }
            }
        }?.addOnFailureListener {
            Log.e("AdminDashboard", "Falha ao obter token para remoção", it)
            Toast.makeText(this, "Não foi possível obter token para a ação.", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupPendingTeachersButton() {
        binding.pendingTeachersButton.setOnClickListener {
            val intent = Intent(this, activity_pending_teachers::class.java)
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