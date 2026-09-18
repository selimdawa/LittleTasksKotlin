package com.flatcode.littletasks.ui.auth

import androidx.lifecycle.ViewModel
import com.flatcode.littletasks.utils.DATA
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth, private val database: FirebaseDatabase
) : ViewModel() {

    private val _authResult = MutableStateFlow<Result<Unit>?>(null)
    val authResult: StateFlow<Result<Unit>?> = _authResult.asStateFlow()

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                Timber.d("Login success for email: $email")
                _authResult.value = Result.success(Unit)
            }.addOnFailureListener {
                Timber.e(it, "Login failed for email: $email")
                _authResult.value = Result.failure(it)
            }
    }

    fun register(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
                Timber.d("Registration success for email: $email")
                updateUserInfo(name, email)
            }.addOnFailureListener {
                Timber.e(it, "Registration failed for email: $email")
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

        database.getReference(DATA.USERS).child(id).setValue(hashMap).addOnSuccessListener {
                Timber.d("User info update success for id: $id")
                _authResult.value = Result.success(Unit)
            }.addOnFailureListener {
                Timber.e(it, "User info update failed for id: $id")
                _authResult.value = Result.failure(it)
            }
    }

    fun recoverPassword(email: String) {
        auth.sendPasswordResetEmail(email).addOnSuccessListener {
                Timber.d("Password reset email sent to: $email")
                _authResult.value = Result.success(Unit)
            }.addOnFailureListener {
                Timber.e(it, "Password reset email failed for: $email")
                _authResult.value = Result.failure(it)
            }
    }
}