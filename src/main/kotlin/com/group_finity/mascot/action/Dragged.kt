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

package com.group_finity.mascot.action

import com.group_finity.mascot.Mascot
import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.getProperty
import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle
import kotlin.math.abs
import kotlin.math.roundToInt

class Dragged(
    schema: ResourceBundle,
    animations: List<Animation>,
    context: VariableMap
) : ActionBase(schema, animations, context) {
    private var footX = 0.0
    private var footDx = 0.0
    private var scaling = 0.0
    var timeToRegist = 0

    private val offsetX: Int
        get() = eval<Number>(schema.getString(PARAMETER_OFFSETX), DEFAULT_OFFSETX).toInt()
    private val offsetY: Int
        get() = eval<Number>(schema.getString(PARAMETER_OFFSETY), DEFAULT_OFFSETY).toInt()
    private val offsetType: String
        get() = eval(schema.getString(PARAMETER_OFFSETTYPE), DEFAULT_OFFSETTYPE)

    override fun init(mascot: Mascot) {
        super.init(mascot)

        scaling = getProperty<Double>("Scaling", "1.0")

        footX = (environment.cursor.x + (offsetX * scaling).roundToInt()).toDouble()
        timeToRegist = 250
    }

    override fun hasNext() = super.hasNext() && time < timeToRegist

    override fun tick() {
        mascot.isLookRight = false
        mascot.isDragging = true
        environment.refreshWorkArea()

        val cursor = environment.cursor

        var offsetX = (offsetX * scaling).roundToInt()
        var offsetY = (offsetY * scaling).roundToInt()
        if (offsetType == schema.getString("Origin")) {
            mascot.image?.let {
                offsetX = 0 - offsetX + it.center.x
                offsetY = 0 - offsetY + it.center.y
            }
        }

        if (abs(cursor.x - mascot.anchor.x + offsetX) >= 5) {
            time = 0
        }

        val newX = cursor.x

        footDx += ((newX - footX) * 0.1) * 0.8
        footX += footDx

        putVariable(schema.getString(VARIABLE_FOOTDX), footDx)
        putVariable(schema.getString(VARIABLE_FOOTX), footX)

        animation?.next(mascot, time)

        mascot.anchor = Point(
            cursor.x + offsetX,
            cursor.y + offsetY
        )

        if (time == timeToRegist - 1 && Math.random() >= 0.1) timeToRegist++
    }

    override fun refreshHotspots() {
        // Action does not support hotspots
        mascot.hotspots.clear()
    }

    companion object {
        const val PARAMETER_OFFSETX = "OffsetX"
        private const val DEFAULT_OFFSETX = 0

        const val PARAMETER_OFFSETY = "OffsetY"
        private const val DEFAULT_OFFSETY = 120

        const val PARAMETER_OFFSETTYPE = "OffsetType"
        private const val DEFAULT_OFFSETTYPE = "ImageAnchor"

        const val VARIABLE_FOOTX = "FootX"
        const val VARIABLE_FOOTDX = "FootDX"
    }
}
