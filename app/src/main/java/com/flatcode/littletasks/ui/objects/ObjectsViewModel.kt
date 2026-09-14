package com.flatcode.littletasks.ui.objects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.data.model.TaskItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ObjectsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : ViewModel() {

    private val _objects = MutableLiveData<List<TaskItem>>()
    val objects: LiveData<List<TaskItem>> = _objects

    private val _objectInfo = MutableLiveData<TaskItem>()
    val objectInfo: LiveData<TaskItem> = _objectInfo

    private val _actionResult = MutableLiveData<Result<String>>()
    val actionResult: LiveData<Result<String>> = _actionResult

    fun loadAllObjects() {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.OBJECTS)
            .orderByChild("publisher")
            .equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<TaskItem>()
                    for (data in snapshot.children) {
                        val item = data.getValue(TaskItem::class.java) ?: continue
                        list.add(item)
                    }
                    _objects.value = list
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun loadPlanObjects(planId: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.PLANS).child(planId).child(DATA.AUTO_TASKS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(planSnapshot: DataSnapshot) {
                    val keys = planSnapshot.children.mapNotNull { it.key }
                    database.getReference(DATA.OBJECTS)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(objectsSnapshot: DataSnapshot) {
                                val list = mutableListOf<TaskItem>()
                                for (data in objectsSnapshot.children) {
                                    val item = data.getValue(TaskItem::class.java) ?: continue
                                    if (item.id in keys && item.publisher == uid) {
                                        list.add(item)
                                    }
                                }
                                _objects.value = list
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
                override fun onCancelled(error: DatabaseError) {}
            })
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
            .addOnSuccessListener { _actionResult.value = Result.success("Object updated") }
            .addOnFailureListener { _actionResult.value = Result.failure(it) }
    }
}
