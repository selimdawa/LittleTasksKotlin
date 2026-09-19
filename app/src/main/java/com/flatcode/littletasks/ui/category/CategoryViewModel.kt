package com.flatcode.littletasks.ui.category

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littletasks.model.Category
import com.flatcode.littletasks.model.Plan
import com.flatcode.littletasks.model.Task
import com.flatcode.littletasks.model.TaskItem
import com.flatcode.littletasks.repository.TaskRepository
import com.flatcode.littletasks.utils.DATA
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val repository: TaskRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _planName = MutableStateFlow("")
    val planName: StateFlow<String> = _planName.asStateFlow()

    private val _uploadResult = MutableStateFlow<Result<String>?>(null)
    val uploadResult: StateFlow<Result<String>?> = _uploadResult.asStateFlow()

    private val _categoryTasks = MutableStateFlow<List<Task>>(emptyList())
    val categoryTasks: StateFlow<List<Task>> = _categoryTasks.asStateFlow()

    private val _pointsSummary = MutableStateFlow(Triple(0, 0, 0)) // all, av, level
    val pointsSummary: StateFlow<Triple<Int, Int, Int>> = _pointsSummary.asStateFlow()

    fun getCategories(orderBy: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.CATEGORIES).orderByChild(orderBy)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Category>()
                    for (data in snapshot.children) {
                        val item = data.getValue(Category::class.java) ?: continue
                        if (item.publisher == uid) {
                            list.add(item)
                        }
                    }
                    fetchTaskCountsAndPost(list)
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error fetching categories")
                }
            })
    }

    private fun fetchTaskCountsAndPost(categories: List<Category>) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.TASKS).addListenerForSingleValueEvent(object : ValueEventListener {
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
                _categories.value = categories.reversed()
            }

            override fun onCancelled(error: DatabaseError) {
                _categories.value = categories.reversed()
            }
        })
    }

    fun loadPlanName(planId: String) {
        database.getReference(DATA.PLANS).child(planId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val plan = snapshot.getValue(Plan::class.java) ?: return
                    _planName.value = plan.name ?: ""
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error loading plan name for planId: $planId")
                }
            })
    }

    fun addCategory(title: String, planId: String, imageUri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val ref = database.getReference(DATA.CATEGORIES)
        val id = ref.push().key ?: return

        MediaManager.get().upload(imageUri)
            .option("public_id", id)
            .option("folder", "Images/Category")
            .unsigned(DATA.CLOUDINARY_UPLOAD_PRESET)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val imageUrl = resultData?.get("secure_url") as? String ?: ""
                    val hashMap = HashMap<String, Any?>().apply {
                        put(DATA.PUBLISHER, uid)
                        put(DATA.TIMESTAMP, System.currentTimeMillis())
                        put(DATA.ID, id)
                        put(DATA.NAME, title)
                        put(DATA.PLAN, planId)
                        put(DATA.IMAGE, imageUrl)
                    }
                    ref.child(id).setValue(hashMap).addOnSuccessListener {
                        addAutoTasksForCategory(id, planId)
                        Timber.d("Category added successfully: $id")
                        _uploadResult.value = Result.success("Category uploaded")
                    }.addOnFailureListener { e ->
                        Timber.e(e, "Failed to add category to database")
                        _uploadResult.value = Result.failure(e)
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    val message = error?.description ?: "Unknown error"
                    Timber.e("Cloudinary upload error: $message")
                    _uploadResult.value = Result.failure(Exception(message))
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    private fun addAutoTasksForCategory(categoryId: String, planId: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.OBJECTS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (data in snapshot.children) {
                        val taskItem = data.getValue(TaskItem::class.java) ?: continue
                        if (taskItem.publisher == uid) {
                            checkObjectAndAdd(taskItem, categoryId, planId)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(
                        error.toException(), "Error adding auto tasks for category: $categoryId"
                    )
                }
            })
    }

    private fun checkObjectAndAdd(taskItem: TaskItem, categoryId: String, planId: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.PLANS).child(planId).child(DATA.AUTO_TASKS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(taskItem.id).exists()) {
                        val ref = database.getReference(DATA.TASKS)
                        val id = ref.push().key ?: return
                        val hashMap = HashMap<String, Any?>().apply {
                            put(DATA.PUBLISHER, uid)
                            put(DATA.ID, id)
                            put(DATA.NAME, taskItem.name)
                            put(DATA.POINTS, taskItem.points)
                            put(DATA.AVAILABLE_POINTS, DATA.ZERO)
                            put(DATA.RANK, DATA.ZERO)
                            put(DATA.CATEGORY, categoryId)
                            put(DATA.TIMESTAMP, System.currentTimeMillis())
                            put(DATA.START, DATA.ZERO)
                            put(DATA.END, DATA.ZERO)
                        }
                        ref.child(id).setValue(hashMap)
                            .addOnSuccessListener { Timber.d("Auto task added: $id") }
                            .addOnFailureListener { e -> Timber.e(e, "Failed to add auto task") }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error checking object for auto task")
                }
            })
    }

    fun updateCategory(categoryId: String, name: String, imageUri: Uri?) {
        if (imageUri == null) {
            updateCategoryInDB(categoryId, name, null)
        } else {
            val publicId = "${categoryId}_${System.currentTimeMillis()}"
            MediaManager.get().upload(imageUri)
                .option("public_id", publicId)
                .option("folder", "Images/Category")
                .unsigned(DATA.CLOUDINARY_UPLOAD_PRESET)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val imageUrl = resultData?.get("secure_url") as? String ?: ""
                        updateCategoryInDB(categoryId, name, imageUrl)
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        val message = error?.description ?: "Unknown error"
                        Timber.e("Cloudinary update upload error: $message")
                        _uploadResult.value = Result.failure(Exception(message))
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        }
    }

    private fun updateCategoryInDB(categoryId: String, name: String, imageUrl: String?) {
        val hashMap = HashMap<String, Any>().apply {
            put(DATA.NAME, name)
            imageUrl?.let { put(DATA.IMAGE, it) }
        }
        database.getReference(DATA.CATEGORIES).child(categoryId).updateChildren(hashMap)
            .addOnSuccessListener {
                Timber.d("Category updated successfully: $categoryId")
                _uploadResult.value = Result.success("Category updated")
            }.addOnFailureListener { e ->
                Timber.e(e, "Failed to update category in database")
                _uploadResult.value = Result.failure(e)
            }
    }

    fun getCategoryTasks(categoryId: String, orderBy: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.CATEGORIES).child(categoryId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(catSnapshot: DataSnapshot) {
                    val category = catSnapshot.getValue(Category::class.java)
                    database.getReference(DATA.TASKS).orderByChild(orderBy)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val list = mutableListOf<Task>()
                                var totalPoints = 0
                                var avPoints = 0
                                for (data in snapshot.children) {
                                    val item = data.getValue(Task::class.java) ?: continue
                                    if (item.category == categoryId && item.publisher == uid) {
                                        item.categoryName = category?.name
                                        item.categoryImage = category?.image
                                        list.add(item)
                                        totalPoints += item.points
                                        avPoints += item.aVPoints
                                    }
                                }
                                _categoryTasks.value = list
                                val level = levelPoint(avPoints)
                                _pointsSummary.value = Triple(totalPoints, avPoints, level)
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Timber.e(
                                    error.toException(),
                                    "Error fetching tasks for category: $categoryId"
                                )
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error fetching category for tasks")
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
                task.end != 0L -> {
                    // Task already completed
                }

                task.start != 0L -> {
                    repository.setTaskEnd(taskId, task.points)
                }

                else -> {
                    repository.setTaskStart(taskId)
                }
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

    // Copying the levelPoint logic here for MVVM compliance
    private fun levelPoint(avPoints: Int): Int {
        val initialPoint = 10
        var mutablePoint = initialPoint
        val half = mutablePoint / 2
        val thresholds = IntArray(21)
        thresholds[1] = mutablePoint * 5
        for (i in 2..20) {
            thresholds[i] = thresholds[i - 1] + half * (i + 1) * half
        }

        return when {
            avPoints <= thresholds[1] -> avPoints / mutablePoint
            avPoints <= thresholds[20] -> {
                var stepIndex = 1
                while (stepIndex < 19 && avPoints > thresholds[stepIndex + 1]) {
                    stepIndex++
                }
                val baseLevel = 5 * stepIndex
                val remainderPoints = avPoints - thresholds[stepIndex]
                mutablePoint += half * (stepIndex - 1)
                baseLevel + (remainderPoints / mutablePoint)
            }

            else -> 100
        }
    }
}