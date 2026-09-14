package com.flatcode.littletasks.ui.plan

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.data.model.Plan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _plans = MutableLiveData<List<Plan>>()
    val plans: LiveData<List<Plan>> = _plans

    private val _planInfo = MutableLiveData<Plan>()
    val planInfo: LiveData<Plan> = _planInfo

    private val _actionResult = MutableLiveData<Result<String>>()
    val actionResult: LiveData<Result<String>> = _actionResult

    fun loadPlans() {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.PLANS)
            .orderByChild("publisher")
            .equalTo(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Plan>()
                    for (data in snapshot.children) {
                        val item = data.getValue(Plan::class.java) ?: continue
                        list.add(item)
                    }
                    _plans.value = list
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun loadPlanInfo(planId: String) {
        database.getReference(DATA.PLANS).child(planId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(Plan::class.java) ?: return
                    _planInfo.value = item
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun addPlan(title: String, imageUri: Uri, extension: String) {
        val uid = auth.currentUser?.uid ?: return
        val ref = database.getReference(DATA.PLANS)
        val id = ref.push().key ?: return
        val filePath = "Images/Plans/$id.$extension"
        val storageRef = storage.getReference(filePath)

        storageRef.putFile(imageUri).addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener { uri ->
                val hashMap = HashMap<String, Any?>().apply {
                    put(DATA.PUBLISHER, uid)
                    put(DATA.TIMESTAMP, System.currentTimeMillis())
                    put(DATA.ID, id)
                    put(DATA.NAME, title)
                    put(DATA.IMAGE, uri.toString())
                }
                ref.child(id).setValue(hashMap).addOnSuccessListener {
                    _actionResult.value = Result.success("Plan added")
                }.addOnFailureListener { e ->
                    _actionResult.value = Result.failure(e)
                }
            }
        }.addOnFailureListener {
            _actionResult.value = Result.failure(it)
        }
    }

    fun updatePlan(planId: String, name: String, imageUri: Uri?, extension: String?) {
        if (imageUri == null) {
            updatePlanInDB(planId, name, null)
        } else {
            val filePath = "Images/Plans/$planId.$extension"
            val storageRef = storage.getReference(filePath)
            storageRef.putFile(imageUri).addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { uri ->
                    updatePlanInDB(planId, name, uri.toString())
                }
            }.addOnFailureListener {
                _actionResult.value = Result.failure(it)
            }
        }
    }

    private fun updatePlanInDB(planId: String, name: String, imageUrl: String?) {
        val hashMap = HashMap<String, Any>().apply {
            put(DATA.NAME, name)
            imageUrl?.let { put(DATA.IMAGE, it) }
        }
        database.getReference(DATA.PLANS).child(planId).updateChildren(hashMap)
            .addOnSuccessListener {
                _actionResult.value = Result.success("Plan updated")
            }.addOnFailureListener {
                _actionResult.value = Result.failure(it)
            }
    }
}
