package com.flatcode.littletasks.data.local.dao

import androidx.room.*
import com.flatcode.littletasks.data.model.Plan
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans ORDER BY timestamp DESC")
    fun getAllPlans(): Flow<List<Plan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<Plan>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: Plan)

    @Delete
    suspend fun deletePlan(plan: Plan)

    @Query("DELETE FROM plans")
    suspend fun deleteAllPlans()
}
