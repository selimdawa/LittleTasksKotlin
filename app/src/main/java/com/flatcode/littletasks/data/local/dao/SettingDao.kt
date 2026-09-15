package com.flatcode.littletasks.data.local.dao

import androidx.room.*
import com.flatcode.littletasks.data.model.Setting
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings")
    fun getAllSettings(): Flow<List<Setting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: Setting)

    @Query("DELETE FROM settings")
    suspend fun deleteAllSettings()
}
