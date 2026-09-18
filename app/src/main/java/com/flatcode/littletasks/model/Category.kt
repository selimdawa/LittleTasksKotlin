package com.flatcode.littletasks.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey var id: String = "",
    var name: String? = null,
    var image: String? = null,
    var publisher: String? = null,
    var plan: String? = null,
    var timestamp: Long = 0,
    var taskCount: Int = 0
) : Parcelable