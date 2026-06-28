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
import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.abs
import kotlin.math.sqrt

@Suppress("unused")
class ScanJump(
    schema: ResourceBundle,
    animations: List<Animation>,
    params: VariableMap
) : ActionBase(schema, animations, params) {
    private var target: Mascot? = null

    private val behavior: String
        get() = eval(schema.getString(PARAMETER_BEHAVIOR), DEFAULT_BEHAVIOR)
    private val targetBehavior: String
        get() = eval(schema.getString(PARAMETER_TARGETBEHAVIOR), DEFAULT_TARGETBEHAVIOR)
    private val targetLook: Boolean
        get() = eval(schema.getString(PARAMETER_TARGETLOOK), DEFAULT_TARGETLOOK)
    private val velocity: Double
        get() = eval<Number>(schema.getString(PARAMETER_VELOCITY), DEFAULT_VELOCITY).toDouble()

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

        val hasAffordance = target?.affordances?.contains(affordance) == true
        return super.hasNext() && hasAffordance
    }

    override fun tick() {
        // Cannot broadcast while scanning for an affordance
        mascot.affordances.clear()

        val target = target ?: return
        val targetX = target.anchor.x
        val targetY = target.anchor.y

        putVariable(schema.getString(VARIABLE_TARGETX), targetX)
        putVariable(schema.getString(VARIABLE_TARGETY), targetY)

        if (mascot.anchor.x != targetX) {
            mascot.isLookRight = mascot.anchor.x < targetX
        }

        val distanceX = (targetX - mascot.anchor.x).toDouble()
        val distanceY = (targetY - mascot.anchor.y).toDouble() - abs(distanceX) / 2.0
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
                Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviorErrorMessage"), e)
            } catch (e: CantBeAliveException) {
                log.log(Level.SEVERE, "Fatal Error", e)
                Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviorErrorMessage"), e)
            }
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        @get:JvmName("PARAMETER_BEHAVIOUR")
        const val PARAMETER_BEHAVIOR = "Behavior"
        private const val DEFAULT_BEHAVIOR = ""

        @get:JvmName("PARAMETER_TARGETBEHAVIOUR")
        const val PARAMETER_TARGETBEHAVIOR = "TargetBehavior"
        private const val DEFAULT_TARGETBEHAVIOR = ""

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
