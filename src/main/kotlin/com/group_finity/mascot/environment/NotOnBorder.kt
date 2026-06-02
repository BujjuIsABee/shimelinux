package com.group_finity.mascot.environment

import java.awt.Point

class NotOnBorder : Border {
    private constructor()

    override fun isOn(location: Point): Boolean {
        return false
    }

    override fun move(location: Point): Point {
        return location
    }

    companion object {
        val INSTANCE = NotOnBorder()
    }
}