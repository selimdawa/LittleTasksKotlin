package com.flatcode.littletasks.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "settings")
data class Setting(
    @PrimaryKey
    var id: String = "",
    var name: String? = null,
    var type: String? = null,
    var image: Int = 0,
    var number: Int = 0,
    @Ignore
    var c: Class<*>? = null
) : Parcelable {
    // Required empty constructor for Room when using @Ignore on a field
    constructor() : this("", null, null, 0, 0, null)
}
