package com.example.todo.ui.roster

import androidx.recyclerview.widget.RecyclerView
import com.example.todo.repo.ToDoModel
import com.example.todo.databinding.TodoRowBinding

class RosterRowHolder(
    private val binding: TodoRowBinding,
    val onCheckboxToggle: (ToDoModel) -> Unit,
    val onRowClick: (ToDoModel) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(model: ToDoModel) {
        binding.apply {
            root.setOnClickListener { onRowClick(model) }
            checkBox.isChecked = model.isCompleted
            checkBox.setOnCheckedChangeListener { _, _ -> onCheckboxToggle(model) }
            desc.text = model.description
        }
    }
}