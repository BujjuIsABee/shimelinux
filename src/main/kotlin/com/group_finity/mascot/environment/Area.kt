package com.group_finity.mascot.environment

import java.awt.Rectangle

class Area {
    var isVisible = true
    var left = 0
    var top = 0
    var right = 0
    var bottom = 0
    var dleft = 0
    var dtop = 0
    var dright = 0
    var dbottom = 0

    val leftBorder = Wall(this, false)
    val topBorder = FloorCeiling(this, false)
    val rightBorder = Wall(this, true)
    val bottomBorder = FloorCeiling(this, true)

    val width: Int
        get() = right - left
    val height: Int
        get() = bottom - top

    fun set(value: Rectangle) {
        dleft = value.x - left
        dtop = value.y - top
        dright = value.x + value.width - right
        dbottom = value.y + value.height - bottom

        left = value.x
        top = value.y
        right = value.x + value.width
        bottom = value.y + value.height
    }

    fun contains(x: Int, y: Int): Boolean {
        return (left <= x) && (x <= right) && (top <= y) && (y <= bottom)
    }

    fun toRectangle(): Rectangle {
        return Rectangle(left, top, width, height)
    }

    override fun toString(): String {
        return "Area (left=$left,top=$top,right=$right,bottom=$bottom)"
    }
}