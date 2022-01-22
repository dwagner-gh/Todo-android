package com.example.todo.repo
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.util.*

// entities used to be saved to the data base
// models are optimized for access by the UI
// view models abstract away even more details
@Entity(tableName = "todos", indices = [Index(value = ["id"])])
data class ToDoEntity(
    val description: String,
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val notes: String = "",
    val createdOn: Instant = Instant.now(),
    val isCompleted: Boolean = false
) {
    @Dao
    interface Store {
        @Query("SELECT * FROM todos ORDER BY description")
        fun all(): Flow<List<ToDoEntity>>
        @Query("SELECT * FROM todos WHERE id = :modelId")
        fun find(modelId: String?): Flow<ToDoEntity?>
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun save(vararg entities: ToDoEntity)
        @Delete
        suspend fun delete(vararg entities: ToDoEntity)
    }
}