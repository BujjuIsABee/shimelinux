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
import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle
import kotlin.math.abs

open class Fall(
    schema: ResourceBundle,
    animations: List<Animation>,
    context: VariableMap
) : ActionBase(schema, animations, context) {
    private var velocityX = 0.0
    private var velocityY = 0.0
    private var modX = 0.0
    private var modY = 0.0

    private val initialVx
        get() = eval(schema.getString(PARAMETER_INITIALVX), Number::class, DEFAULT_INITIALVX).toInt()
    private val initialVy
        get() = eval(schema.getString(PARAMETER_INITIALVY), Number::class, DEFAULT_INITIALVY).toInt()
    private val gravity
        get() = eval(schema.getString(PARAMETER_GRAVITY), Number::class, DEFAULT_GRAVITY).toDouble()
    private val resistanceX
        get() = eval(schema.getString(PARAMETER_RESISTANCEX), Number::class, DEFAULT_RESISTANCEX).toDouble()
    private val resistanceY
        get() = eval(schema.getString(PARAMETER_RESISTANCEY), Number::class, DEFAULT_RESISTANCEY).toDouble()

    override fun init(mascot: Mascot) {
        super.init(mascot)

        velocityX = initialVx.toDouble()
        velocityY = initialVy.toDouble()
    }

    override fun hasNext(): Boolean {
        val isOnBorder = environment.floor.isOn(mascot.anchor) ||
            environment.wall.isOn(mascot.anchor)

        return super.hasNext() && !isOnBorder
    }

    override fun tick() {
        if (velocityX != 0.0) {
            mascot.isLookRight = velocityX > 0.0
        }

        velocityX -= velocityX * resistanceX
        velocityY -= (velocityY * resistanceY) - gravity

        putVariable(schema.getString(VARIABLE_VELOCITYX), velocityX)
        putVariable(schema.getString(VARIABLE_VELOCITYY), velocityY)

        modX += (velocityX % 1.0)
        modY += (velocityY % 1.0)

        val dx = velocityX.toInt() + modX.toInt()
        val dy = velocityY.toInt() + modY.toInt()

        modX %= 1.0
        modY %= 1.0

        val dev = 1.coerceAtLeast(abs(dx).coerceAtLeast(abs(dy)))

        val start = mascot.anchor

        outer@ for (i in 0 until dev) {
            val x = start.x + dx * i / dev
            val y = start.y + dy * i / dev

            mascot.anchor = Point(x, y)
            if (dy > 0) {
                for (j in -80 until 0) {
                    mascot.anchor = Point(x, y + j)
                    if (environment.getFloor(true).isOn(mascot.anchor)) break@outer
                }
            }
            if (environment.getWall(true).isOn(mascot.anchor)) break
        }

        animation?.next(mascot, time)
    }

    companion object {
        const val PARAMETER_INITIALVX = "InitialVX"
        private const val DEFAULT_INITIALVX = 0

        const val PARAMETER_INITIALVY = "InitialVY"
        private const val DEFAULT_INITIALVY = 0

        const val PARAMETER_RESISTANCEX = "ResistanceX"
        private const val DEFAULT_RESISTANCEX = 0.05

        const val PARAMETER_RESISTANCEY = "ResistanceY"
        private const val DEFAULT_RESISTANCEY = 0.1

        const val PARAMETER_GRAVITY = "Gravity"
        private const val DEFAULT_GRAVITY = 2

        const val VARIABLE_VELOCITYX = "VelocityX"
        const val VARIABLE_VELOCITYY = "VelocityY"
    }
}
