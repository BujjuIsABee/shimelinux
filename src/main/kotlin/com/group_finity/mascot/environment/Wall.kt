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
        area.isVisible && x == location.x && top <= location.y && location.y <= bottom

    override fun move(location: Point): Point {
        val d = bottom - dbottom - (top - dtop)
        return if (!area.isVisible || d == 0) {
            location
        } else {
            Point(
                location.x + dx,
                (location.y - (top - dtop)) * (bottom - top) / d + top
            ).takeUnless { abs(it.x - location.x) >= 80 || abs(it.y - location.y) >= 80 } ?: location
        }
    }
}
