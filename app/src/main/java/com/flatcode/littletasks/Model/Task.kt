package com.flatcode.littletasks.Model

class Task {
    var name: String? = null
    var publisher: String? = null
    var id: String? = null
    var category: String? = null
    var timestamp: Long = 0
    var start: Long = 0
    var end: Long = 0
    var points = 0
    var aVPoints = 0

    constructor()

    constructor(
        name: String?, publisher: String?, id: String?, category: String?,
        timestamp: Long, start: Long, end: Long, points: Int, AVPoints: Int
    ) {
        this.name = name
        this.publisher = publisher
        this.id = id
        this.category = category
        this.timestamp = timestamp
        this.start = start
        this.end = end
        this.points = points
        aVPoints = AVPoints
    }
}