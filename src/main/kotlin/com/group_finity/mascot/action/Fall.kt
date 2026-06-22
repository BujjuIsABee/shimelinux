/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
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
