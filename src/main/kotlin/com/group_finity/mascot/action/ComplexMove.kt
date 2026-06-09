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
import com.group_finity.mascot.exception.LostGroundException
import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

class ComplexMove(
    schema: ResourceBundle,
    animations: ArrayList<Animation>,
    params: VariableMap
) : BorderedAction(schema, animations, params) {
    private val delegate = Breed.Delegate(this)
    private var target: Mascot? = null
    internal var isTurning = false
        private set
    internal val hasTurningAnimation: Boolean by lazy {
        for (animation in animations) {
            if (animation.isTurn) {
                return@lazy true
            }
        }
        return@lazy false
    }
    private var isBreedEnabled = false
    private var isScanEnabled = false

    private val characteristics: String
        get() = eval(schema.getString(PARAMETER_CHARACTERISTICS), String::class, DEFAULT_CHARACTERISTICS)
    private val behavior: String
        get() = eval(schema.getString(PARAMETER_BEHAVIOUR), String::class, DEFAULT_BEHAVIOUR)
    private val targetBehavior: String
        get() = eval(schema.getString(PARAMETER_TARGETBEHAVIOUR), String::class, DEFAULT_TARGETBEHAVIOUR)
    private val targetLook: Boolean
        get() = eval(schema.getString(PARAMETER_TARGETLOOK), Boolean::class, DEFAULT_TARGETLOOK)
    private val targetX: Int
        get() = eval(schema.getString(PARAMETER_TARGETX), Number::class, DEFAULT_TARGETX).toInt()
    private val targetY: Int
        get() = eval(schema.getString(PARAMETER_TARGETY), Number::class, DEFAULT_TARGETY).toInt()

    override val animation: Animation?
        get() {
            for (animation in animations) {
                if (animation.isEffective(variables) && isTurning != animation.isTurn) {
                    return animation
                }
            }
            return null
        }

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

            if (mascot.manager != null) {
                target = mascot.manager!!.getMascotWithAffordance(affordance)?.get()
            }

            putVariable(schema.getString(VARIABLE_TARGETX), target?.anchor?.x)
            putVariable(schema.getString(VARIABLE_TARGETY), target?.anchor?.y)
        }
    }

    override fun hasNext(): Boolean {
        if (isScanEnabled) {
            if (mascot.manager == null) return super.hasNext()
            return super.hasNext() && (isTurning || target?.affordances?.contains(affordance) ?: false)
        } else {
            val hasNotReached =
                (targetX != Int.MIN_VALUE && mascot.anchor.x == targetX) ||
                (targetY != Int.MIN_VALUE && mascot.anchor.y == targetY)
            return super.hasNext() && hasNotReached
        }
    }

    override fun tick() {
        super.tick()

        if (isScanEnabled) {
            // Cannot broadcast while scanning for an affordance
            mascot.affordances.clear()
        }

        if ((border != null) && !border!!.isOn(mascot.anchor)) {
            log.log(Level.INFO, "Lost ground ($mascot,$this)")
            throw LostGroundException()
        }

        val targetX = if (isScanEnabled) checkNotNull(target?.anchor?.x) else targetX
        val targetY = if (isScanEnabled) checkNotNull(target?.anchor?.y) else targetY

        if (isScanEnabled) {
            putVariable(schema.getString(VARIABLE_TARGETX), targetX)
            putVariable(schema.getString(VARIABLE_TARGETY), targetY)
        }

        if (mascot.anchor.x != targetX) {
            isTurning = hasTurningAnimation && (isTurning || mascot.anchor.x < targetX != mascot.isLookRight)
            mascot.isLookRight = mascot.anchor.x < targetX
        }

        val down = mascot.anchor.y < targetY

        if (isTurning && time >= checkNotNull(animation).duration) {
            isTurning = false
        }

        animation?.next(mascot, time)

        if (targetX != DEFAULT_TARGETX || isScanEnabled) {
            if ((mascot.isLookRight && (mascot.anchor.x >= targetX)) ||
                (!mascot.isLookRight && (mascot.anchor.x <= targetX))
            ) {
                mascot.anchor = Point(targetX, mascot.anchor.y)
            }
        }

        if (targetY != DEFAULT_TARGETY || isScanEnabled) {
            if ((down && (mascot.anchor.y >= targetY)) ||
                (!down && (mascot.anchor.y <= targetY))
            ) {
                mascot.anchor = Point(mascot.anchor.x, targetY)
            }
        }

        if (isBreedEnabled && delegate.isIntervalFrame && delegate.isEnabled) {
            delegate.breed()
        }

        if (!isTurning && mascot.anchor.x == targetX && mascot.anchor.y == targetY) {
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

        const val PARAMETER_TARGETX = "TargetX"
        private const val DEFAULT_TARGETX = 0

        const val PARAMETER_TARGETY = "TargetY"
        private const val DEFAULT_TARGETY = 0

        const val VARIABLE_TARGETX = "TargetX"
        const val VARIABLE_TARGETY = "TargetY"
    }
}
