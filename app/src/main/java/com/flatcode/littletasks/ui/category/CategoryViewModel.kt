package com.flatcode.littletasks.ui.category

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.flatcode.littletasks.db.CategoryDao
import com.flatcode.littletasks.db.TaskDao
import com.flatcode.littletasks.model.Category
import com.flatcode.littletasks.model.Plan
import com.flatcode.littletasks.model.Task
import com.flatcode.littletasks.model.TaskItem
import com.flatcode.littletasks.repository.CategoryRepository
import com.flatcode.littletasks.repository.PlanRepository
import com.flatcode.littletasks.repository.TaskRepository
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.Resource
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
class CategoryViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val repository: TaskRepository,
    private val categoryRepository: CategoryRepository,
    private val planRepository: PlanRepository,
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao
) : ViewModel() {

    private val _categories = MutableStateFlow<Resource<List<Category>>>(Resource.Idle)
    val categories: StateFlow<Resource<List<Category>>> = _categories.asStateFlow()

    private val _planName = MutableStateFlow("")
    val planName: StateFlow<String> = _planName.asStateFlow()

    private val _uploadResult = MutableStateFlow<Result<String>?>(null)
    val uploadResult: StateFlow<Result<String>?> = _uploadResult.asStateFlow()

    private val _categoryTasks = MutableStateFlow<List<Task>>(emptyList())
    val categoryTasks: StateFlow<List<Task>> = _categoryTasks.asStateFlow()

    private val _pointsSummary = MutableStateFlow(Triple(0, 0, 0)) // all, av, level
    val pointsSummary: StateFlow<Triple<Int, Int, Int>> = _pointsSummary.asStateFlow()

    fun getCategories(orderBy: String) {
        viewModelScope.launch {
            categoryRepository.getCategories(orderBy).collectLatest {
                _categories.value = it
            }
        }
    }

    fun setPlanName(name: String) {
        if (name.isNotEmpty()) {
            _planName.value = name
        }
    }

    fun loadPlanName(planId: String) {
        if (planId.isEmpty()) return

        viewModelScope.launch {
            val localPlan = planRepository.getPlanById(planId)
            localPlan?.name?.let { name ->
                if (name.isNotEmpty()) {
                    _planName.value = name
                }
            }
        }

        database.getReference(DATA.PLANS).child(planId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val plan = snapshot.getValue(Plan::class.java) ?: return
                    val name = plan.name ?: ""
                    if (name.isNotEmpty()) {
                        _planName.value = name
                    }
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

        MediaManager.get().upload(imageUri).option("public_id", id)
            .option("folder", "Images/Category").unsigned(DATA.CLOUDINARY_UPLOAD_PRESET)
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
            MediaManager.get().upload(imageUri).option("public_id", publicId)
                .option("folder", "Images/Category").unsigned(DATA.CLOUDINARY_UPLOAD_PRESET)
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

    private var categoryTasksJob: Job? = null

    fun getCategoryTasks(categoryId: String, orderBy: String) {
        if (categoryId.isEmpty()) return
        val uid = auth.currentUser?.uid ?: return

        categoryTasksJob?.cancel()
        categoryTasksJob = viewModelScope.launch {
            combine(
                taskDao.getTasksByCategory(categoryId),
                categoryDao.getAllCategories()
            ) { localTasks, categoriesList ->
                val categoriesById = categoriesList.associateBy { it.id }
                val categoriesByName = categoriesList.associateBy { it.name }
                val category = categoriesById[categoryId] ?: categoriesByName[categoryId]
                var totalPoints = 0
                var avPoints = 0
                val filteredList = mutableListOf<Task>()
                for (task in localTasks) {
                    if (task.publisher == uid) {
                        val taskCat = categoriesById[task.category] ?: categoriesByName[task.category] ?: category
                        val displayCatName = when {
                            taskCat != null -> taskCat.name
                            !task.categoryName.isNullOrEmpty() -> task.categoryName
                            task.category?.startsWith("-") == true -> category?.name ?: ""
                            else -> task.category
                        }
                        task.categoryName = displayCatName
                        task.categoryImage = taskCat?.image ?: task.categoryImage ?: category?.image
                        filteredList.add(task)
                        totalPoints += task.points
                        avPoints += task.aVPoints
                    }
                }
                val sorted = sortTasks(filteredList, orderBy)
                val level = levelPoint(avPoints)
                Pair(sorted, Triple(totalPoints, avPoints, level))
            }.collectLatest { (sortedTasks, summary) ->
                _categoryTasks.value = sortedTasks
                _pointsSummary.value = summary
            }
        }

        syncCategoryTasksFromFirebase(orderBy, uid)
    }

    private fun syncCategoryTasksFromFirebase(orderBy: String, uid: String) {
        database.getReference(DATA.TASKS).orderByChild(orderBy)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val remoteTasks = mutableListOf<Task>()
                    for (data in snapshot.children) {
                        val item = data.getValue(Task::class.java) ?: continue
                        if (item.publisher == uid) {
                            remoteTasks.add(item)
                        }
                    }
                    viewModelScope.launch {
                        taskDao.insertTasks(remoteTasks)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error syncing category tasks")
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
            if (databaseName == DATA.CATEGORIES) {
                categoryRepository.deleteCategory(databaseName, id)
            } else {
                repository.deleteTask(databaseName, id)
            }
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