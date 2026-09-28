package com.flatcode.littletasks.db

import androidx.room.Dao
import androidx.room.Query
import com.flatcode.littletasks.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TasksDao {

    @Query("SELECT * FROM tasks WHERE publisher = :publisher ORDER BY timestamp DESC")
    fun getAllTasksByPublisher(publisher: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE publisher = :publisher AND start = 0 AND `end` = 0 ORDER BY timestamp DESC")
    fun getUnstartedTasksByPublisher(publisher: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE publisher = :publisher AND start != 0 AND `end` = 0 ORDER BY timestamp DESC")
    fun getStartedTasksByPublisher(publisher: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE publisher = :publisher AND start != 0 AND `end` != 0 ORDER BY timestamp DESC")
    fun getCompletedTasksByPublisher(publisher: String): Flow<List<Task>>
}