/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.hotspot

import com.group_finity.mascot.Mascot
import java.awt.Point
import java.awt.Shape

class Hotspot(val behavior: String?, val shape: Shape) {
    fun contains(mascot: Mascot, point: Point): Boolean = shape.contains(
        if (mascot.isLookRight) Point(mascot.bounds.width - point.x, point.y) else point
    )
}
