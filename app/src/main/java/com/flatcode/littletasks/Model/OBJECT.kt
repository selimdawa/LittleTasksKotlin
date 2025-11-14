package com.flatcode.littletasks.Model

class OBJECT {
    var id: String? = null
    var name: String? = null
    var publisher: String? = null
    var points = 0
    var timestamp: Long = 0

    constructor()

    constructor(id: String?, name: String?, points: Int, publisher: String?, timestamp: Long) {
        this.id = id
        this.name = name
        this.publisher = publisher
        this.points = points
        this.timestamp = timestamp
    }
}