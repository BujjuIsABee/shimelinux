/*
 * Copyright (c) 2026, Bujju
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *        following disclaimer.
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *        following disclaimer in the documentation and/or other materials provided with the distribution.
 *     3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
 *        products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.group_finity.mascot.environment

import java.awt.Point
import kotlin.math.abs

/**
 * A vertical border.
 *
 * @author Yuki Yamada
 * @author Shimeji-ee Group
 * @author Bujju
 */
class FloorCeiling(val area: Area, val isBottom: Boolean) : Border {
    /**
     * The position of the border on the Y-axis.
     */
    val y: Int
        get() = if (isBottom) area.bottom else area.top

    /**
     * The position of the left edge of the border on the X-axis.
     */
    val left: Int
        get() = area.left

    /**
     * The position of the right edge of the border on the X-axis.
     */
    val right: Int
        get() = area.right

    /**
     * The distance that the [y] position of the border moved the last time [Area.set] was called on the [area].
     */
    val dy: Int
        @JvmName("getDY")
        get() = if (isBottom) area.dbottom else area.dtop

    /**
     * The distance that the left edge of the border moved the last time [Area.set] was called on the [area].
     */
    val dleft: Int
        @JvmName("getDLeft")
        get() = area.dleft

    /**
     * The distance that the right edge of the border moved the last time [Area.set] was called on the [area].
     */
    val dright: Int
        @JvmName("getDRight")
        get() = area.dright

    /**
     * The width of the border.
     */
    val width: Int
        get() = area.width

    override fun isOn(location: Point): Boolean = area.isVisible && y == location.y && location.x in left..right

    override fun move(location: Point): Point {
        if (!area.isVisible) {
            return location
        }

        val d = right - dright - (left - dleft)
        if (d == 0) {
            return location
        }

        val newLocation = Point(
            (location.x - (left - dleft)) * (right - left) / d + left,
            location.y + dy
        )

        if (abs(newLocation.x - location.x) >= 80 || dy !in -80..20) {
            return location
        }

        return newLocation
    }
}
