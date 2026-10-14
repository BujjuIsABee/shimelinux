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

/**
 * A movable rectangle.
 *
 * @author Yuki Yamada
 * @author Shimeji-ee Group
 * @author Bujju
 */
class Area {
    /**
     * Whether the interactive window that the area represents is active.
     */
    var isVisible = true

    /**
     * The position of the left edge of the rectangle on the X-axis.
     */
    var left = 0

    /**
     * The position of the top edge of the rectangle on the Y-axis.
     */
    var top = 0

    /**
     * The position of the right edge of the rectangle on the X-axis.
     */
    var right = 0

    /**
     * The position of the bottom edge of the rectangle on the Y-axis.
     */
    var bottom = 0

    /**
     * The distance that the left edge of the rectangle moved the last time [set] was called.
     */
    var dleft = 0

    /**
     * The distance that the top edge of the rectangle moved the last time [set] was called.
     */
    var dtop = 0

    /**
     * The distance that the right edge of the rectangle moved the last time [set] was called.
     */
    var dright = 0

    /**
     * The distance that the bottom edge of the rectangle moved the last time [set] was called.
     */
    var dbottom = 0

    /**
     * The interactable border on the area's left wall.
     */
    val leftBorder = Wall(this, false)

    /**
     * The interactable border on the area's ceiling.
     */
    val topBorder = FloorCeiling(this, false)

    /**
     * The interactable border on the area's right wall.
     */
    val rightBorder = Wall(this, true)

    /**
     * The interactable border on the area's floor.
     */
    val bottomBorder = FloorCeiling(this, true)

    /**
     * The width of the rectangle.
     */
    val width: Int
        get() = right - left

    /**
     * The height of the rectangle.
     */
    val height: Int
        get() = bottom - top

    /**
     * Sets the bounds of the rectangle.
     */
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

    /**
     * Gets whether a point at the given [x] and [y] coordinates is inside the rectangle.
     */
    fun contains(x: Int, y: Int): Boolean = x in left..right && y in top..bottom

    /**
     * Gets the area as a [Rectangle].
     */
    fun toRectangle() = Rectangle(left, top, width, height)

    override fun toString() = "Area[left=$left, top=$top, right=$right, bottom=$bottom]"
}
