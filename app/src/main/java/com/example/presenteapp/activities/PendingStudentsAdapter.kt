package com.example.presenteapp.activities

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.presenteapp.databinding.ItemPendingStudentBinding
import com.example.presenteapp.network.model.UserProfile

class PendingStudentsAdapter(
    private var students: MutableList<UserProfile>,
    private val listener: OnActionClickListener
) : RecyclerView.Adapter<PendingStudentsAdapter.StudentViewHolder>() {

    interface OnActionClickListener {
        fun onApproveClick(student: UserProfile, position: Int)
        fun onRejectClick(student: UserProfile, position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val binding = ItemPendingStudentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        val student = students[position]
        holder.bind(student)
    }

    override fun getItemCount(): Int = students.size

    fun removeItem(position: Int) {
        if (position in 0 until students.size) {
            students.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, students.size)
        }
    }

    inner class StudentViewHolder(private val binding: ItemPendingStudentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(student: UserProfile) {
            binding.studentNameTextView.text = student.nome
            binding.studentEmailTextView.text = student.email

            binding.approveButton.setOnClickListener {
                listener.onApproveClick(student, adapterPosition)
            }
            binding.rejectButton.setOnClickListener {
                listener.onRejectClick(student, adapterPosition)
            }
        }
    }
}