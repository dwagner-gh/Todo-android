package com.example.todo

class ToDoRepository {
    var items = listOf(
        ToDoModel(
            description = "Cleaning my room",
            isCompleted = true,
            notes = "Nasty spot in the kitchen. Buy cleaning materials."
        ),
        ToDoModel(
            description = "Conquer the world"
        ),
        ToDoModel(
            description = "Deepen knowledge of Kotlin",
            notes = "Look for good resources such as books/eBooks and articles. Skill comes in handy as a developer."
        )
    )

    fun save(model: ToDoModel) {
        items = if (items.any { it.id == model.id }) {
            items.map { if (it.id == model.id) model else it }
        } else {
            items + model
        }
    }

    fun find(modelId: String) = items.find { it.id == modelId }
}