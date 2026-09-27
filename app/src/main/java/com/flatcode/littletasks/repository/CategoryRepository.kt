package com.flatcode.littletasks.repository

import com.flatcode.littletasks.db.CategoryDao
import com.flatcode.littletasks.model.Category
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
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface CategoryRepository {
    fun getCategories(orderBy: String): Flow<Resource<List<Category>>>
    suspend fun deleteCategory(databaseName: String, id: String): Result<Unit>
}

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getCategories(orderBy: String): Flow<Resource<List<Category>>> = channelFlow {
        val localJob = launch {
            categoryDao.getAllCategories().collectLatest { localList ->
                send(Resource.Success(localList))
            }
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            localJob.join()
            return@channelFlow
        }

        val categoriesRef = database.getReference(DATA.CATEGORIES).orderByChild(orderBy)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val remoteList = mutableListOf<Category>()
                for (data in snapshot.children) {
                    val item = data.getValue(Category::class.java) ?: continue
                    if (item.publisher == uid) {
                        remoteList.add(item)
                    }
                }
                fetchTaskCountsAndSaveToRoom(remoteList)
            }

            override fun onCancelled(error: DatabaseError) {
                // If remote fails, Room cached data remains visible
            }
        }

        categoriesRef.addValueEventListener(listener)

        awaitClose {
            categoriesRef.removeEventListener(listener)
            localJob.cancel()
        }
    }

    private fun fetchTaskCountsAndSaveToRoom(categories: List<Category>) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.TASKS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val taskMap = mutableMapOf<String, Int>()
                    for (data in snapshot.children) {
                        val task = data.getValue(Task::class.java) ?: continue
                        if (task.publisher == uid) {
                            val catId = task.category ?: continue
                            taskMap[catId] = (taskMap[catId] ?: 0) + 1
                        }
                    }
                    categories.forEach { it.taskCount = taskMap[it.id] ?: 0 }
                    CoroutineScope(Dispatchers.IO).launch {
                        categoryDao.insertCategories(categories)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    CoroutineScope(Dispatchers.IO).launch {
                        categoryDao.insertCategories(categories)
                    }
                }
            })
    }

    override suspend fun deleteCategory(databaseName: String, id: String): Result<Unit> {
        return try {
            database.getReference(databaseName).child(id).removeValue().await()
            categoryDao.deleteCategoryById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            categoryDao.deleteCategoryById(id)
            Result.failure(e)
        }
    }
}
