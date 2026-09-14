package com.flatcode.littletasks.ui.settings

import androidx.lifecycle.ViewModel
import com.flatcode.littletasks.core.utils.DATA
import com.flatcode.littletasks.data.model.Task
import com.flatcode.littletasks.data.model.User
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
class SettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
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
        database.getReference(DATA.USERS).child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java) ?: return
                    _userInfo.value = user
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error loading user info for settings")
                }
            })
    }

    fun loadPoints() {
        database.getReference(DATA.TASKS)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var total = 0
                    var av = 0
                    for (data in snapshot.children) {
                        val task = data.getValue(Task::class.java) ?: continue
                        total += task.points
                        av += task.aVPoints
                    }
                    val level = levelPoint(av, 10)
                    _pointsSummary.value = Triple(total, av, level)
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error loading points summary")
                }
            })
    }

    fun loadItemCounts() {
        val uid = auth.currentUser?.uid ?: return
        val counts = mutableMapOf<String, Int>()

        val refs = listOf(DATA.CATEGORIES, DATA.PLANS, DATA.OBJECTS)
        refs.forEach { ref ->
            database.getReference(ref).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val count = snapshot.children.count {
                        it.child("publisher").getValue(String::class.java) == uid
                    }
                    counts[ref] = count
                    if (counts.size == 4) _itemCounts.value = counts
                }

                override fun onCancelled(error: DatabaseError) {
                    Timber.e(error.toException(), "Error counting items in $ref")
                }
            })
        }

        database.getReference(DATA.FAVORITES).child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                counts[DATA.FAVORITES] = snapshot.childrenCount.toInt()
                if (counts.size == 4) _itemCounts.value = counts
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "Error counting favorites")
            }
        })
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

    private fun levelPoint(AVPoints: Int, initialPoint: Int): Int {
        var mutablePoint = initialPoint
        val half = mutablePoint / 2
        val thresholds = IntArray(21)
        thresholds[1] = mutablePoint * 5
        for (i in 2..20) {
            thresholds[i] = thresholds[i - 1] + half * (i + 1) * half
        }

        return when {
            AVPoints <= thresholds[1] -> AVPoints / mutablePoint
            AVPoints <= thresholds[20] -> {
                var stepIndex = 1
                while (stepIndex < 19 && AVPoints > thresholds[stepIndex + 1]) {
                    stepIndex++
                }
                val baseLevel = 5 * stepIndex
                val remainderPoints = AVPoints - thresholds[stepIndex]
                mutablePoint += half * (stepIndex - 1)
                baseLevel + (remainderPoints / mutablePoint)
            }
            else -> 100
        }
    }
}
