/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.environment

import java.awt.Point
import kotlin.math.abs

class Wall(val area: Area, val isRight: Boolean) : Border {
    val x: Int get() = if (isRight) area.right else area.left
    val top: Int get() = area.top
    val bottom: Int get() = area.bottom
    val dx: Int
        @JvmName("getDX")
        get() = if (isRight) area.dright else area.dleft
    val dtop: Int
        @JvmName("getDTop")
        get() = area.dtop
    val dbottom: Int
        @JvmName("getDBottom")
        get() = area.dbottom
    val height: Int get() = area.height

    override fun isOn(location: Point): Boolean =
        area.isVisible && (x == location.x) && (top <= location.y) && (location.y <= bottom)

    override fun move(location: Point): Point {
        if (!area.isVisible) return location

        val d = bottom - dbottom - (top - dtop)
        if (d == 0) return location
        
        val newLocation = Point(location.x + dx, (location.y - (top - dtop)) * (bottom - top) / d + top)
        if ((abs(newLocation.x - location.x) >= 80) || (abs(newLocation.y - location.y) >= 80)) return location

        return newLocation
    }
}
