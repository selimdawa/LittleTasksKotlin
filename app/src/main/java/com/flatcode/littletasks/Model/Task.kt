package com.flatcode.littletasks.Model

data class Task(
    var name: String? = null,
    var publisher: String? = null,
    var id: String? = null,
    var category: String? = null,
    var timestamp: Long = 0,
    var start: Long = 0,
    var end: Long = 0,
    var points: Int = 0,
    var aVPoints: Int = 0
)