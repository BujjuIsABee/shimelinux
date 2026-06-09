/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
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
    animations: ArrayList<Animation>,
    context: VariableMap,
) : ActionBase(schema, animations, context) {
    private val velocity: Double
        get() = eval(schema.getString(PARAMETER_VELOCITY), Number::class, DEFAULT_VELOCITY).toDouble()
    private val targetX: Int
        get() = eval(schema.getString(PARAMETER_TARGETX), Number::class, DEFAULT_TARGETX).toInt()
    private val targetY: Int
        get() = eval(schema.getString(PARAMETER_TARGETY), Number::class, DEFAULT_TARGETY).toInt()

    override fun hasNext(): Boolean {
        val distanceX = (targetX - mascot.anchor.x).toDouble()
        val distanceY = (targetY - mascot.anchor.y).toDouble() - abs(distanceX) / 2
        val distance = sqrt(distanceX * distanceX + distanceY * distanceY)
        return super.hasNext() && distance != 0.0
    }

    override fun tick() {
        mascot.isLookRight = mascot.anchor.x < targetX

        val distanceX = (targetX - mascot.anchor.x).toDouble()
        val distanceY = (targetY - mascot.anchor.y).toDouble() - abs(distanceX) / 2
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
