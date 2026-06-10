/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.Main
import com.group_finity.mascot.Mascot
import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.exception.BehaviorInstantiationException
import com.group_finity.mascot.exception.CantBeAliveException
import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.abs
import kotlin.math.sqrt

@Suppress("UNUSED")
class ScanJump(
    schema: ResourceBundle,
    animations: List<Animation>,
    params: VariableMap,
) : ActionBase(schema, animations, params) {
    private var target: Mascot? = null

    private val behavior
        get() = eval(schema.getString(PARAMETER_BEHAVIOUR), String::class, DEFAULT_BEHAVIOUR)
    private val targetBehavior
        get() = eval(schema.getString(PARAMETER_TARGETBEHAVIOUR), String::class, DEFAULT_TARGETBEHAVIOUR)
    private val targetLook
        get() = eval(schema.getString(PARAMETER_TARGETLOOK), Boolean::class, DEFAULT_TARGETLOOK)
    private val velocity
        get() = eval(schema.getString(PARAMETER_VELOCITY), Number::class, DEFAULT_VELOCITY).toDouble()

    override fun init(mascot: Mascot) {
        super.init(mascot)

        // Cannot broadcast while scanning for an affordance
        mascot.affordances.clear()

        if (mascot.manager != null) {
            target = mascot.manager!!.getMascotWithAffordance(affordance)?.get()
        }

        putVariable(schema.getString(VARIABLE_TARGETX), target?.anchor?.x)
        putVariable(schema.getString(VARIABLE_TARGETY), target?.anchor?.y)
    }

    override fun hasNext(): Boolean {
        if (mascot.manager == null) return super.hasNext()
        return super.hasNext() && target?.affordances?.contains(affordance) ?: false
    }

    override fun tick() {
        // Cannot broadcast while scanning for an affordance
        mascot.affordances.clear()

        val target = checkNotNull(target)
        val targetX = target.anchor.x
        val targetY = target.anchor.y

        putVariable(schema.getString(VARIABLE_TARGETX), targetX)
        putVariable(schema.getString(VARIABLE_TARGETY), targetY)

        if (mascot.anchor.x != targetX) {
            mascot.isLookRight = mascot.anchor.x < targetX
        }

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

            try {
                mascot.behavior = checkNotNull(Main.instance.getConfiguration(mascot.imageSet)).buildBehavior(behavior, mascot)
                target.behavior = checkNotNull(Main.instance.getConfiguration(target.imageSet)).buildBehavior(targetBehavior, target)
                if (targetLook && target.isLookRight == mascot.isLookRight) {
                    target.isLookRight = !mascot.isLookRight
                }
            } catch (e: IllegalStateException) {
                log.log(Level.SEVERE, "Fatal Error", e)
                Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviourErrorMessage"), e)
            } catch (e: BehaviorInstantiationException) {
                log.log(Level.SEVERE, "Fatal Error", e)
                Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviourErrorMessage"), e)
            } catch (e: CantBeAliveException) {
                log.log(Level.SEVERE, "Fatal Error", e)
                Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviourErrorMessage"), e)
            }
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        const val PARAMETER_BEHAVIOUR = "Behaviour"
        private const val DEFAULT_BEHAVIOUR = ""

        const val PARAMETER_TARGETBEHAVIOUR = "TargetBehaviour"
        private const val DEFAULT_TARGETBEHAVIOUR = ""

        const val PARAMETER_TARGETLOOK = "TargetLook"
        private const val DEFAULT_TARGETLOOK = false

        const val PARAMETER_VELOCITY = "VelocityParam"
        private const val DEFAULT_VELOCITY = 20.0

        const val VARIABLE_VELOCITYX = "VelocityX"
        const val VARIABLE_VELOCITYY = "VelocityY"
        const val VARIABLE_TARGETX = "TargetX"
        const val VARIABLE_TARGETY = "TargetY"
    }
}
