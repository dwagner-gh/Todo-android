package com.example.todo

import androidx.lifecycle.ViewModel

class SingleModelViewModel(
    private val repo: ToDoRepository,
    private val modelId: String
) : ViewModel() {

    fun getModel() = repo.find(modelId)
}