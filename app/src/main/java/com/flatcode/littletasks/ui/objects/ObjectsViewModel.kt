package com.flatcode.littletasks.ui.objects

import androidx.lifecycle.ViewModel
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.data.model.TaskItem
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
class ObjectsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : ViewModel() {

    private val _objects = MutableStateFlow<List<TaskItem>>(emptyList())
    val objects: StateFlow<List<TaskItem>> = _objects.asStateFlow()

    private val _objectInfo = MutableStateFlow<TaskItem?>(null)
    val objectInfo: StateFlow<TaskItem?> = _objectInfo.asStateFlow()

    private val _actionResult = MutableStateFlow<Result<String>?>(null)
    val actionResult: StateFlow<Result<String>?> = _actionResult.asStateFlow()

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

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error loading all objects for uid: $uid")
                }
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

                            override fun onCancelled(error: DatabaseError) {
                                Timber.e(error.toException(), "Error loading objects for plan: $planId")
                            }
                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error loading auto tasks for plan: $planId")
                }
            })
    }

    fun loadObjectInfo(objectId: String) {
        database.getReference(DATA.OBJECTS).child(objectId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(TaskItem::class.java) ?: return
                    _objectInfo.value = item
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error loading object info for objectId: $objectId")
                }
            })
    }

    fun updateObject(objectId: String, name: String, points: Int) {
        val hashMap = HashMap<String, Any>().apply {
            put(DATA.NAME, name)
            put(DATA.POINTS, points)
        }
        database.getReference(DATA.OBJECTS).child(objectId).updateChildren(hashMap)
            .addOnSuccessListener {
                Timber.d("Object updated successfully: $objectId")
                _actionResult.value = Result.success("Object updated")
            }
            .addOnFailureListener { e ->
                Timber.e(e, "Failed to update object: $objectId")
                _actionResult.value = Result.failure(e)
            }
    }
}
