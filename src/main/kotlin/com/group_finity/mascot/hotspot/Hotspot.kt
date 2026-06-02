package com.group_finity.mascot.hotspot

import com.group_finity.mascot.Mascot
import java.awt.Point
import java.awt.Shape

class Hotspot(val behavior: String?, val shape: Shape) {
    fun contains(mascot: Mascot, point: Point): Boolean {
        var p = point
        if (mascot.isLookRight) {
            p = Point(mascot.bounds.width - point.x, point.y)
        }

        return shape.contains(p)
    }
}