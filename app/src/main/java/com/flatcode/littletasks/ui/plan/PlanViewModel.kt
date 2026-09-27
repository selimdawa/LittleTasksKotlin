package com.flatcode.littletasks.ui.plan

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.flatcode.littletasks.model.Plan
import com.flatcode.littletasks.repository.PlanRepository
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
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PlanViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val planRepository: PlanRepository
) : ViewModel() {

    private val _plans = MutableStateFlow<Resource<List<Plan>>>(Resource.Idle)
    val plans: StateFlow<Resource<List<Plan>>> = _plans.asStateFlow()

    private val _planInfo = MutableStateFlow<Plan?>(null)
    val planInfo: StateFlow<Plan?> = _planInfo.asStateFlow()

    private val _actionResult = MutableStateFlow<Result<String>?>(null)
    val actionResult: StateFlow<Result<String>?> = _actionResult.asStateFlow()

    fun loadPlans() {
        viewModelScope.launch {
            planRepository.getPlans().collectLatest {
                _plans.value = it
            }
        }
    }

    fun loadPlanInfo(planId: String) {
        database.getReference(DATA.PLANS).child(planId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(Plan::class.java) ?: return
                    _planInfo.value = item
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error loading plan info for planId: $planId")
                }
            })
    }

    fun addPlan(title: String, imageUri: Uri) {
        val uid = auth.currentUser?.uid ?: return
        val ref = database.getReference(DATA.PLANS)
        val id = ref.push().key ?: return

        MediaManager.get().upload(imageUri).option("public_id", id).option("folder", "Images/Plans")
            .unsigned(DATA.CLOUDINARY_UPLOAD_PRESET).callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val imageUrl = resultData?.get("secure_url") as? String ?: ""
                    val hashMap = HashMap<String, Any?>().apply {
                        put(DATA.PUBLISHER, uid)
                        put(DATA.TIMESTAMP, System.currentTimeMillis())
                        put(DATA.ID, id)
                        put(DATA.NAME, title)
                        put(DATA.IMAGE, imageUrl)
                    }
                    ref.child(id).setValue(hashMap).addOnSuccessListener {
                        Timber.d("Plan added successfully: $id")
                        _actionResult.value = Result.success("Plan added")
                    }.addOnFailureListener { e ->
                        Timber.e(e, "Failed to add plan to database")
                        _actionResult.value = Result.failure(e)
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    val message = error?.description ?: "Unknown error"
                    Timber.e("Cloudinary plan upload error: $message")
                    _actionResult.value = Result.failure(Exception(message))
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    fun updatePlan(planId: String, name: String, imageUri: Uri?) {
        if (imageUri == null) {
            updatePlanInDB(planId, name, null)
        } else {
            val publicId = "${planId}_${System.currentTimeMillis()}"
            MediaManager.get().upload(imageUri).option("public_id", publicId)
                .option("folder", "Images/Plans").unsigned(DATA.CLOUDINARY_UPLOAD_PRESET)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {}
                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val imageUrl = resultData?.get("secure_url") as? String ?: ""
                        updatePlanInDB(planId, name, imageUrl)
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        val message = error?.description ?: "Unknown error"
                        Timber.e("Cloudinary plan update error: $message")
                        _actionResult.value = Result.failure(Exception(message))
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        }
    }

    private fun updatePlanInDB(planId: String, name: String, imageUrl: String?) {
        val hashMap = HashMap<String, Any>().apply {
            put(DATA.NAME, name)
            imageUrl?.let { put(DATA.IMAGE, it) }
        }
        database.getReference(DATA.PLANS).child(planId).updateChildren(hashMap)
            .addOnSuccessListener {
                Timber.d("Plan updated successfully: $planId")
                _actionResult.value = Result.success("Plan updated")
            }.addOnFailureListener { e ->
                Timber.e(e, "Failed to update plan in database")
                _actionResult.value = Result.failure(e)
            }
    }

    fun deletePlan(id: String) {
        viewModelScope.launch {
            planRepository.deletePlan(DATA.PLANS, id)
        }
    }
}