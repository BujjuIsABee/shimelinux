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
import com.group_finity.mascot.getConfiguration
import com.group_finity.mascot.localize
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

@Suppress("unused")
class ScanInteract(
    schema: ResourceBundle,
    animations: List<Animation>,
    context: VariableMap
) : BorderedAction(schema, animations, context) {
    private var target: Mascot? = null
    internal val hasTurningAnimation = animations.any { it.isTurn }
    internal var isTurning = false
        private set

    override val animation: Animation?
        get() = animations.firstOrNull { it.isEffective(variables) && isTurning == it.isTurn }

    private val behavior: String
        get() = eval(schema.getString(PARAMETER_BEHAVIOR), DEFAULT_BEHAVIOR)
    private val targetBehavior: String
        get() = eval(schema.getString(PARAMETER_TARGETBEHAVIOR), DEFAULT_TARGETBEHAVIOR)
    private val targetLook: Boolean
        get() = eval(schema.getString(PARAMETER_TARGETLOOK), DEFAULT_TARGETLOOK)

    override fun init(mascot: Mascot) {
        super.init(mascot)

        // Cannot broadcast while scanning for an affordance
        mascot.affordances.clear()

        putVariable(schema.getString(VARIABLE_TARGETX), null)
        putVariable(schema.getString(VARIABLE_TARGETY), null)
    }

    override fun hasNext() = super.hasNext() && (isTurning || animation?.let { time < it.duration } == true)

    override fun tick() {
        super.tick()

        // Cannot broadcast while scanning for an affordance
        mascot.affordances.clear()

        if (border?.isOn(mascot.anchor) == false) {
            log.log(Level.INFO, "Lost ground ($mascot, $this)")
            throw LostGroundException()
        }

        if (target?.affordances?.contains(affordance) == false) {
            target = mascot.manager?.getMascotWithAffordance(affordance)?.get()
        }

        putVariable(schema.getString(VARIABLE_TARGETX), target?.anchor?.x)
        putVariable(schema.getString(VARIABLE_TARGETY), target?.anchor?.y)

        if (target?.affordances?.contains(affordance) == true) {
            val target = checkNotNull(target)
            val animation = checkNotNull(animation)

            if (mascot.anchor.x != target.anchor.x) {
                isTurning = hasTurningAnimation && (isTurning || mascot.anchor.x < target.anchor.x != mascot.isLookRight)
                mascot.isLookRight = mascot.anchor.x < target.anchor.x
            }

            if (isTurning && time >= animation.duration) {
                time -= animation.duration
                isTurning = false
            }

            animation.next(mascot, time)

            if (!isTurning && (time == animation.duration - 1 || animation.duration == 1) && behavior.isNotBlank()) {
                try {
                    mascot.behavior = getConfiguration(mascot.imageSet).buildBehavior(behavior, mascot)
                    if (targetBehavior.isNotBlank()) {
                        target.behavior = getConfiguration(target.imageSet).buildBehavior(targetBehavior, target)
                    }
                    if (targetLook && target.isLookRight == mascot.isLookRight) {
                        target.isLookRight = !mascot.isLookRight
                    }
                } catch (e: Exception) {
                    when (e) {
                        is IllegalStateException,
                        is BehaviorInstantiationException,
                        is CantBeAliveException -> {
                            log.log(Level.SEVERE, "Fatal Error", e)
                            Main.showError("FailedSetBehaviorErrorMessage".localize(), e)
                        }

                        else -> throw e
                    }
                }
            }
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        const val PARAMETER_BEHAVIOR = "Behavior"
        private const val DEFAULT_BEHAVIOR = ""

        const val PARAMETER_TARGETBEHAVIOR = "TargetBehavior"
        private const val DEFAULT_TARGETBEHAVIOR = ""

        const val PARAMETER_TARGETLOOK = "TargetLook"
        private const val DEFAULT_TARGETLOOK = false

        const val VARIABLE_TARGETX = "TargetX"
        const val VARIABLE_TARGETY = "TargetY"
    }
}
