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
class ComplexJump(
    schema: ResourceBundle,
    animations: List<Animation>,
    params: VariableMap
) : ActionBase(schema, animations, params) {
    private val delegate = Breed.Delegate(this)
    private var target: Mascot? = null
    private var isBreedEnabled = false
    private var isScanEnabled = false

    private val characteristics
        get() = eval(schema.getString(PARAMETER_CHARACTERISTICS), String::class, DEFAULT_CHARACTERISTICS)
    private val behavior
        get() = eval(schema.getString(PARAMETER_BEHAVIOUR), String::class, DEFAULT_BEHAVIOUR)
    private val targetBehavior
        get() = eval(schema.getString(PARAMETER_TARGETBEHAVIOUR), String::class, DEFAULT_TARGETBEHAVIOUR)
    private val targetLook
        get() = eval(schema.getString(PARAMETER_TARGETLOOK), Boolean::class, DEFAULT_TARGETLOOK)
    private val velocity
        get() = eval(schema.getString(PARAMETER_VELOCITY), Number::class, DEFAULT_VELOCITY).toDouble()
    private val targetX
        get() = eval(schema.getString(PARAMETER_TARGETX), Number::class, DEFAULT_TARGETX).toInt()
    private val targetY
        get() = eval(schema.getString(PARAMETER_TARGETY), Number::class, DEFAULT_TARGETY).toInt()

    override fun init(mascot: Mascot) {
        super.init(mascot)

        for (characteristic in characteristics.split(',')) {
            if (characteristic == schema.getString("Breed")) {
                isBreedEnabled = true
            }
            if (characteristic == schema.getString("Scan")) {
                isScanEnabled = true
            }
        }

        if (isBreedEnabled) {
            delegate.validateBornCount()
            delegate.validateBornInterval()
        }

        if (isScanEnabled) {
            // Cannot broadcast while scanning for an affordance
            mascot.affordances.clear()

            mascot.manager?.let {
                target = it.getMascotWithAffordance(affordance)?.get()
            }

            putVariable(schema.getString(VARIABLE_TARGETX), target?.anchor?.x)
            putVariable(schema.getString(VARIABLE_TARGETY), target?.anchor?.y)
        }
    }

    override fun hasNext(): Boolean {
        if (isScanEnabled) {
            if (mascot.manager == null) return super.hasNext()
            return super.hasNext() && (target?.affordances?.contains(affordance) ?: false)
        } else {
            val distanceX = (targetX - mascot.anchor.x).toDouble()
            val distanceY = (targetY - mascot.anchor.y).toDouble() - abs(distanceX) / 2
            val distance = sqrt(distanceX * distanceX + distanceY * distanceY)

            return super.hasNext() && (distance != 0.0)
        }
    }

    override fun tick() {
        var targetX: Int
        var targetY: Int

        if (isScanEnabled) {
            // Cannot broadcast while scanning for an affordance
            mascot.affordances.clear()

            val target = checkNotNull(target)
            targetX = target.anchor.x
            targetY = target.anchor.y

            putVariable(schema.getString(VARIABLE_TARGETX), targetX)
            putVariable(schema.getString(VARIABLE_TARGETY), targetY)

            if (mascot.anchor.x != targetX) {
                mascot.isLookRight = mascot.anchor.x < targetX
            }
        } else {
            targetX = this.targetX
            targetY = this.targetY

            mascot.isLookRight = mascot.anchor.x < targetX
        }

        val distanceX = (targetX - mascot.anchor.x).toDouble()
        val distanceY = (targetY - mascot.anchor.y).toDouble() - abs(distanceX) / 2
        val distance = sqrt(distanceX * distanceX + distanceY * distanceY)

        if (distance != 0.0) {
            val velocityX = (velocity * distanceX / distance).toInt()
            val velocityY = (velocity * distanceY / distance).toInt()

            putVariable(schema.getString(VARIABLE_VELOCITYX), velocityX)
            putVariable(schema.getString(VARIABLE_VELOCITYY), velocityY)

            mascot.anchor = Point(
                mascot.anchor.x + velocityX,
                mascot.anchor.y + velocityY
            )

            animation?.next(mascot, time)
        }

        if (distance <= velocity) {
            mascot.anchor = Point(targetX, targetY)

            if (isScanEnabled) {
                try {
                    val target = checkNotNull(target)
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

        if (isBreedEnabled && delegate.isIntervalFrame && delegate.isEnabled) {
            delegate.breed()
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        const val PARAMETER_CHARACTERISTICS = "Characteristics"
        private const val DEFAULT_CHARACTERISTICS = ""

        const val PARAMETER_BEHAVIOUR = "Behaviour"
        private const val DEFAULT_BEHAVIOUR = ""

        const val PARAMETER_TARGETBEHAVIOUR = "TargetBehaviour"
        private const val DEFAULT_TARGETBEHAVIOUR = ""

        const val PARAMETER_TARGETLOOK = "TargetLook"
        private const val DEFAULT_TARGETLOOK = false

        const val PARAMETER_VELOCITY = "VelocityParam"
        private const val DEFAULT_VELOCITY = 20.0

        const val PARAMETER_TARGETX = "TargetX"
        private const val DEFAULT_TARGETX = 0

        const val PARAMETER_TARGETY = "TargetY"
        private const val DEFAULT_TARGETY = 0

        const val VARIABLE_VELOCITYX = "VelocityX"
        const val VARIABLE_VELOCITYY = "VelocityY"
        const val VARIABLE_TARGETX = "TargetX"
        const val VARIABLE_TARGETY = "TargetY"
    }
}
