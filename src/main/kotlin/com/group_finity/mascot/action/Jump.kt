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

import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle
import kotlin.math.abs
import kotlin.math.sqrt

open class Jump(
    schema: ResourceBundle,
    animations: List<Animation>,
    context: VariableMap
) : ActionBase(schema, animations, context) {
    private val velocity: Double
        get() = eval<Number>(schema.getString(PARAMETER_VELOCITY), DEFAULT_VELOCITY).toDouble()
    private val targetX: Int
        get() = eval<Number>(schema.getString(PARAMETER_TARGETX), DEFAULT_TARGETX).toInt()
    private val targetY: Int
        get() = eval<Number>(schema.getString(PARAMETER_TARGETY), DEFAULT_TARGETY).toInt()

    override fun hasNext(): Boolean {
        val distanceX = (targetX - mascot.anchor.x).toDouble()
        val distanceY = (targetY - mascot.anchor.y).toDouble() - abs(distanceX) / 2.0
        val distance = sqrt(distanceX * distanceX + distanceY * distanceY)

        return super.hasNext() && distance != 0.0
    }

    override fun tick() {
        mascot.isLookRight = mascot.anchor.x < targetX

        val distanceX = (targetX - mascot.anchor.x).toDouble()
        val distanceY = (targetY - mascot.anchor.y).toDouble() - abs(distanceX) / 2.0
        val distance = sqrt(distanceX * distanceX + distanceY * distanceY)

        if (distance != 0.0) {
            val velocityX = (velocity * distanceX / distance).toInt()
            val velocityY = (velocity * distanceY / distance).toInt()

            putVariable(schema.getString(VARIABLE_VELOCITYX), velocity * distanceX / distance)
            putVariable(schema.getString(VARIABLE_VELOCITYY), velocity * distanceY / distance)

            mascot.anchor = Point(
                mascot.anchor.x + velocityX,
                mascot.anchor.y + velocityY
            )

            animation?.next(mascot, time)
        }

        if (distance <= velocity) {
            mascot.anchor = Point(targetX, targetY)
        }
    }

    companion object {
        const val PARAMETER_TARGETX = "TargetX"
        private const val DEFAULT_TARGETX = 0

        const val PARAMETER_TARGETY = "TargetY"
        private const val DEFAULT_TARGETY = 0

        const val PARAMETER_VELOCITY = "VelocityParam"
        private const val DEFAULT_VELOCITY = 20.0

        const val VARIABLE_VELOCITYX = "VelocityX"
        const val VARIABLE_VELOCITYY = "VelocityY"
    }
}
