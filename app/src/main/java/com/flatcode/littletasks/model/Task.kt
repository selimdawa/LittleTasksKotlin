package com.flatcode.littletasks.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey var id: String = "",
    var name: String? = null,
    var publisher: String? = null,
    var category: String? = null,
    var timestamp: Long = 0,
    var start: Long = 0,
    var end: Long = 0,
    var points: Int = 0,
    var aVPoints: Int = 0
) : Parcelable
