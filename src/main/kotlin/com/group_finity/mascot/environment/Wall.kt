/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.environment

import java.awt.Point
import kotlin.math.abs

class Wall(val area: Area, val isRight: Boolean) : Border {
    val x
        get() = if (isRight) area.right else area.left
    val top
        get() = area.top
    val bottom
        get() = area.bottom
    val dx
        @JvmName("getDX")
        get() = if (isRight) area.dright else area.dleft
    val dtop
        @JvmName("getDTop")
        get() = area.dtop
    val dbottom
        @JvmName("getDBottom")
        get() = area.dbottom
    val height
        get() = area.height

    override fun isOn(location: Point) =
        area.isVisible && (x == location.x) && (top <= location.y) && (location.y <= bottom)

    override fun move(location: Point): Point {
        if (!area.isVisible) return location

        val d = bottom - dbottom - (top - dtop)
        if (d == 0) return location

        val newLocation = Point(location.x + dx, (location.y - (top - dtop)) * (bottom - top) / d + top)
        if (abs(newLocation.x - location.x) >= 80 || abs(newLocation.y - location.y) >= 80) return location
        return newLocation
    }
}
