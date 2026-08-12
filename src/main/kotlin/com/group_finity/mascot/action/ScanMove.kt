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
import java.awt.Point
import java.lang.ref.WeakReference
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

private val logger = Logger.getLogger(ScanMove::class.java.name)

@Suppress("unused")
class ScanMove(
    schema: ResourceBundle,
    animations: List<Animation>,
    params: VariableMap
) : BorderedAction(schema, animations, params) {
    private var target: WeakReference<Mascot>? = null
    internal val hasTurningAnimation = animations.any { it.isTurn }
    internal var isTurning = false

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

        target = mascot.manager?.getMascotWithAffordance(affordance)

        putVariable(schema.getString(VARIABLE_TARGETX), target?.get()?.anchor?.x)
        putVariable(schema.getString(VARIABLE_TARGETY), target?.get()?.anchor?.y)
    }

    override fun hasNext(): Boolean {
        if (mascot.manager == null) {
            return super.hasNext()
        }

        return super.hasNext() && (isTurning || target?.get()?.affordances?.contains(affordance) == true)
    }

    override fun tick() {
        super.tick()

        val target = checkNotNull(target?.get())

        // Cannot broadcast while scanning for an affordance
        mascot.affordances.clear()

        if (border?.isOn(mascot.anchor) == false) {
            logger.info { "Lost ground ($mascot, $this)" }
            throw LostGroundException()
        }

        val targetX = target.anchor.x
        val targetY = target.anchor.y

        putVariable(schema.getString(VARIABLE_TARGETX), targetX)
        putVariable(schema.getString(VARIABLE_TARGETY), targetY)

        if (mascot.anchor.x != targetX) {
            isTurning = hasTurningAnimation && (isTurning || mascot.anchor.x < targetX != mascot.isLookRight)
            mascot.isLookRight = mascot.anchor.x < targetX
        }

        val down = mascot.anchor.y < targetY

        val animation = checkNotNull(animation)

        if (isTurning && time >= animation.duration) {
            isTurning = false
        }

        animation.next(mascot, time)

        if (mascot.isLookRight && mascot.anchor.x >= targetX || !mascot.isLookRight && mascot.anchor.x <= targetX) {
            mascot.anchor = Point(targetX, mascot.anchor.y)
        }

        if (down && mascot.anchor.y >= targetY || !down && mascot.anchor.y <= targetY) {
            mascot.anchor = Point(mascot.anchor.x, targetY)
        }

        if (!isTurning && mascot.anchor.x == targetX && mascot.anchor.y == targetY) {
            try {
                mascot.behavior = Main.getConfiguration(mascot.imageSet).buildBehavior(behavior, mascot)
                target.behavior = Main.getConfiguration(target.imageSet).buildBehavior(targetBehavior, target)
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

    companion object {
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
