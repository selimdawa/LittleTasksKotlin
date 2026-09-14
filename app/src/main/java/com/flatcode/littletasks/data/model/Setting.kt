package com.flatcode.littletasks.data.model

data class Setting(
    var id: String? = null,
    var name: String? = null,
    var type: String? = null,
    var image: Int = 0,
    var number: Int = 0,
    var c: Class<*>? = null
)