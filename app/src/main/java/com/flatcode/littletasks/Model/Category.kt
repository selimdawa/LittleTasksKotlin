package com.flatcode.littletasks.Model

class Category {
    var id: String? = null
    var name: String? = null
    var image: String? = null
    var publisher: String? = null
    var plan: String? = null
    var timestamp: Long = 0

    constructor()

    constructor(
        id: String?, name: String?, image: String?, publisher: String?, plan: String?,
        timestamp: Long
    ) {
        this.id = id
        this.name = name
        this.publisher = publisher
        this.image = image
        this.plan = plan
        this.timestamp = timestamp
    }
}