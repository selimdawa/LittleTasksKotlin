package com.flatcode.littletasks.ui.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littletasks.model.Category
import com.flatcode.littletasks.model.Task
import com.flatcode.littletasks.model.User
import com.flatcode.littletasks.repository.TaskRepository
import com.flatcode.littletasks.utils.DATA
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage,
    private val repository: TaskRepository
) : ViewModel() {

    private val _userInfo = MutableStateFlow<User?>(null)
    val userInfo: StateFlow<User?> = _userInfo.asStateFlow()

    private val _nrTasks = MutableStateFlow(0)
    val nrTasks: StateFlow<Int> = _nrTasks.asStateFlow()

    private val _nrPlans = MutableStateFlow(0)
    val nrPlans: StateFlow<Int> = _nrPlans.asStateFlow()

    private val _nrObjects = MutableStateFlow(0)
    val nrObjects: StateFlow<Int> = _nrObjects.asStateFlow()

    private val _nrCategories = MutableStateFlow(0)
    val nrCategories: StateFlow<Int> = _nrCategories.asStateFlow()

    private val _favoriteTasks = MutableStateFlow<List<Task>>(emptyList())
    val favoriteTasks: StateFlow<List<Task>> = _favoriteTasks.asStateFlow()

    private val _actionResult = MutableStateFlow<Result<String>?>(null)
    val actionResult: StateFlow<Result<String>?> = _actionResult.asStateFlow()

    fun loadUserInfo(userId: String) {
        database.getReference(DATA.USERS).child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java) ?: return
                    _userInfo.value = user
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error loading user info for userId: $userId")
                }
            })
    }

    fun getNrItems(databaseName: String, userId: String) {
        database.getReference(databaseName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var i = 0
                    for (data in snapshot.children) {
                        val publisher = data.child("publisher").getValue(String::class.java)
                        if (publisher == userId) i++
                    }
                    when (databaseName) {
                        DATA.TASKS -> _nrTasks.value = i
                        DATA.PLANS -> _nrPlans.value = i
                        DATA.OBJECTS -> _nrObjects.value = i
                        DATA.CATEGORIES -> _nrCategories.value = i
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(
                        error.toException(),
                        "Error counting items in $databaseName for userId: $userId"
                    )
                }
            })
    }

    fun updateProfile(username: String, imageUri: Uri?, extension: String?) {
        val uid = auth.currentUser?.uid ?: return
        if (imageUri == null) {
            updateProfileInDB(uid, username, null)
        } else {
            val filePath = "Images/Profile/$uid.$extension"
            val storageRef = storage.getReference(filePath)
            storageRef.putFile(imageUri).addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { uri ->
                    updateProfileInDB(uid, username, uri.toString())
                }
            }.addOnFailureListener {
                Timber.e(it, "Failed to upload profile image for uid: $uid")
                _actionResult.value = Result.failure(it)
            }
        }
    }

    private fun updateProfileInDB(uid: String, username: String, imageUrl: String?) {
        val hashMap = HashMap<String, Any>().apply {
            put(DATA.USER_NAME, username)
            imageUrl?.let { put(DATA.PROFILE_IMAGE, it) }
        }
        database.getReference(DATA.USERS).child(uid).updateChildren(hashMap).addOnSuccessListener {
            Timber.d("Profile updated successfully for uid: $uid")
            _actionResult.value = Result.success("Profile updated")
        }.addOnFailureListener { e ->
            Timber.e(e, "Failed to update profile in database for uid: $uid")
            _actionResult.value = Result.failure(e)
        }
    }

    fun fetchFavoriteTasks(tasksType: String, orderBy: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.CATEGORIES)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(catSnapshot: DataSnapshot) {
                    val categoriesMap = mutableMapOf<String, Category>()
                    for (data in catSnapshot.children) {
                        val cat = data.getValue(Category::class.java) ?: continue
                        categoriesMap[cat.id] = cat
                    }

                    database.getReference(DATA.FAVORITES).child(uid)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val favoriteKeys = snapshot.children.mapNotNull { it.key }
                                database.getReference(DATA.TASKS).orderByChild(orderBy)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(tasksSnapshot: DataSnapshot) {
                                            val list = mutableListOf<Task>()
                                            for (data in tasksSnapshot.children) {
                                                val task = data.getValue(Task::class.java) ?: continue
                                                if (task.id in favoriteKeys && task.publisher == uid) {
                                                    val category = categoriesMap[task.category]
                                                    task.categoryName = category?.name
                                                    task.categoryImage = category?.image
                                                    when (tasksType) {
                                                        DATA.TASKS_ALL -> list.add(task)
                                                        DATA.TASKS_UN_STARTED -> if (task.start == 0L && task.end == 0L) list.add(
                                                            task
                                                        )

                                                        DATA.TASKS_STARTED -> if (task.start != 0L && task.end == 0L) list.add(
                                                            task
                                                        )

                                                        DATA.TASKS_COMPLETED -> if (task.start != 0L && task.end != 0L) list.add(
                                                            task
                                                        )
                                                    }
                                                }
                                            }
                                            _favoriteTasks.value = list
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Timber.e(
                                                error.toException(),
                                                "Error fetching tasks for favorites"
                                            )
                                        }
                                    })
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Timber.e(
                                    error.toException(),
                                    "Error fetching favorite keys for uid: $uid"
                                )
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error fetching categories for favorites mapping")
                }
            })
    }

    fun toggleFavorite(task: Task) {
        val uid = auth.currentUser?.uid ?: return
        val taskId = task.id
        viewModelScope.launch {
            val isFav = isTaskFavorite(taskId, uid)
            repository.toggleFavorite(taskId, uid, !isFav)
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
        val taskId = task.id
        viewModelScope.launch {
            when {
                task.end != 0L -> {}
                task.start != 0L -> repository.setTaskEnd(taskId, task.points)
                else -> repository.setTaskStart(taskId)
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