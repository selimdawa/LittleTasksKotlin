package com.flatcode.littletasks.Model

class Plan {
    var id: String? = null
    var name: String? = null
    var image: String? = null
    var publisher: String? = null
    var timestamp: Long = 0

    constructor()

    constructor(id: String?, name: String?, image: String?, publisher: String?, timestamp: Long) {
        this.id = id
        this.name = name
        this.publisher = publisher
        this.image = image
        this.timestamp = timestamp
    }
}