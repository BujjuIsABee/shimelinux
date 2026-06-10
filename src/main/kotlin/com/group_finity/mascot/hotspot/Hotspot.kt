/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.hotspot

import com.group_finity.mascot.Mascot
import java.awt.Point
import java.awt.Shape

class Hotspot(val behavior: String?, val shape: Shape) {
    fun contains(mascot: Mascot, point: Point) = shape.contains(
        if (mascot.isLookRight) Point(mascot.bounds.width - point.x, point.y) else point
    )
}
