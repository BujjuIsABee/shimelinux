/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.environment

import java.awt.Point
import kotlin.math.abs

class FloorCeiling(val area: Area, val isBottom: Boolean) : Border {
    val y: Int
        get() = if (isBottom) area.bottom else area.top
    val left: Int
        get() = area.left
    val right: Int
        get() = area.right
    @get:JvmName("getDY")
    val dy: Int
        get() = if (isBottom) area.dbottom else area.dtop
    @get:JvmName("getDLeft")
    val dleft: Int
        get() = area.dleft
    @get:JvmName("getDRight")
    val dright: Int
        get() = area.dright
    val width: Int
        get() = area.width

    override fun isOn(location: Point): Boolean =
        area.isVisible && (y == location.y) && (left <= location.x) && (location.x <= right)

    override fun move(location: Point): Point {
        if (!area.isVisible) return location

        val d = right - dright - (left - dleft)
        if (d == 0) return location

        val newLocation = Point((location.x - (left - dleft)) * ((right - left) / d) + left, location.y + dy)
        if ((abs(newLocation.x - location.x) >= 80) ||
            (newLocation.y - location.y > 20) ||
            (newLocation.y - location.y < -80)
        ) {
            return location
        }

        return newLocation
    }
}
