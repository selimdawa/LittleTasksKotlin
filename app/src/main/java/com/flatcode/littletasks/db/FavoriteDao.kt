package com.flatcode.littletasks.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.flatcode.littletasks.model.FavoriteEntity
import com.flatcode.littletasks.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT tasks.* FROM tasks INNER JOIN favorites ON tasks.id = favorites.taskId WHERE favorites.userId = :userId ORDER BY tasks.timestamp DESC")
    fun getFavoriteTasks(userId: String): Flow<List<Task>>

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId")
    fun getFavoriteCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorites(favorites: List<FavoriteEntity>)

    @Query("DELETE FROM favorites WHERE userId = :userId AND taskId = :taskId")
    suspend fun deleteFavorite(userId: String, taskId: String)

    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun deleteAllFavoritesForUser(userId: String)
}