package com.flatcode.littletasks.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littletasks.repository.UserRepository
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.Resource
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val auth: FirebaseAuth, private val userRepository: UserRepository
) : ViewModel() {

    private val _profileImage = MutableStateFlow("")
    val profileImage: StateFlow<String> = _profileImage.asStateFlow()

    fun loadUserInfo() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.getUserInfo(uid).collectLatest { resource ->
                if (resource is Resource.Success && resource.data != null) {
                    _profileImage.value = resource.data.profileImage ?: DATA.EMPTY
                }
            }
        }
    }
}