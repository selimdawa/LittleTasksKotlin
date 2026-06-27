package com.flatcode.littletasks.Model

data class TaskItem(
    var id: String? = null,
    var name: String? = null,
    var publisher: String? = null,
    var points: Int = 0,
    var timestamp: Long = 0
)