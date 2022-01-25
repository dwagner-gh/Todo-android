package com.example.todo.repo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ToDoRepository(
    private val store: ToDoEntity.Store,
    private val appScope: CoroutineScope
) {
    // map is an intermediate operator, intermediate operators set up a chain of operations,
    // that get executed lazily whenever a new value is emitted into the flow. I. e. this map
    // operation is executed on every new List<ToDoEntity> to create a new List<ToDoModel>.
    // https://developer.android.com/kotlin/flow
    fun items(): Flow<List<ToDoModel>> =
        store.all().map { all -> all.map { it.toModel() } }

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
}