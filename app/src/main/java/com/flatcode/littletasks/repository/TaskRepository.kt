package com.flatcode.littletasks.repository

import com.flatcode.littletasks.db.FavoriteDao
import com.flatcode.littletasks.db.TaskDao
import com.flatcode.littletasks.model.FavoriteEntity
import com.flatcode.littletasks.model.Task
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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
    fun getTasksByCategoryLocal(categoryId: String): Flow<List<Task>>
    fun getTasksByCategory(categoryId: String): Flow<Resource<List<Task>>>
    fun syncTasks()
    suspend fun insertTaskLocal(task: Task)
    suspend fun deleteTaskLocal(task: Task)
}

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val taskDao: TaskDao,
    private val favoriteDao: FavoriteDao
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
                favoriteDao.insertFavorite(FavoriteEntity(userId, taskId))
            } else {
                ref.removeValue().await()
                favoriteDao.deleteFavorite(userId, taskId)
            }
            Result.success(Unit)
        } catch (_: Exception) {
            if (isFavorite) {
                favoriteDao.insertFavorite(FavoriteEntity(userId, taskId))
            } else {
                favoriteDao.deleteFavorite(userId, taskId)
            }
            Result.success(Unit)
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
            if (startStatus) {
                hashMap[DATA.START] = DATA.ZERO
                hashMap[DATA.END] = DATA.ZERO
            } else if (endStatus) {
                hashMap[DATA.END] = DATA.ZERO
            }

            if (hashMap.isNotEmpty()) {
                database.getReference(DATA.TASKS).child(taskId).updateChildren(hashMap).await()
            }
            taskDao.getTaskById(taskId).firstOrNull()?.let { task ->
                if (startStatus) {
                    task.start = 0L
                    task.end = 0L
                } else if (endStatus) {
                    task.end = 0L
                }
                taskDao.insertTask(task)
            }
            Result.success(Unit)
        } catch (_: Exception) {
            taskDao.getTaskById(taskId).firstOrNull()?.let { task ->
                if (startStatus) {
                    task.start = 0L
                    task.end = 0L
                } else if (endStatus) {
                    task.end = 0L
                }
                taskDao.insertTask(task)
            }
            Result.success(Unit)
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
            if (databaseName == DATA.TASKS) {
                taskDao.deleteTaskById(id)
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    database.getReference(DATA.FAVORITES).child(uid).child(id).removeValue().await()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setTaskStart(taskId: String): Result<Unit> {
        val now = System.currentTimeMillis()
        return try {
            database.getReference(DATA.TASKS).child(taskId).child(DATA.START)
                .setValue(now).await()
            taskDao.getTaskById(taskId).firstOrNull()?.let { task ->
                task.start = now
                taskDao.insertTask(task)
            }
            Result.success(Unit)
        } catch (_: Exception) {
            taskDao.getTaskById(taskId).firstOrNull()?.let { task ->
                task.start = now
                taskDao.insertTask(task)
            }
            Result.success(Unit)
        }
    }

    override suspend fun setTaskEnd(taskId: String, points: Int): Result<Unit> {
        val now = System.currentTimeMillis()
        return try {
            val taskRef = database.getReference(DATA.TASKS).child(taskId)
            taskRef.child(DATA.END).setValue(now).await()
            taskRef.child(DATA.AVAILABLE_POINTS).setValue(points).await()
            taskDao.getTaskById(taskId).firstOrNull()?.let { task ->
                task.end = now
                task.aVPoints = points
                taskDao.insertTask(task)
            }
            Result.success(Unit)
        } catch (_: Exception) {
            taskDao.getTaskById(taskId).firstOrNull()?.let { task ->
                task.end = now
                task.aVPoints = points
                taskDao.insertTask(task)
            }
            Result.success(Unit)
        }
    }

    override fun getAllTasksLocal(): Flow<List<Task>> = taskDao.getAllTasks()

    override fun getTasksByCategoryLocal(categoryId: String): Flow<List<Task>> =
        taskDao.getTasksByCategory(categoryId)

    override fun getTasksByCategory(categoryId: String): Flow<Resource<List<Task>>> = channelFlow {
        val localJob = launch {
            taskDao.getTasksByCategory(categoryId).collectLatest { localList ->
                send(Resource.Success(localList))
            }
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            localJob.join()
            return@channelFlow
        }

        val ref = database.getReference(DATA.TASKS)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Task>()
                for (data in snapshot.children) {
                    val item = data.getValue(Task::class.java) ?: continue
                    if (item.category == categoryId && item.publisher == uid) {
                        list.add(item)
                    }
                }
                CoroutineScope(Dispatchers.IO).launch {
                    taskDao.insertTasks(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        ref.addValueEventListener(listener)

        awaitClose {
            ref.removeEventListener(listener)
            localJob.cancel()
        }
    }

    override fun syncTasks() {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.TASKS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Task>()
                    for (data in snapshot.children) {
                        val item = data.getValue(Task::class.java) ?: continue
                        if (item.publisher == uid) {
                            list.add(item)
                        }
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        taskDao.insertTasks(list)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        database.getReference(DATA.FAVORITES).child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favList = snapshot.children.mapNotNull { it.key }.map { FavoriteEntity(uid, it) }
                    CoroutineScope(Dispatchers.IO).launch {
                        favoriteDao.deleteAllFavoritesForUser(uid)
                        favoriteDao.insertFavorites(favList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override suspend fun insertTaskLocal(task: Task) {
        taskDao.insertTask(task)
    }

    override suspend fun deleteTaskLocal(task: Task) {
        taskDao.deleteTask(task)
    }
}