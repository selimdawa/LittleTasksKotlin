package com.flatcode.littletasks.ui.category

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.data.model.Category
import com.flatcode.littletasks.data.model.Plan
import com.flatcode.littletasks.data.model.Task
import com.flatcode.littletasks.data.model.TaskItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _planName = MutableLiveData<String>()
    val planName: LiveData<String> = _planName

    private val _uploadResult = MutableLiveData<Result<String>>()
    val uploadResult: LiveData<Result<String>> = _uploadResult

    private val _categoryTasks = MutableLiveData<List<Task>>()
    val categoryTasks: LiveData<List<Task>> = _categoryTasks

    private val _pointsSummary = MutableLiveData<Triple<Int, Int, Int>>() // all, av, level
    val pointsSummary: LiveData<Triple<Int, Int, Int>> = _pointsSummary

    fun getCategories(orderBy: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.CATEGORIES)
            .orderByChild(orderBy)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Category>()
                    for (data in snapshot.children) {
                        val item = data.getValue(Category::class.java) ?: continue
                        if (item.publisher == uid) {
                            list.add(item)
                        }
                    }
                    _categories.value = list.reversed()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun loadPlanName(planId: String) {
        database.getReference(DATA.PLANS).child(planId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val plan = snapshot.getValue(Plan::class.java) ?: return
                    _planName.value = plan.name ?: ""
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun addCategory(title: String, planId: String, imageUri: Uri, extension: String) {
        val uid = auth.currentUser?.uid ?: return
        val ref = database.getReference(DATA.CATEGORIES)
        val id = ref.push().key ?: return
        val filePath = "Images/Category/$id.$extension"
        val storageRef = storage.getReference(filePath)

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { uri ->
                    val hashMap = HashMap<String, Any?>().apply {
                        put(DATA.PUBLISHER, uid)
                        put(DATA.TIMESTAMP, System.currentTimeMillis())
                        put(DATA.ID, id)
                        put(DATA.NAME, title)
                        put(DATA.PLAN, planId)
                        put(DATA.IMAGE, uri.toString())
                    }
                    ref.child(id).setValue(hashMap).addOnSuccessListener {
                        addAutoTasksForCategory(id, planId)
                        _uploadResult.value = Result.success("Category uploaded")
                    }.addOnFailureListener { e ->
                        _uploadResult.value = Result.failure(e)
                    }
                }
            }
            .addOnFailureListener { _uploadResult.value = Result.failure(it) }
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

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun checkObjectAndAdd(taskItem: TaskItem, categoryId: String, planId: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.PLANS).child(planId).child(DATA.AUTO_TASKS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(taskItem.id!!).exists()) {
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
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun updateCategory(categoryId: String, name: String, imageUri: Uri?, extension: String?) {
        if (imageUri == null) {
            updateCategoryInDB(categoryId, name, null)
        } else {
            val filePath = "Images/Category/$categoryId.$extension"
            val storageRef = storage.getReference(filePath)
            storageRef.putFile(imageUri).addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { uri ->
                    updateCategoryInDB(categoryId, name, uri.toString())
                }
            }
        }
    }

    private fun updateCategoryInDB(categoryId: String, name: String, imageUrl: String?) {
        val hashMap = HashMap<String, Any>().apply {
            put(DATA.NAME, name)
            imageUrl?.let { put(DATA.IMAGE, it) }
        }
        database.getReference(DATA.CATEGORIES).child(categoryId).updateChildren(hashMap)
            .addOnSuccessListener { _uploadResult.value = Result.success("Category updated") }
            .addOnFailureListener { _uploadResult.value = Result.failure(it) }
    }

    fun getCategoryTasks(categoryId: String, orderBy: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.TASKS)
            .orderByChild(orderBy)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Task>()
                    var totalPoints = 0
                    var avPoints = 0
                    for (data in snapshot.children) {
                        val item = data.getValue(Task::class.java) ?: continue
                        if (item.category == categoryId && item.publisher == uid) {
                            list.add(item)
                            totalPoints += item.points
                            avPoints += item.aVPoints
                        }
                    }
                    _categoryTasks.value = list
                    // Level calculation logic from VOID.levelPoint
                    val level = levelPoint(avPoints, 10)
                    _pointsSummary.value = Triple(totalPoints, avPoints, level)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // Copying the levelPoint logic here for MVVM compliance
    private fun levelPoint(AVPoints: Int, initialPoint: Int): Int {
        var mutablePoint = initialPoint
        val half = mutablePoint / 2
        val thresholds = IntArray(21)
        thresholds[1] = mutablePoint * 5
        for (i in 2..20) {
            thresholds[i] = thresholds[i - 1] + half * (i + 1) * half
        }

        return when {
            AVPoints <= thresholds[1] -> AVPoints / mutablePoint
            AVPoints <= thresholds[20] -> {
                var stepIndex = 1
                while (stepIndex < 19 && AVPoints > thresholds[stepIndex + 1]) {
                    stepIndex++
                }
                val baseLevel = 5 * stepIndex
                val remainderPoints = AVPoints - thresholds[stepIndex]
                mutablePoint += half * (stepIndex - 1)
                baseLevel + (remainderPoints / mutablePoint)
            }
            else -> 100
        }
    }
}