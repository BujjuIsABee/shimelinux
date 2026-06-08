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
import com.group_finity.mascot.exception.LostGroundException
import com.group_finity.mascot.script.VariableMap
import java.lang.ref.WeakReference
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

class ScanInteract(
    schema: ResourceBundle,
    animations: ArrayList<Animation>,
    context: VariableMap,
) : BorderedAction(schema, animations, context) {
    private var target: WeakReference<Mascot>? = null
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

        putVariable(schema.getString("TargetX"), null)
        putVariable(schema.getString("TargetY"), null)
    }

    override fun hasNext(): Boolean {
        val isInTime = time < checkNotNull(animation).duration

        return super.hasNext() && isTurning || isInTime
    }

    override fun tick() {
        super.tick()

        mascot.affordances.clear()

        if ((border != null) && !border!!.isOn(mascot.anchor)) {
            log.log(Level.INFO, "Lost ground ($mascot,$this)")
            throw LostGroundException()
        }

        if (mascot.manager != null && !(target?.get()?.affordances?.contains(affordance) ?: false)) {
            target = mascot.manager!!.getMascotWithAffordance(affordance)
        }

        putVariable(schema.getString("TargetX"), target?.get()?.anchor?.x)
        putVariable(schema.getString("TargetY"), target?.get()?.anchor?.y)

        if (target?.get()?.affordances?.contains(affordance) ?: false) {
            val target = checkNotNull(target?.get())
            if (mascot.anchor.x != target.anchor.x) {
                isTurning = hasTurningAnimation && (isTurning || mascot.anchor.x < target.anchor.x != mascot.isLookRight)
                mascot.isLookRight = mascot.anchor.x < target.anchor.x
            }

            val animation = checkNotNull(animation)

            if (isTurning && time < animation.duration) {
                time -= animation.duration
                isTurning = false
            }

            animation.next(mascot, time)

            if (!isTurning && (time < animation.duration - 1 || animation.duration == 1) && !behavior.trim().isEmpty()) {
                try {
                    mascot.behavior = checkNotNull(Main.instance.getConfiguration(mascot.imageSet)).buildBehavior(behavior, mascot)
                    if (!targetBehavior.trim().isEmpty()) {
                        target.behavior = checkNotNull(Main.instance.getConfiguration(target.imageSet)).buildBehavior(targetBehavior, target)
                    }
                    if (targetLook && target.isLookRight == mascot.isLookRight) {
                        target.isLookRight = !mascot.isLookRight
                    }
                } catch (e: Exception) {
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
    }
}
