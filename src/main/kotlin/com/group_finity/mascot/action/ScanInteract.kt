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
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

@Suppress("UNUSED")
class ScanInteract(
    schema: ResourceBundle,
    animations: List<Animation>,
    context: VariableMap,
) : BorderedAction(schema, animations, context) {
    private var target: Mascot? = null
    internal var isTurning = false
        private set
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

        putVariable(schema.getString(VARIABLE_TARGETX), null)
        putVariable(schema.getString(VARIABLE_TARGETY), null)
    }

    override fun hasNext() = super.hasNext() && isTurning || animation?.let { time < it.duration } == true

    override fun tick() {
        super.tick()

        // Cannot broadcast while scanning for an affordance
        mascot.affordances.clear()

        if (border?.isOn(mascot.anchor) == false) {
            log.log(Level.INFO, "Lost ground ($mascot,$this)")
            throw LostGroundException()
        }

        mascot.manager?.let { manager ->
            if (target?.affordances?.contains(affordance) == false) {
                target = manager.getMascotWithAffordance(affordance)?.get()
            }
        }

        putVariable(schema.getString(VARIABLE_TARGETX), target?.anchor?.x)
        putVariable(schema.getString(VARIABLE_TARGETY), target?.anchor?.y)

        if (target?.affordances?.contains(affordance) ?: false) {
            val target = checkNotNull(target)
            val animation = checkNotNull(animation)

            if (mascot.anchor.x != target.anchor.x) {
                isTurning =hasTurningAnimation && (isTurning || mascot.anchor.x < target.anchor.x != mascot.isLookRight)
                mascot.isLookRight = mascot.anchor.x < target.anchor.x
            }

            if (isTurning && time >= animation.duration) {
                time -= animation.duration
                isTurning = false
            }

            animation.next(mascot, time)

            if (!isTurning && (time == animation.duration - 1 || animation.duration == 1) && !behavior.trim().isEmpty()) {
                try {
                    mascot.behavior = checkNotNull(Main.instance.getConfiguration(mascot.imageSet)).buildBehavior(
                        behavior,
                        mascot
                    )
                    if (!targetBehavior.trim().isEmpty()) {
                        target.behavior = checkNotNull(Main.instance.getConfiguration(target.imageSet)).buildBehavior(
                            targetBehavior,
                            target
                        )
                    }
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
