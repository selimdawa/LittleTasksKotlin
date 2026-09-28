package com.flatcode.littletasks.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flatcode.littletasks.db.CategoryDao
import com.flatcode.littletasks.db.FavoriteDao
import com.flatcode.littletasks.db.PlanDao
import com.flatcode.littletasks.db.TaskDao
import com.flatcode.littletasks.db.TaskItemDao
import com.flatcode.littletasks.model.User
import com.flatcode.littletasks.repository.UserRepository
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
class SettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val userRepository: UserRepository,
    private val categoryDao: CategoryDao,
    private val planDao: PlanDao,
    private val taskItemDao: TaskItemDao,
    private val taskDao: TaskDao,
    private val favoriteDao: FavoriteDao
) : ViewModel() {

    private val _userInfo = MutableStateFlow<User?>(null)
    val userInfo: StateFlow<User?> = _userInfo.asStateFlow()

    private val _pointsSummary = MutableStateFlow(Triple(0, 0, 0))
    val pointsSummary: StateFlow<Triple<Int, Int, Int>> = _pointsSummary.asStateFlow()

    private val _itemCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val itemCounts: StateFlow<Map<String, Int>> = _itemCounts.asStateFlow()

    private val _privacyPolicy = MutableStateFlow("")
    val privacyPolicy: StateFlow<String> = _privacyPolicy.asStateFlow()

    fun loadUserInfo() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.getUserInfo(uid).collectLatest { resource ->
                if (resource is Resource.Success) {
                    _userInfo.value = resource.data
                }
            }
        }
    }

    fun loadPoints() {
        viewModelScope.launch {
            taskDao.getAllTasks().collectLatest { taskList ->
                var total = 0
                var av = 0
                for (task in taskList) {
                    total += task.points
                    av += task.aVPoints
                }
                val level = levelPoint(av)
                _pointsSummary.value = Triple(total, av, level)
            }
        }
    }

    fun loadItemCounts() {
        val uid = auth.currentUser?.uid ?: return
        val counts = mutableMapOf<String, Int>()
        viewModelScope.launch {
            categoryDao.getCategoriesCount().collectLatest { count ->
                counts[DATA.CATEGORIES] = count
                _itemCounts.value = counts.toMap()
            }
        }
        viewModelScope.launch {
            planDao.getPlansCount().collectLatest { count ->
                counts[DATA.PLANS] = count
                _itemCounts.value = counts.toMap()
            }
        }
        viewModelScope.launch {
            taskItemDao.getTaskItemsCount().collectLatest { count ->
                counts[DATA.OBJECTS] = count
                _itemCounts.value = counts.toMap()
            }
        }
        viewModelScope.launch {
            favoriteDao.getFavoriteCount(uid).collectLatest { count ->
                counts[DATA.FAVORITES] = count
                _itemCounts.value = counts.toMap()
            }
        }
    }

    fun loadPrivacyPolicy() {
        database.getReference(DATA.TOOLS).child(DATA.PRIVACY_POLICY)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _privacyPolicy.value = snapshot.value?.toString().orEmpty()
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error loading privacy policy")
                }
            })
    }

    private fun levelPoint(avPoints: Int): Int {
        val initialPoint = 10
        var mutablePoint = initialPoint
        val half = mutablePoint / 2
        val thresholds = IntArray(21)
        thresholds[1] = mutablePoint * 5
        for (i in 2..20) {
            thresholds[i] = thresholds[i - 1] + half * (i + 1) * half
        }

        return when {
            avPoints <= thresholds[1] -> avPoints / mutablePoint
            avPoints <= thresholds[20] -> {
                var stepIndex = 1
                while (stepIndex < 19 && avPoints > thresholds[stepIndex + 1]) {
                    stepIndex++
                }
                val baseLevel = 5 * stepIndex
                val remainderPoints = avPoints - thresholds[stepIndex]
                mutablePoint += half * (stepIndex - 1)
                baseLevel + (remainderPoints / mutablePoint)
            }

            else -> 100
        }
    }
}