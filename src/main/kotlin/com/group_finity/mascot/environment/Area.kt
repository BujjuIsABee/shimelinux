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

import java.awt.Rectangle

class Area {
    var isVisible = true
    var left = 0
    var top = 0
    var right = 0
    var bottom = 0
    var dleft = 0
    var dtop = 0
    var dright = 0
    var dbottom = 0

    val leftBorder = Wall(this, false)
    val topBorder = FloorCeiling(this, false)
    val rightBorder = Wall(this, true)
    val bottomBorder = FloorCeiling(this, true)

    val width
        get() = right - left
    val height
        get() = bottom - top

    fun set(value: Rectangle) {
        dleft = value.x - left
        dtop = value.y - top
        dright = value.x + value.width - right
        dbottom = value.y + value.height - bottom

        left = value.x
        top = value.y
        right = value.x + value.width
        bottom = value.y + value.height
    }

    fun contains(x: Int, y: Int) = (left <= x) && (x <= right) && (top <= y) && (y <= bottom)

    fun toRectangle() = Rectangle(left, top, width, height)

    override fun toString() = "Area ($left, $top, $right, $bottom)"
}
