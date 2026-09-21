package com.flatcode.littletasks.repository

import com.flatcode.littletasks.db.TaskDao
import com.flatcode.littletasks.model.Task
import com.flatcode.littletasks.utils.DATA
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface TaskRepository {
    fun isFavorite(taskId: String, userId: String): Flow<Boolean>
    suspend fun toggleFavorite(taskId: String, userId: String, isFavorite: Boolean): Result<Unit>
    fun isPlan(objectId: String, planId: String): Flow<Boolean>
    suspend fun togglePlan(objectId: String, planId: String, isAdded: Boolean): Result<Unit>
    suspend fun updateTaskStatus(
        taskId: String, startStatus: Boolean, endStatus: Boolean
    ): Result<Unit>

    fun observeTask(taskId: String): Flow<Task?>
    suspend fun deleteTask(databaseName: String, id: String): Result<Unit>
    suspend fun setTaskStart(taskId: String): Result<Unit>
    suspend fun setTaskEnd(taskId: String, points: Int): Result<Unit>

    // Room operations
    fun getAllTasksLocal(): Flow<List<Task>>
    suspend fun insertTaskLocal(task: Task)
    suspend fun deleteTaskLocal(task: Task)
}

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase, private val taskDao: TaskDao
) : TaskRepository {

    override fun isFavorite(taskId: String, userId: String): Flow<Boolean> = callbackFlow {
        val ref = database.getReference(DATA.FAVORITES).child(userId).child(taskId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun toggleFavorite(
        taskId: String, userId: String, isFavorite: Boolean
    ): Result<Unit> {
        return try {
            val ref = database.getReference(DATA.FAVORITES).child(userId).child(taskId)
            if (isFavorite) {
                ref.setValue(true).await()
            } else {
                ref.removeValue().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isPlan(objectId: String, planId: String): Flow<Boolean> = callbackFlow {
        val ref =
            database.getReference(DATA.PLANS).child(planId).child(DATA.AUTO_TASKS).child(objectId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    override suspend fun togglePlan(
        objectId: String, planId: String, isAdded: Boolean
    ): Result<Unit> {
        return try {
            val ref = database.getReference(DATA.PLANS).child(planId).child(DATA.AUTO_TASKS)
                .child(objectId)
            if (isAdded) {
                ref.setValue(true).await()
            } else {
                ref.removeValue().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTaskStatus(
        taskId: String, startStatus: Boolean, endStatus: Boolean
    ): Result<Unit> {
        return try {
            val hashMap = HashMap<String, Any>()
            if (startStatus) hashMap[DATA.START] = DATA.ZERO
            if (endStatus) hashMap[DATA.END] = DATA.ZERO

            if (hashMap.isNotEmpty()) {
                database.getReference(DATA.TASKS).child(taskId).updateChildren(hashMap).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeTask(taskId: String): Flow<Task?> = callbackFlow {
        val ref = database.getReference(DATA.TASKS).child(taskId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val task = snapshot.getValue(Task::class.java)
                trySend(task)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }.onEach { task ->
        task?.let { insertTaskLocal(it) }
    }

    override suspend fun deleteTask(databaseName: String, id: String): Result<Unit> {
        return try {
            database.getReference(databaseName).child(id).removeValue().await()
            // Also delete locally if it's a task
            if (databaseName == DATA.TASKS) {
                taskDao.deleteTaskById(id)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setTaskStart(taskId: String): Result<Unit> {
        return try {
            database.getReference(DATA.TASKS).child(taskId).child(DATA.START)
                .setValue(System.currentTimeMillis()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setTaskEnd(taskId: String, points: Int): Result<Unit> {
        return try {
            val taskRef = database.getReference(DATA.TASKS).child(taskId)
            taskRef.child(DATA.END).setValue(System.currentTimeMillis()).await()
            taskRef.child(DATA.AVAILABLE_POINTS).setValue(points).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllTasksLocal(): Flow<List<Task>> = taskDao.getAllTasks()

    override suspend fun insertTaskLocal(task: Task) {
        taskDao.insertTask(task)
    }

    override suspend fun deleteTaskLocal(task: Task) {
        taskDao.deleteTask(task)
    }
}