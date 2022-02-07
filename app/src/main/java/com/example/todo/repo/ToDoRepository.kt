package com.example.todo.repo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext

class ToDoRepository(
    private val store: ToDoEntity.Store,
    private val appScope: CoroutineScope
) {
    // map is an intermediate operator, intermediate operators set up a chain of operations,
    // that get executed lazily whenever a new value is emitted into the flow. I. e. this map
    // operation is executed on every new List<ToDoEntity> to create a new List<ToDoModel>.
    // https://developer.android.com/kotlin/flow
    fun items(filterMode: FilterMode = FilterMode.ALL): Flow<List<ToDoModel>> =
        filteredEntities(filterMode).map { all -> all.map { it.toModel() } }
        //    .onStart { delay(5000) } delay 5 seconds on flow collection

    fun find(id: String?): Flow<ToDoModel?> = store.find(id).map { it?.toModel() }

    suspend fun save(model: ToDoModel) {
        withContext(appScope.coroutineContext) {
            store.save(ToDoEntity(model))
        }
    }

    suspend fun delete(model: ToDoModel) {
        withContext(appScope.coroutineContext) {
            store.delete(ToDoEntity(model))
        }
    }

    private fun filteredEntities(filterMode: FilterMode) = when (filterMode) {
        FilterMode.ALL -> store.all()
        FilterMode.OUTSTANDING -> store.filtered(isCompleted = false)
        FilterMode.COMPLETED -> store.filtered(isCompleted = true)
    }
}

enum class FilterMode { ALL, OUTSTANDING, COMPLETED }