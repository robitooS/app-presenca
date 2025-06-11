// Garanta que o pacote corresponde à localização do seu arquivo
package com.example.presenteapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.presenteapp.databinding.ActivityRoleSelectionBinding

class activity_role_selection : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        // Define o layout da activity como a view raiz do binding
        setContentView(binding.root)

        // Ajusta o padding para as barras de sistema (código padrão)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 4. Configura a ação de clique para o botão "Sou Professor"
        binding.teacherButton.setOnClickListener {
            // Cria uma "Intenção" para abrir a tela de registro de professor
            val intent = Intent(this, activity_teacher_registration::class.java)
            // Inicia a nova tela
            startActivity(intent)
        }

        // 5. Configura a ação de clique para o botão "Sou Aluno"
        binding.studentButton.setOnClickListener {
            // Cria uma "Intenção" para abrir a tela de registro de aluno
            val intent = Intent(this, activity_student_registration::class.java)
            // Inicia a nova tela
            startActivity(intent)
        }
    }
}