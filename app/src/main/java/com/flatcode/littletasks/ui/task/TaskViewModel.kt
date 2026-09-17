package com.flatcode.littletasks.ui.task

import androidx.lifecycle.ViewModel
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.model.Category
import com.flatcode.littletasks.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : ViewModel() {

    private val _categoryInfo = MutableStateFlow<Category?>(null)
    val categoryInfo: StateFlow<Category?> = _categoryInfo.asStateFlow()

    private val _taskInfo = MutableStateFlow<Task?>(null)
    val taskInfo: StateFlow<Task?> = _taskInfo.asStateFlow()

    private val _actionResult = MutableStateFlow<Result<String>?>(null)
    val actionResult: StateFlow<Result<String>?> = _actionResult.asStateFlow()

    fun loadCategoryInfo(catId: String) {
        database.getReference(DATA.CATEGORIES).child(catId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(Category::class.java) ?: return
                    _categoryInfo.value = item
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error loading category info for catId: $catId")
                }
            })
    }

    fun loadTaskInfo(taskId: String) {
        database.getReference(DATA.TASKS).child(taskId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(Task::class.java) ?: return
                    _taskInfo.value = item
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error loading task info for taskId: $taskId")
                }
            })
    }

    fun addTask(name: String, points: Int, categoryId: String) {
        val uid = auth.currentUser?.uid ?: return
        val ref = database.getReference(DATA.TASKS)
        val id = ref.push().key ?: return

        val hashMap = HashMap<String, Any?>().apply {
            put(DATA.PUBLISHER, uid)
            put(DATA.ID, id)
            put(DATA.NAME, name)
            put(DATA.POINTS, points)
            put(DATA.AVAILABLE_POINTS, DATA.ZERO)
            put(DATA.RANK, DATA.ZERO)
            put(DATA.CATEGORY, categoryId)
            put(DATA.TIMESTAMP, System.currentTimeMillis())
            put(DATA.START, DATA.ZERO)
            put(DATA.END, DATA.ZERO)
        }

        ref.child(id).setValue(hashMap).addOnSuccessListener {
            Timber.d("Task added successfully: $id")
            _actionResult.value = Result.success("Task uploaded")
        }.addOnFailureListener { e ->
            Timber.e(e, "Failed to add task to database")
            _actionResult.value = Result.failure(e)
        }
    }

    fun updateTask(taskId: String, name: String, points: Int) {
        val hashMap = HashMap<String, Any>().apply {
            put(DATA.NAME, name)
            put(DATA.POINTS, points)
        }

        database.getReference(DATA.TASKS).child(taskId).updateChildren(hashMap)
            .addOnSuccessListener {
                Timber.d("Task updated successfully: $taskId")
                _actionResult.value = Result.success("Task updated")
            }.addOnFailureListener { e ->
                Timber.e(e, "Failed to update task: $taskId")
                _actionResult.value = Result.failure(e)
            }
    }
}
