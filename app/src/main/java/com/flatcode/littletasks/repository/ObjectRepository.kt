package com.flatcode.littletasks.repository

import com.flatcode.littletasks.db.TaskItemDao
import com.flatcode.littletasks.model.TaskItem
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
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface ObjectRepository {
    fun getAllObjects(): Flow<Resource<List<TaskItem>>>
    fun getPlanObjects(planId: String): Flow<Resource<List<TaskItem>>>
    suspend fun deleteObject(databaseName: String, id: String): Result<Unit>
}

@Singleton
class ObjectRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val taskItemDao: TaskItemDao
) : ObjectRepository {

    override fun getAllObjects(): Flow<Resource<List<TaskItem>>> = channelFlow {
        val localJob = launch {
            taskItemDao.getAllTaskItems().collectLatest { localList ->
                send(Resource.Success(localList.reversed()))
            }
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            localJob.join()
            return@channelFlow
        }

        val objectsRef = database.getReference(DATA.OBJECTS).orderByChild("publisher").equalTo(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val remoteList = mutableListOf<TaskItem>()
                for (data in snapshot.children) {
                    val item = data.getValue(TaskItem::class.java) ?: continue
                    remoteList.add(item)
                }
                CoroutineScope(Dispatchers.IO).launch {
                    taskItemDao.insertTaskItems(remoteList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Keep showing cached data
            }
        }

        objectsRef.addValueEventListener(listener)

        awaitClose {
            objectsRef.removeEventListener(listener)
            localJob.cancel()
        }
    }

    override fun getPlanObjects(planId: String): Flow<Resource<List<TaskItem>>> = channelFlow {
        val localJob = launch {
            taskItemDao.getAllTaskItems().collectLatest { localList ->
                send(Resource.Success(localList.reversed()))
            }
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            localJob.join()
            return@channelFlow
        }

        val planTasksRef = database.getReference(DATA.PLANS).child(planId).child(DATA.AUTO_TASKS)
        val listener = object : ValueEventListener {
            override fun onDataChange(planSnapshot: DataSnapshot) {
                val keys = planSnapshot.children.mapNotNull { it.key }
                database.getReference(DATA.OBJECTS)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(objectsSnapshot: DataSnapshot) {
                            val remoteList = mutableListOf<TaskItem>()
                            for (data in objectsSnapshot.children) {
                                val item = data.getValue(TaskItem::class.java) ?: continue
                                if (item.id in keys && item.publisher == uid) {
                                    remoteList.add(item)
                                }
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                taskItemDao.insertTaskItems(remoteList)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        planTasksRef.addValueEventListener(listener)

        awaitClose {
            planTasksRef.removeEventListener(listener)
            localJob.cancel()
        }
    }

    override suspend fun deleteObject(databaseName: String, id: String): Result<Unit> {
        return try {
            database.getReference(databaseName).child(id).removeValue().await()
            taskItemDao.deleteTaskItemById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            taskItemDao.deleteTaskItemById(id)
            Result.failure(e)
        }
    }
}
