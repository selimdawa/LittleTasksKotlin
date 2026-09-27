package com.flatcode.littletasks.repository

import com.flatcode.littletasks.db.UserDao
import com.flatcode.littletasks.model.User
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.Resource
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

interface UserRepository {
    fun getUserInfo(userId: String): Flow<Resource<User>>
}

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val database: FirebaseDatabase,
    private val userDao: UserDao
) : UserRepository {

    override fun getUserInfo(userId: String): Flow<Resource<User>> = channelFlow {
        val localJob = launch {
            userDao.getUserById(userId).collectLatest { user ->
                if (user != null) {
                    send(Resource.Success(user))
                }
            }
        }

        if (userId.isEmpty()) {
            localJob.join()
            return@channelFlow
        }

        val userRef = database.getReference(DATA.USERS).child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java) ?: return
                CoroutineScope(Dispatchers.IO).launch {
                    userDao.insertUser(user)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Keep showing cached user data
            }
        }

        userRef.addValueEventListener(listener)

        awaitClose {
            userRef.removeEventListener(listener)
            localJob.cancel()
        }
    }
}
