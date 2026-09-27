package com.flatcode.littletasks.ui.objects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littletasks.model.TaskItem
import com.flatcode.littletasks.repository.ObjectRepository
import com.flatcode.littletasks.repository.TaskRepository
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ObjectsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val repository: TaskRepository,
    private val objectRepository: ObjectRepository
) : ViewModel() {

    private val _objects = MutableStateFlow<Resource<List<TaskItem>>>(Resource.Idle)
    val objects: StateFlow<Resource<List<TaskItem>>> = _objects.asStateFlow()

    private val _objectInfo = MutableStateFlow<TaskItem?>(null)
    val objectInfo: StateFlow<TaskItem?> = _objectInfo.asStateFlow()

    private val _actionResult = MutableStateFlow<Result<String>?>(null)
    val actionResult: StateFlow<Result<String>?> = _actionResult.asStateFlow()

    fun loadAllObjects() {
        viewModelScope.launch {
            objectRepository.getAllObjects().collectLatest {
                _objects.value = it
            }
        }
    }

    fun loadPlanObjects(planId: String) {
        viewModelScope.launch {
            objectRepository.getPlanObjects(planId).collectLatest {
                _objects.value = it
            }
        }
    }

    fun loadObjectInfo(objectId: String) {
        database.getReference(DATA.OBJECTS).child(objectId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(TaskItem::class.java) ?: return
                    _objectInfo.value = item
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun updateObject(objectId: String, name: String, points: Int) {
        val hashMap = HashMap<String, Any>().apply {
            put(DATA.NAME, name)
            put(DATA.POINTS, points)
        }
        database.getReference(DATA.OBJECTS).child(objectId).updateChildren(hashMap)
            .addOnSuccessListener {
                _actionResult.value = Result.success("Object updated")
            }.addOnFailureListener { e ->
                _actionResult.value = Result.failure(e)
            }
    }

    fun deleteTask(databaseName: String, id: String) {
        viewModelScope.launch {
            objectRepository.deleteObject(databaseName, id)
        }
    }

    fun togglePlan(objectId: String, planId: String, isAdded: Boolean) {
        viewModelScope.launch {
            repository.togglePlan(objectId, planId, isAdded)
        }
    }

    fun addObject(name: String, points: Int) {
        val uid = auth.currentUser?.uid ?: return
        val ref = database.getReference(DATA.OBJECTS)
        val id = ref.push().key ?: return

        val hashMap = HashMap<String, Any?>().apply {
            put(DATA.PUBLISHER, uid)
            put(DATA.ID, id)
            put(DATA.NAME, name)
            put(DATA.POINTS, points)
            put(DATA.TIMESTAMP, System.currentTimeMillis())
        }

        ref.child(id).setValue(hashMap).addOnSuccessListener {
            _actionResult.value = Result.success("Object added: $id")
        }.addOnFailureListener { e ->
            _actionResult.value = Result.failure(e)
        }
    }

    fun observePlanStatus(objectId: String, planId: String) = repository.isPlan(objectId, planId)
}
