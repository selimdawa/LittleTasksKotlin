package com.flatcode.littletasks.model

import androidx.room.Entity

@Entity(tableName = "favorites", primaryKeys = ["userId", "taskId"])
data class FavoriteEntity(
    val userId: String = "", val taskId: String = ""
)