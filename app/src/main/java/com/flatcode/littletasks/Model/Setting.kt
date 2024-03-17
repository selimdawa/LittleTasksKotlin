package com.flatcode.littletasks.Model

class Setting {
    var id: String? = null
    var name: String? = null
    var type: String? = null
    var image = 0
    var number = 0
    var c: Class<*>? = null

    constructor()

    constructor(id: String?, name: String?, image: Int, number: Int, c: Class<*>?) {
        this.id = id
        this.name = name
        this.image = image
        this.number = number
        this.c = c
    }

    constructor(id: String?, name: String?, image: Int, number: Int, type: String?) {
        this.id = id
        this.name = name
        this.image = image
        this.number = number
        this.type = type
    }

    constructor(id: String?, name: String?, image: Int, number: Int) {
        this.id = id
        this.name = name
        this.image = image
        this.number = number
    }
}