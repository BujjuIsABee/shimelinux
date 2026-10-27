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
import com.group_finity.mascot.localize
import com.group_finity.mascot.script.VariableMap
import java.lang.ref.WeakReference
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.abs
import kotlin.math.sqrt

private val logger: Logger = Logger.getLogger(ComplexJump::class.java.name)

/**
 * An action that causes the [mascot] to jump towards a target position or mascot and breed or interact.
 *
 * @author Kilkakon
 * @author Bujju
 */
@Suppress("unused")
class ComplexJump(
    schema: ResourceBundle,
    animations: List<Animation>,
    params: VariableMap
) : ActionBase(schema, animations, params) {
    private val delegate = Breed.Delegate(this)
    private var target: WeakReference<Mascot>? = null
    private var isBreedEnabled = false
    private var isScanEnabled = false

    /**
     * The characteristics of the action, separated by a forward slash. Options include:
     * - Breed
     * - Scan
     */
    private val characteristics: String
        get() = eval(schema.getString(PARAMETER_CHARACTERISTICS), DEFAULT_CHARACTERISTICS)

    /**
     * The behavior to set for [mascot].
     */
    private val behavior: String
        get() = eval(schema.getString(PARAMETER_BEHAVIOUR), DEFAULT_BEHAVIOUR)

    /**
     * The behavior to set for the [target] mascot.
     */
    private val targetBehavior: String
        get() = eval(schema.getString(PARAMETER_TARGETBEHAVIOUR), DEFAULT_TARGETBEHAVIOUR)

    /**
     * Whether the mascots should face each other.
     */
    private val targetLook: Boolean
        get() = eval(schema.getString(PARAMETER_TARGETLOOK), DEFAULT_TARGETLOOK)

    /**
     * The velocity of the [mascot] as it jumps towards the target position.
     */
    private val velocity: Double
        get() = eval<Number>(schema.getString(PARAMETER_VELOCITY), DEFAULT_VELOCITY).toDouble()

    /**
     * The X position that the [mascot] jumps towards. If [characteristics] includes "Scan," then the [target]'s X
     * position will be used instead.
     */
    private val targetX: Int
        get() = eval<Number>(schema.getString(PARAMETER_TARGETX), DEFAULT_TARGETX).toInt()

    /**
     * The Y position that the [mascot] jumps towards. If [characteristics] includes "Scan," then the [target]'s Y
     * position will be used instead.
     */
    private val targetY: Int
        get() = eval<Number>(schema.getString(PARAMETER_TARGETY), DEFAULT_TARGETY).toInt()

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

            target = mascot.manager?.getMascotWithAffordance(affordance)

            putVariable(schema.getString(VARIABLE_TARGETX), target?.get()?.anchor?.x)
            putVariable(schema.getString(VARIABLE_TARGETY), target?.get()?.anchor?.y)
        }
    }

    override fun hasNext(): Boolean {
        if (isScanEnabled) {
            if (mascot.manager == null) {
                return super.hasNext()
            }

            return super.hasNext() && target?.get()?.affordances?.contains(affordance) == true
        } else {
            val distanceX = (targetX - mascot.anchor.x).toDouble()
            val distanceY = (targetY - mascot.anchor.y).toDouble() - abs(distanceX) / 2.0
            val distance = sqrt(distanceX * distanceX + distanceY * distanceY)

            return super.hasNext() && distance != 0.0
        }
    }

    override fun tick() {
        val targetX: Int
        val targetY: Int

        if (isScanEnabled) {
            val target = checkNotNull(target?.get())

            // Cannot broadcast while scanning for an affordance
            mascot.affordances.clear()

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
        val distanceY = (targetY - mascot.anchor.y).toDouble() - abs(distanceX) / 2.0
        val distance = sqrt(distanceX * distanceX + distanceY * distanceY)

        val velocity = velocity

        if (distance != 0.0) {
            val velocityX = (velocity * distanceX / distance).toInt()
            val velocityY = (velocity * distanceY / distance).toInt()

            putVariable(schema.getString(VARIABLE_VELOCITYX), velocity * distanceX / distance)
            putVariable(schema.getString(VARIABLE_VELOCITYY), velocity * distanceY / distance)

            mascot.anchor.translate(velocityX, velocityY)

            checkNotNull(animation).next(mascot, time)
        }

        if (distance <= velocity) {
            mascot.anchor.setLocation(targetX, targetY)

            if (isScanEnabled) {
                try {
                    val target = checkNotNull(target?.get())
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

        if (isBreedEnabled && delegate.isIntervalFrame && delegate.isEnabled) {
            delegate.breed()
        }
    }

    companion object {
        /**
         * The key for the name of the [characteristics] parameter in the schema.
         */
        const val PARAMETER_CHARACTERISTICS = "Characteristics"
        private const val DEFAULT_CHARACTERISTICS = ""

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
         * The key for the name of the [velocity] parameter in the schema.
         */
        const val PARAMETER_VELOCITY = "VelocityParam"
        private const val DEFAULT_VELOCITY = 20.0

        /**
         * The key for the name of the [targetX] parameter in the schema.
         */
        const val PARAMETER_TARGETX = "TargetX"
        private const val DEFAULT_TARGETX = 0

        /**
         * The key for the name of the [targetY] parameter in the schema.
         */
        const val PARAMETER_TARGETY = "TargetY"
        private const val DEFAULT_TARGETY = 0

        /**
         * The key for the name of the `VelocityX` variable in the schema.
         */
        const val VARIABLE_VELOCITYX = "VelocityX"

        /**
         * The key for the name of the `VelocityY` variable in the schema.
         */
        const val VARIABLE_VELOCITYY = "VelocityY"

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
