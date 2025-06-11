// Em: app/src/main/java/com/example/presenteapp/activities/ActiveTeachersAdapter.kt
package com.example.presenteapp.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.presenteapp.databinding.ItemTeacheBinding
import com.example.presenteapp.network.model.UserProfile

class ActiveTeachersAdapter(
    private var teachers: MutableList<UserProfile>, // Mude de List para MutableList
    private val listener: OnTeacherRemoveClickListener // Adicione o listener
) : RecyclerView.Adapter<ActiveTeachersAdapter.ActiveTeacherViewHolder>() {

    // 1. Interface para comunicar o clique para a Activity
    interface OnTeacherRemoveClickListener {
        fun onRemoveClick(userProfile: UserProfile, position: Int)
    }

    inner class ActiveTeacherViewHolder(private val binding: ItemTeacheBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(teacher: UserProfile) {
            binding.teacherNameTextView.text = teacher.nome
            binding.teacherEmailTextView.text = teacher.email

            // 2. Configure o clique no botão de remover
            binding.removeTeacherButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onRemoveClick(teacher, position)
                }
            }
        }
    }

    // 3. Adicione uma função para remover o item da lista (e da UI)
    fun removeItem(position: Int) {
        if (position >= 0 && position < teachers.size) {
            teachers.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, teachers.size) // Atualiza as posições dos itens restantes
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ActiveTeacherViewHolder {
        val binding = ItemTeacheBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ActiveTeacherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ActiveTeacherViewHolder, position: Int) {
        val teacher = teachers[position]
        holder.bind(teacher)
    }

    override fun getItemCount(): Int = teachers.size
}