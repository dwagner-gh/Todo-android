package com.example.todo

import androidx.lifecycle.ViewModel
import com.example.todo.repo.ToDoModel
import com.example.todo.repo.ToDoRepository

class SingleModelViewModel(
    private val repo: ToDoRepository,
    private val modelId: String?
) : ViewModel() {

    fun getModel() = repo.find(modelId)
    fun save(model: ToDoModel) = repo.save(model)
    fun delete(model: ToDoModel) {
        repo.delete(model)
    }
}