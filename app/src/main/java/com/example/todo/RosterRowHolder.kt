package com.example.todo

import androidx.recyclerview.widget.RecyclerView
import com.example.todo.databinding.TodoRowBinding

class RosterRowHolder(private val binding: TodoRowBinding, val onCheckboxToggle: (ToDoModel) -> Unit) : RecyclerView.ViewHolder(binding.root) {

    fun bind(model: ToDoModel) {
        binding.apply {
            checkBox.isChecked = model.isCompleted
            checkBox.setOnCheckedChangeListener { _, _ -> onCheckboxToggle(model) }
            desc.text = model.description
        }
    }
}