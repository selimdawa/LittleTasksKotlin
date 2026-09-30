package com.flatcode.littletasks.repository

import com.flatcode.littletasks.db.PlanDao
import com.flatcode.littletasks.model.Plan
import com.flatcode.littletasks.utils.DATA
import com.flatcode.littletasks.utils.Resource
import com.google.firebase.auth.FirebaseAuth
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
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface PlanRepository {
    fun getPlans(): Flow<Resource<List<Plan>>>
    suspend fun getPlanById(id: String): Plan?
    suspend fun deletePlan(databaseName: String, id: String): Result<Unit>
}

@Singleton
class PlanRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase,
    private val planDao: PlanDao
) : PlanRepository {

    override fun getPlans(): Flow<Resource<List<Plan>>> = channelFlow {
        val localJob = launch {
            planDao.getAllPlans().collectLatest { localList ->
                send(Resource.Success(localList))
            }
        }

        val uid = auth.currentUser?.uid
        if (uid == null) {
            localJob.join()
            return@channelFlow
        }

        val plansRef = database.getReference(DATA.PLANS).orderByChild("publisher").equalTo(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val remoteList = mutableListOf<Plan>()
                for (data in snapshot.children) {
                    val item = data.getValue(Plan::class.java) ?: continue
                    remoteList.add(item)
                }
                CoroutineScope(Dispatchers.IO).launch {
                    planDao.insertPlans(remoteList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Keep showing local cached data
            }
        }

        plansRef.addValueEventListener(listener)

        awaitClose {
            plansRef.removeEventListener(listener)
            localJob.cancel()
        }
    }

    override suspend fun getPlanById(id: String): Plan? {
        return planDao.getPlanById(id)
    }

    override suspend fun deletePlan(databaseName: String, id: String): Result<Unit> {
        return try {
            database.getReference(databaseName).child(id).removeValue().await()
            planDao.deletePlanById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            planDao.deletePlanById(id)
            Result.failure(e)
        }
    }
}
