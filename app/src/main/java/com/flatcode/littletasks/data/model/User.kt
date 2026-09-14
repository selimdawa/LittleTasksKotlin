package com.flatcode.littletasks.data.model

data class User(
    var id: String? = null,
    var username: String? = null,
    var profileImage: String? = null,
    var email: String? = null,
    var timestamp: Long = 0,
    var version: Int = 0
)