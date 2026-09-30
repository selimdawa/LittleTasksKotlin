package com.flatcode.littletasks.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flatcode.littletasks.model.Plan
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {
    @Query("SELECT * FROM plans ORDER BY timestamp DESC")
    fun getAllPlans(): Flow<List<Plan>>

    @Query("SELECT COUNT(*) FROM plans")
    fun getPlansCount(): Flow<Int>

    @Query("SELECT * FROM plans WHERE id = :id")
    suspend fun getPlanById(id: String): Plan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<Plan>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: Plan)

    @Delete
    suspend fun deletePlan(plan: Plan)

    @Query("DELETE FROM plans WHERE id = :id")
    suspend fun deletePlanById(id: String)

    @Query("DELETE FROM plans")
    suspend fun deleteAllPlans()
}