package com.example.todo

import androidx.lifecycle.ViewModel

class RosterViewModel(private val repo: ToDoRepository) : ViewModel() {
    val items = repo.items

    fun save(model: ToDoModel) {
        repo.save(model)
    }
}