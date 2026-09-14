package com.flatcode.littletasks.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.data.model.Task
import com.flatcode.littletasks.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _userInfo = MutableLiveData<User>()
    val userInfo: LiveData<User> = _userInfo

    private val _nrTasks = MutableLiveData<Int>()
    val nrTasks: LiveData<Int> = _nrTasks

    private val _nrPlans = MutableLiveData<Int>()
    val nrPlans: LiveData<Int> = _nrPlans

    private val _nrObjects = MutableLiveData<Int>()
    val nrObjects: LiveData<Int> = _nrObjects

    private val _nrCategories = MutableLiveData<Int>()
    val nrCategories: LiveData<Int> = _nrCategories

    private val _favoriteTasks = MutableLiveData<List<Task>>()
    val favoriteTasks: LiveData<List<Task>> = _favoriteTasks

    private val _actionResult = MutableLiveData<Result<String>>()
    val actionResult: LiveData<Result<String>> = _actionResult

    fun loadUserInfo(userId: String) {
        database.getReference(DATA.USERS).child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java) ?: return
                    _userInfo.value = user
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun getNrItems(databaseName: String, userId: String) {
        database.getReference(databaseName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var i = 0
                    for (data in snapshot.children) {
                        val publisher = data.child("publisher").getValue(String::class.java)
                        if (publisher == userId) i++
                    }
                    when (databaseName) {
                        DATA.TASKS -> _nrTasks.value = i
                        DATA.PLANS -> _nrPlans.value = i
                        DATA.OBJECTS -> _nrObjects.value = i
                        DATA.CATEGORIES -> _nrCategories.value = i
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun updateProfile(username: String, imageUri: Uri?, extension: String?) {
        val uid = auth.currentUser?.uid ?: return
        if (imageUri == null) {
            updateProfileInDB(uid, username, null)
        } else {
            val filePath = "Images/Profile/$uid.$extension"
            val storageRef = storage.getReference(filePath)
            storageRef.putFile(imageUri).addOnSuccessListener {
                it.storage.downloadUrl.addOnSuccessListener { uri ->
                    updateProfileInDB(uid, username, uri.toString())
                }
            }.addOnFailureListener {
                _actionResult.value = Result.failure(it)
            }
        }
    }

    private fun updateProfileInDB(uid: String, username: String, imageUrl: String?) {
        val hashMap = HashMap<String, Any>().apply {
            put(DATA.USER_NAME, username)
            imageUrl?.let { put(DATA.PROFILE_IMAGE, it) }
        }
        database.getReference(DATA.USERS).child(uid).updateChildren(hashMap)
            .addOnSuccessListener { _actionResult.value = Result.success("Profile updated") }
            .addOnFailureListener { _actionResult.value = Result.failure(it) }
    }

    fun fetchFavoriteTasks(tasksType: String, orderBy: String) {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.FAVORITES).child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favoriteKeys = snapshot.children.mapNotNull { it.key }
                    database.getReference(DATA.TASKS).orderByChild(orderBy)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(tasksSnapshot: DataSnapshot) {
                                val list = mutableListOf<Task>()
                                for (data in tasksSnapshot.children) {
                                    val task = data.getValue(Task::class.java) ?: continue
                                    if (task.id in favoriteKeys && task.publisher == uid) {
                                        when (tasksType) {
                                            DATA.TASKS_ALL -> list.add(task)
                                            DATA.TASKS_UN_STARTED -> if (task.start == 0L && task.end == 0L) list.add(task)
                                            DATA.TASKS_STARTED -> if (task.start != 0L && task.end == 0L) list.add(task)
                                            DATA.TASKS_COMPLETED -> if (task.start != 0L && task.end != 0L) list.add(task)
                                        }
                                    }
                                }
                                _favoriteTasks.value = list
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
