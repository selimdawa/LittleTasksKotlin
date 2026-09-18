package com.flatcode.littletasks.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "task_items")
data class TaskItem(
    @PrimaryKey var id: String = "",
    var name: String? = null,
    var publisher: String? = null,
    var points: Int = 0,
    var timestamp: Long = 0
) : Parcelable
