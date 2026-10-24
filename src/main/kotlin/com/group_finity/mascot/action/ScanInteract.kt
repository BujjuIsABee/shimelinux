/*
 * Copyright (c) 2026, Bujju
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *        following disclaimer.
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *        following disclaimer in the documentation and/or other materials provided with the distribution.
 *     3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
 *        products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.Main
import com.group_finity.mascot.Mascot
import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.exception.BehaviorInstantiationException
import com.group_finity.mascot.exception.CantBeAliveException
import com.group_finity.mascot.exception.LostGroundException
import com.group_finity.mascot.localize
import com.group_finity.mascot.script.VariableMap
import java.lang.ref.WeakReference
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

private val logger: Logger = Logger.getLogger(ScanInteract::class.java.name)

/**
 * An action that scans for a mascot with the [affordance], then sets the behavior of [mascot] if one is found.
 *
 * @author Kilkakon
 * @author Bujju
 */
@Suppress("unused")
class ScanInteract(
    schema: ResourceBundle,
    animations: List<Animation>,
    context: VariableMap
) : BorderedAction(schema, animations, context) {
    private var target: WeakReference<Mascot>? = null

    /**
     * Whether there are any turning animations in [animations].
     */
    internal val hasTurningAnimation: Boolean = animations.any { it.isTurn }

    /**
     * Whether the [mascot] is currently turning.
     */
    internal var isTurning = false
        private set

    override val animation: Animation?
        get() = animations.firstOrNull { it.isEffective(variables) && isTurning == it.isTurn }

    /**
     * The behavior to set for [mascot] if another mascot with the [affordance] is found.
     */
    private val behavior: String
        get() = eval(schema.getString(PARAMETER_BEHAVIOUR), DEFAULT_BEHAVIOUR)

    /**
     * The behavior to set for the other mascot.
     */
    private val targetBehavior: String
        get() = eval(schema.getString(PARAMETER_TARGETBEHAVIOUR), DEFAULT_TARGETBEHAVIOUR)

    /**
     * Whether the mascots should face each other.
     */
    private val targetLook: Boolean
        get() = eval(schema.getString(PARAMETER_TARGETLOOK), DEFAULT_TARGETLOOK)

    override fun init(mascot: Mascot) {
        super.init(mascot)

        // Cannot broadcast while scanning for an affordance
        mascot.affordances.clear()

        putVariable(schema.getString(VARIABLE_TARGETX), null)
        putVariable(schema.getString(VARIABLE_TARGETY), null)
    }

    override fun hasNext(): Boolean = super.hasNext() && isTurning || time < checkNotNull(animation).duration

    override fun tick() {
        super.tick()

        // Cannot broadcast while scanning for an affordance
        mascot.affordances.clear()

        if (border?.isOn(mascot.anchor) == false) {
            logger.info { "Lost ground ($mascot, $this)" }
            throw LostGroundException()
        }

        if (target?.get()?.affordances?.contains(affordance) == false) {
            target = mascot.manager?.getMascotWithAffordance(affordance)
        }

        putVariable(schema.getString(VARIABLE_TARGETX), target?.get()?.anchor?.x)
        putVariable(schema.getString(VARIABLE_TARGETY), target?.get()?.anchor?.y)

        if (target?.get()?.affordances?.contains(affordance) == true) {
            val target = checkNotNull(target?.get())

            if (mascot.anchor.x != target.anchor.x) {
                isTurning = hasTurningAnimation && (isTurning || mascot.anchor.x < target.anchor.x != mascot.isLookRight)
                mascot.isLookRight = mascot.anchor.x < target.anchor.x
            }

            val animation = checkNotNull(animation)

            if (isTurning && time >= animation.duration) {
                time -= animation.duration
                isTurning = false
            }

            animation.next(mascot, time)

            if (!isTurning && (time == animation.duration - 1 || animation.duration == 1) && behavior.isNotBlank()) {
                try {
                    mascot.behavior = Main.getConfiguration(mascot.imageSet).buildBehavior(behavior, mascot)
                    if (targetBehavior.isNotBlank()) {
                        target.behavior = Main.getConfiguration(target.imageSet).buildBehavior(targetBehavior, target)
                    }
                    if (targetLook && target.isLookRight == mascot.isLookRight) {
                        target.isLookRight = !mascot.isLookRight
                    }
                } catch (e: Exception) {
                    when (e) {
                        is IllegalStateException,
                        is BehaviorInstantiationException,
                        is CantBeAliveException -> {
                            logger.log(Level.SEVERE, e) { "Failed to set behavior" }
                            Main.showError(localize("FailedSetBehaviorErrorMessage"), e)
                        }

                        else -> throw e
                    }
                }
            }
        }
    }

    companion object {
        /**
         * The key for the name of the [behavior] parameter in the schema.
         */
        const val PARAMETER_BEHAVIOUR = "Behaviour"
        private const val DEFAULT_BEHAVIOUR = ""

        /**
         * The key for the name of the [targetBehavior] parameter in the schema.
         */
        const val PARAMETER_TARGETBEHAVIOUR = "TargetBehaviour"
        private const val DEFAULT_TARGETBEHAVIOUR = ""

        /**
         * The key for the name of the [targetLook] parameter in the schema.
         */
        const val PARAMETER_TARGETLOOK = "TargetLook"
        private const val DEFAULT_TARGETLOOK = false

        /**
         * The key for the name of the `TargetX` variable in the schema.
         */
        const val VARIABLE_TARGETX = "TargetX"

        /**
         * The key for the name of the `TargetY` variable in the schema.
         */
        const val VARIABLE_TARGETY = "TargetY"
    }
}
