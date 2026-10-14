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
 * A horizontal border.
 *
 * @author Yuki Yamada
 * @author Shimeji-ee Group
 * @author Bujju
 */
class Wall(val area: Area, val isRight: Boolean) : Border {
    /**
     * The position of the border on the X-axis.
     */
    val x: Int
        get() = if (isRight) area.right else area.left

    /**
     * The position of the top edge of the border on the Y-axis.
     */
    val top: Int
        get() = area.top

    /**
     * The position of the bottom edge of the border on the Y-axis.
     */
    val bottom: Int
        get() = area.bottom

    /**
     * The distance that the [x] position of the border moved the last time [Area.set] was called on the [area].
     */
    val dx: Int
        @JvmName("getDX")
        get() = if (isRight) area.dright else area.dleft

    /**
     * The distance that the top edge of the border moved the last time [Area.set] was called on the [area].
     */
    val dtop: Int
        @JvmName("getDTop")
        get() = area.dtop

    /**
     * The distance that the bottom edge of the border moved the last time [Area.set] was called on the [area].
     */
    val dbottom: Int
        @JvmName("getDBottom")
        get() = area.dbottom

    /**
     * The height of the border.
     */
    val height: Int
        get() = area.height

    override fun isOn(location: Point): Boolean = area.isVisible && x == location.x && location.y in top..bottom

    override fun move(location: Point): Point {
        if (!area.isVisible) {
            return location
        }

        val d = bottom - dbottom - (top - dtop)
        if (d == 0) {
            return location
        }

        val newLocation = Point(
            location.x + dx,
            (location.y - (top - dtop)) * (bottom - top) / d + top
        )

        if (abs(dx) >= 80 || abs(newLocation.y - location.y) >= 80) {
            return location
        }

        return newLocation
    }
}
