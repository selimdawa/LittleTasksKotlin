package com.flatcode.littletasks.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.flatcode.littletasks.core.utils.DATA
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : ViewModel() {

    private val _authResult = MutableLiveData<Result<Unit>>()
    val authResult: LiveData<Result<Unit>> = _authResult

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _authResult.value = Result.success(Unit)
            }
            .addOnFailureListener {
                _authResult.value = Result.failure(it)
            }
    }

    fun register(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                updateUserInfo(name, email)
            }
            .addOnFailureListener {
                _authResult.value = Result.failure(it)
            }
    }

    private fun updateUserInfo(name: String, email: String) {
        val id = auth.uid ?: return
        val hashMap = HashMap<String, Any>()
        hashMap[DATA.EMAIL] = email
        hashMap[DATA.ID] = id
        hashMap[DATA.PROFILE_IMAGE] = DATA.BASIC
        hashMap[DATA.TIMESTAMP] = System.currentTimeMillis()
        hashMap[DATA.USER_NAME] = name
        hashMap[DATA.VERSION] = DATA.CURRENT_VERSION

        database.getReference(DATA.USERS).child(id).setValue(hashMap)
            .addOnSuccessListener {
                _authResult.value = Result.success(Unit)
            }
            .addOnFailureListener {
                _authResult.value = Result.failure(it)
            }
    }

    fun recoverPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                _authResult.value = Result.success(Unit)
            }
            .addOnFailureListener {
                _authResult.value = Result.failure(it)
            }
    }
}