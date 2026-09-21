package com.flatcode.littletasks.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flatcode.littletasks.model.TaskItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskItemDao {
    @Query("SELECT * FROM task_items ORDER BY timestamp DESC")
    fun getAllTaskItems(): Flow<List<TaskItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTaskItem(taskItem: TaskItem)

    @Delete
    suspend fun deleteTaskItem(taskItem: TaskItem)

    @Query("DELETE FROM task_items")
    suspend fun deleteAllTaskItems()
}