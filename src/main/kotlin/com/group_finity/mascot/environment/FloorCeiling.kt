/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.environment

import java.awt.Point
import kotlin.math.abs

class FloorCeiling(val area: Area, val isBottom: Boolean) : Border {
    val y: Int get() = if (isBottom) area.bottom else area.top
    val left: Int get() = area.left
    val right: Int get() = area.right
    val dy: Int
        @JvmName("getDY")
        get() = if (isBottom) area.dbottom else area.dtop
    val dleft: Int
        @JvmName("getDLeft")
        get() = area.dleft
    val dright: Int
        @JvmName("getDRight")
        get() = area.dright
    val width: Int get() = area.width

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
