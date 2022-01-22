package com.example.todo.repo

class ToDoRepository {
    var items = emptyList<ToDoModel>()

    fun find(modelId: String?) = items.find { it.id == modelId }

    fun save(model: ToDoModel) {
        items = if (items.any { it.id == model.id }) {
            items.map { if (it.id == model.id) model else it }
        } else {
            items + model
        }
    }

    fun delete(model: ToDoModel) {
        items = items.filter { it.id != model.id }
    }
}