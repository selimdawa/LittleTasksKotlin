package com.flatcode.littletasks.ui.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.data.model.Category
import com.flatcode.littletasks.data.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : ViewModel() {

    private val _categoryInfo = MutableLiveData<Category>()
    val categoryInfo: LiveData<Category> = _categoryInfo

    private val _taskInfo = MutableLiveData<Task>()
    val taskInfo: LiveData<Task> = _taskInfo

    private val _actionResult = MutableLiveData<Result<String>>()
    val actionResult: LiveData<Result<String>> = _actionResult

    fun loadCategoryInfo(catId: String) {
        database.getReference(DATA.CATEGORIES).child(catId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(Category::class.java) ?: return
                    _categoryInfo.value = item
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun loadTaskInfo(taskId: String) {
        database.getReference(DATA.TASKS).child(taskId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(Task::class.java) ?: return
                    _taskInfo.value = item
                }
                override fun onCancelled(error: DatabaseError) {}
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
            _actionResult.value = Result.success("Task uploaded")
        }.addOnFailureListener { e ->
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
                _actionResult.value = Result.success("Task updated")
            }.addOnFailureListener { e ->
                _actionResult.value = Result.failure(e)
            }
    }
}
