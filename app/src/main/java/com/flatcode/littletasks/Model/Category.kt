package com.flatcode.littletasks.Model

data class Category(
    var id: String? = null,
    var name: String? = null,
    var image: String? = null,
    var publisher: String? = null,
    var plan: String? = null,
    var timestamp: Long = 0
)