package com.flatcode.littletasks.data.model

data class Plan(
    var id: String? = null,
    var name: String? = null,
    var image: String? = null,
    var publisher: String? = null,
    var timestamp: Long = 0
)