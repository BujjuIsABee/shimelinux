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

class ScanMove(
    schema: ResourceBundle,
    animations: List<Animation>,
    params: VariableMap
) : BorderedAction(schema, animations, params) {
    private var target: Mascot? = null
    internal var isTurning = false
    override val animation
        get() = animations.firstOrNull { it.isEffective(variables) && isTurning == it.isTurn }
    internal val hasTurningAnimation by lazy {
        return@lazy animations.any { it.isTurn }
    }

    private val behavior
        get() = eval(schema.getString(PARAMETER_BEHAVIOUR), String::class, DEFAULT_BEHAVIOUR)
    private val targetBehavior
        get() = eval(schema.getString(PARAMETER_TARGETBEHAVIOUR), String::class, DEFAULT_TARGETBEHAVIOUR)
    private val targetLook
        get() = eval(schema.getString(PARAMETER_TARGETLOOK), Boolean::class, DEFAULT_TARGETLOOK)

    override fun init(mascot: Mascot) {
        super.init(mascot)

        // Cannot broadcast while scanning for an affordance
        mascot.affordances.clear()

        target = mascot.manager?.getMascotWithAffordance(affordance)?.get()

        putVariable(schema.getString(VARIABLE_TARGETX), target?.anchor?.x)
        putVariable(schema.getString(VARIABLE_TARGETY), target?.anchor?.y)
    }

    override fun hasNext(): Boolean {
        if (mascot.manager == null) return super.hasNext()
        return super.hasNext() && (isTurning || target?.affordances?.contains(affordance) == true)
    }

    override fun tick() {
        super.tick()

        // Cannot broadcast while scanning for an affordance
        mascot.affordances.clear()

        if (border?.isOn(mascot.anchor) == false) {
            log.log(Level.INFO, "Lost ground ($mascot, $this)")
            throw LostGroundException()
        }

        val target = checkNotNull(target)
        val targetX = target.anchor.x
        val targetY = target.anchor.y

        putVariable(schema.getString(VARIABLE_TARGETX), targetX)
        putVariable(schema.getString(VARIABLE_TARGETY), targetY)

        if (mascot.anchor.x != targetX) {
            isTurning = hasTurningAnimation && (isTurning || mascot.anchor.x < targetX != mascot.isLookRight)
            mascot.isLookRight = mascot.anchor.x < targetX
        }

        val down = mascot.anchor.y < targetY

        if (isTurning && animation?.let { time >= it.duration } == true) {
            isTurning = false
        }

        animation?.next(mascot, time)

        if ((mascot.isLookRight && mascot.anchor.x >= targetX) || (!mascot.isLookRight && mascot.anchor.x <= targetX)) {
            mascot.anchor = Point(targetX, mascot.anchor.y)
        }
        if ((down && mascot.anchor.y >= targetY) || (!down && mascot.anchor.y <= targetY)) {
            mascot.anchor = Point(mascot.anchor.x, targetY)
        }

        if (!isTurning && mascot.anchor.x == targetX && mascot.anchor.y == targetY) {
            try {
                mascot.behavior = Main.instance.getConfiguration(mascot.imageSet)?.buildBehavior(
                    behavior,
                    mascot
                )
                target.behavior = Main.instance.getConfiguration(target.imageSet)?.buildBehavior(
                    targetBehavior,
                    target
                )
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

        const val VARIABLE_TARGETX = "TargetX"
        const val VARIABLE_TARGETY = "TargetY"
    }
}
