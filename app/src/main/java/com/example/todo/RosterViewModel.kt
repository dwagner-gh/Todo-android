package com.example.todo

import androidx.lifecycle.ViewModel

class RosterViewModel(private val repo: ToDoRepository) : ViewModel() {
    // original version contains a bug, saves() creates a new list in the repository
    // items would however always point to the old list, with this getter, the view model
    // always gets the new and updated list of items even after a save
    val items
        get() = repo.items

    fun save(model: ToDoModel) {
        repo.save(model)
    }
}