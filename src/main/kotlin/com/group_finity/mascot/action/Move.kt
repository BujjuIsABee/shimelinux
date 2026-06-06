/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.exception.LostGroundException
import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

open class Move(
    schema: ResourceBundle,
    animations: ArrayList<Animation>,
    context: VariableMap,
) : BorderedAction(schema, animations, context) {
    internal var isTurning = false
    internal val hasTurningAnimation: Boolean by lazy {
        var result = false
        for (animation in super.animations) {
            if (animation.isTurn) {
                result = true
                break
            }
        }
        return@lazy result
    }

    private val targetX: Int
        get() = eval(schema.getString(PARAMETER_TARGETX), Number::class, DEFAULT_TARGETX).toInt()
    private val targetY: Int
        get() = eval(schema.getString(PARAMETER_TARGETY), Number::class, DEFAULT_TARGETY).toInt()

    override val animation: Animation?
        get() {
            for (animation in super.animations) {
                if (animation.isEffective(variables) && isTurning == animation.isTurn) {
                    return animation
                }
            }
            return null
        }

    override fun hasNext(): Boolean {
        val hasNotReached =
            (targetX != Int.MIN_VALUE && mascot.anchor.x == targetX) ||
            (targetY != Int.MIN_VALUE && mascot.anchor.y == targetY)
        return super.hasNext() && (!hasNotReached || isTurning)
    }

    override fun tick() {
        super.tick()

        if (border != null && !border!!.isOn(mascot.anchor)) {
            log.log(Level.INFO, "Lost ground ($mascot,$this)")
            throw LostGroundException()
        }

        var isDown = false
        if (targetX != DEFAULT_TARGETX && mascot.anchor.x != targetX) {
            isTurning = hasTurningAnimation && (isTurning || mascot.anchor.x < targetX != mascot.isLookRight)
            mascot.isLookRight = mascot.anchor.x < targetX
        }
        if (targetY != DEFAULT_TARGETY) {
            isDown = mascot.anchor.y < targetY
        }

        if (isTurning && time >= checkNotNull(animation).duration) {
            isTurning = false
        }

        checkNotNull(animation).next(mascot, time)

        if (targetX != DEFAULT_TARGETX) {
            if ((mascot.isLookRight && (mascot.anchor.x >= targetX)) ||
                (!mascot.isLookRight && (mascot.anchor.x <= targetX))
            ) {
                mascot.anchor = Point(targetX, mascot.anchor.y)
            }
        }
        if (targetY != DEFAULT_TARGETY) {
            if ((isDown && (mascot.anchor.y >= targetY)) ||
                (!isDown && (mascot.anchor.y <= targetY))
            ) {
                mascot.anchor = Point(mascot.anchor.x, targetY)
            }
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        const val PARAMETER_TARGETX = "TargetX"
        private const val DEFAULT_TARGETX = Int.MAX_VALUE

        const val PARAMETER_TARGETY = "TargetY"
        private const val DEFAULT_TARGETY = Int.MAX_VALUE
    }
}
