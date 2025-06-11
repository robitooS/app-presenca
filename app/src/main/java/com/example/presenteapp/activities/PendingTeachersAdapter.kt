package com.example.presenteapp.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.presenteapp.databinding.ItemPendingTeacherBinding
import com.example.presenteapp.network.model.UserProfile

class PendingTeachersAdapter(
    private var teachers: MutableList<UserProfile>,
    private val listener: OnActionClickListener
) : RecyclerView.Adapter<PendingTeachersAdapter.TeacherViewHolder>() {

    // Interface para comunicar os cliques de volta para a Activity
    interface OnActionClickListener {
        fun onApproveClick(userProfile: UserProfile, position: Int)
        fun onRejectClick(userProfile: UserProfile, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeacherViewHolder {
        val binding = ItemPendingTeacherBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TeacherViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TeacherViewHolder, position: Int) {
        val teacher = teachers[position]
        holder.bind(teacher)
    }

    override fun getItemCount(): Int = teachers.size

    // Função para remover um item da lista e notificar o adapter
    fun removeItem(position: Int) {
        if (position in 0 until teachers.size) {
            teachers.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, teachers.size)
        }
    }

    inner class TeacherViewHolder(private val binding: ItemPendingTeacherBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(teacher: UserProfile) {
            binding.teacherNameTextView.text = teacher.nome
            binding.teacherEmailTextView.text = teacher.email

            // Configura os cliques nos botões
            binding.approveButton.setOnClickListener {
                listener.onApproveClick(teacher, adapterPosition)
            }
            binding.rejectButton.setOnClickListener {
                listener.onRejectClick(teacher, adapterPosition)
            }
        }
    }
}