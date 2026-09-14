package com.flatcode.littletasks.ui.main

import androidx.lifecycle.ViewModel
import com.flatcode.littletasks.core.utils.DATA
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
class MainViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) : ViewModel() {

    private val _profileImage = MutableStateFlow("")
    val profileImage: StateFlow<String> = _profileImage.asStateFlow()

    fun loadUserInfo() {
        val uid = auth.currentUser?.uid ?: return
        database.getReference(DATA.USERS).child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val image = DATA.EMPTY + snapshot.child(DATA.PROFILE_IMAGE).value
                    _profileImage.value = image
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error loading user info for uid: $uid")
                }
            })
    }
}