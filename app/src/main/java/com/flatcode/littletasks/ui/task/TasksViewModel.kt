package com.flatcode.littletasks.ui.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littletasks.db.CategoryDao
import com.flatcode.littletasks.db.TasksDao
import com.flatcode.littletasks.model.Category
import com.flatcode.littletasks.model.Task
import com.flatcode.littletasks.repository.TaskRepository
import com.flatcode.littletasks.utils.DATA
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val repository: TaskRepository,
    private val categoryDao: CategoryDao,
    private val tasksDao: TasksDao
) : ViewModel() {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private var tasksJob: Job? = null

    fun fetchTasks(tasksType: String, orderBy: String) {
        val uid = auth.currentUser?.uid ?: return

        val tasksFlow = when (tasksType) {
            DATA.TASKS_UN_STARTED -> tasksDao.getUnstartedTasksByPublisher(uid)
            DATA.TASKS_STARTED -> tasksDao.getStartedTasksByPublisher(uid)
            DATA.TASKS_COMPLETED -> tasksDao.getCompletedTasksByPublisher(uid)
            else -> tasksDao.getAllTasksByPublisher(uid)
        }

        tasksJob?.cancel()
        tasksJob = viewModelScope.launch {
            combine(
                tasksFlow,
                categoryDao.getAllCategories()
            ) { localTasks, categoriesList ->
                val categoriesMap = categoriesList.associateBy { it.id }
                val list = mutableListOf<Task>()
                for (task in localTasks) {
                    val cat = categoriesMap[task.category]
                    task.categoryName = cat?.name ?: task.categoryName
                    task.categoryImage = cat?.image ?: task.categoryImage
                    list.add(task)
                }
                sortTasks(list, orderBy)
            }.collectLatest { resultList ->
                _tasks.value = resultList
            }
        }

        syncTasksFromFirebase()
    }

    private fun syncTasksFromFirebase() {
        database.getReference(DATA.CATEGORIES)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(catSnapshot: DataSnapshot) {
                    val catList = mutableListOf<Category>()
                    for (data in catSnapshot.children) {
                        val cat = data.getValue(Category::class.java) ?: continue
                        catList.add(cat)
                    }
                    viewModelScope.launch {
                        categoryDao.insertCategories(catList)
                    }

                    database.getReference(DATA.TASKS)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(tasksSnapshot: DataSnapshot) {
                                viewModelScope.launch {
                                    repository.syncTasks()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Timber.e(error.toException(), "Error syncing tasks")
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error syncing categories")
                }
            })
    }

    private fun sortTasks(list: List<Task>, orderBy: String): List<Task> {
        return when (orderBy) {
            DATA.POINTS -> list.sortedBy { it.points }
            DATA.AVAILABLE_POINTS -> list.sortedBy { it.aVPoints }
            DATA.START -> list.sortedBy { it.start }
            DATA.END -> list.sortedBy { it.end }
            DATA.TIMESTAMP -> list.sortedBy { it.timestamp }
            else -> list
        }
    }

    fun toggleFavorite(task: Task) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            repository.toggleFavorite(task.id, uid, !isTaskFavorite(task.id, uid))
        }
    }

    private suspend fun isTaskFavorite(taskId: String, userId: String): Boolean {
        return try {
            database.getReference(DATA.FAVORITES).child(userId).child(taskId).get().await().exists()
        } catch (_: Exception) {
            false
        }
    }

    fun onTaskAction(task: Task) {
        viewModelScope.launch {
            when {
                task.end != 0L -> {}
                task.start != 0L -> repository.setTaskEnd(task.id, task.points)
                else -> repository.setTaskStart(task.id)
            }
        }
    }

    fun deleteTask(databaseName: String, id: String) {
        viewModelScope.launch {
            repository.deleteTask(databaseName, id)
        }
    }

    fun updateTaskStatus(taskId: String, startStatus: Boolean, endStatus: Boolean) {
        viewModelScope.launch {
            repository.updateTaskStatus(taskId, startStatus, endStatus)
        }
    }

    fun observeFavoriteStatus(taskId: String, userId: String) =
        repository.isFavorite(taskId, userId)
}