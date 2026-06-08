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
import java.lang.ref.WeakReference
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.abs
import kotlin.math.sqrt

class ScanMove(
    schema: ResourceBundle,
    animations: ArrayList<Animation>,
    params: VariableMap,
) : BorderedAction(schema, animations, params) {
    private var target: WeakReference<Mascot>? = null
    internal var isTurning = false
    internal val hasTurningAnimation: Boolean by lazy {
        for (animation in animations) {
            if (animation.isTurn) {
                return@lazy true
            }
        }
        return@lazy false
    }

    private val behavior: String
        get() = eval(schema.getString(PARAMETER_BEHAVIOUR), String::class, DEFAULT_BEHAVIOUR)
    private val targetBehavior: String
        get() = eval(schema.getString(PARAMETER_TARGETBEHAVIOUR), String::class, DEFAULT_TARGETBEHAVIOUR)
    private val targetLook: Boolean
        get() = eval(schema.getString(PARAMETER_TARGETLOOK), Boolean::class, DEFAULT_TARGETLOOK)

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

        mascot.affordances.clear()

        if (mascot.manager != null) {
            target = mascot.manager!!.getMascotWithAffordance(affordance)
        }

        putVariable(schema.getString("TargetX"), target?.get()?.anchor?.x)
        putVariable(schema.getString("TargetY"), target?.get()?.anchor?.y)
    }

    override fun hasNext(): Boolean {
        if (mascot.manager == null) return super.hasNext()

        return super.hasNext() && (target?.get()?.affordances?.contains(affordance) ?: false)
    }

    override fun tick() {
        super.tick()

        mascot.affordances.clear()

        if ((border != null) && !border!!.isOn(mascot.anchor)) {
            log.log(Level.INFO, "Lost ground ($mascot,$this)")
            throw LostGroundException()
        }

        val target = checkNotNull(target?.get())
        val targetX = target.anchor.x
        val targetY = target.anchor.y

        putVariable(schema.getString("TargetX"), targetX)
        putVariable(schema.getString("TargetY"), targetY)

        if (mascot.anchor.x != targetX) {
            isTurning = hasTurningAnimation && (isTurning || mascot.anchor.x < targetX != mascot.isLookRight)
            mascot.isLookRight = mascot.anchor.x < targetX
        }

        val down = mascot.anchor.y < targetY

        if (isTurning && time >= checkNotNull(animation).duration) {
            isTurning = false
        }

        animation?.next(mascot, time)

        if ((mascot.isLookRight && (mascot.anchor.x >= targetX)) ||
            (!mascot.isLookRight && (mascot.anchor.x <= targetX))
        ) {
            mascot.anchor = Point(targetX, mascot.anchor.y)
        }

        if ((down && (mascot.anchor.y >= targetY)) ||
            (!down && (mascot.anchor.y <= targetY))
        ) {
            mascot.anchor = Point(mascot.anchor.x, targetY)
        }

        if (!isTurning && mascot.anchor.x == targetX && mascot.anchor.y == targetY) {
            try {
                mascot.behavior = checkNotNull(Main.instance.getConfiguration(mascot.imageSet)).buildBehavior(behavior, mascot)
                target.behavior = checkNotNull(Main.instance.getConfiguration(target.imageSet)).buildBehavior(targetBehavior, target)
                if (targetLook && target.isLookRight == mascot.isLookRight) {
                    target.isLookRight = !mascot.isLookRight
                }
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
    }
}
