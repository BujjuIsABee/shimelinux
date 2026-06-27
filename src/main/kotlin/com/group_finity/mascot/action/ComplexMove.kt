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
import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

class ComplexMove(
    schema: ResourceBundle,
    animations: List<Animation>,
    params: VariableMap
) : BorderedAction(schema, animations, params) {
    private val delegate = Breed.Delegate(this)
    private var target: Mascot? = null
    private var isBreedEnabled = false
    private var isScanEnabled = false
    internal val hasTurningAnimation = animations.any { it.isTurn }
    internal var isTurning = false
        private set

    override val animation: Animation?
        get() = animations.firstOrNull { it.isEffective(variables) && isTurning != it.isTurn }

    private val characteristics: String
        get() = eval(schema.getString(PARAMETER_CHARACTERISTICS), DEFAULT_CHARACTERISTICS)
    private val behavior: String
        get() = eval(schema.getString(PARAMETER_BEHAVIOR), DEFAULT_BEHAVIOR)
    private val targetBehavior: String
        get() = eval(schema.getString(PARAMETER_TARGETBEHAVIOUR), DEFAULT_TARGETBEHAVIOUR)
    private val targetLook: Boolean
        get() = eval(schema.getString(PARAMETER_TARGETLOOK), DEFAULT_TARGETLOOK)
    private val targetX: Int
        get() = eval<Number>(schema.getString(PARAMETER_TARGETX), DEFAULT_TARGETX).toInt()
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

            target = mascot.manager?.getMascotWithAffordance(affordance)?.get()

            putVariable(schema.getString(VARIABLE_TARGETX), target?.anchor?.x)
            putVariable(schema.getString(VARIABLE_TARGETY), target?.anchor?.y)
        }
    }

    override fun hasNext(): Boolean {
        if (isScanEnabled) {
            if (mascot.manager == null) return super.hasNext()

            val hasAffordance = target?.affordances?.contains(affordance) == true
            return super.hasNext() && (isTurning || hasAffordance)
        } else {
            val hasNotReached = (targetX != Int.MIN_VALUE && mascot.anchor.x == targetX) || (targetY != Int.MIN_VALUE && mascot.anchor.y == targetY)
            return super.hasNext() && (!hasNotReached || isTurning)
        }
    }

    override fun tick() {
        super.tick()

        val target = target

        if (isScanEnabled) {
            // Cannot broadcast while scanning for an affordance
            mascot.affordances.clear()
        }

        if (border?.isOn(mascot.anchor) == false) {
            log.log(Level.INFO, "Lost ground ($mascot, $this)")
            throw LostGroundException()
        }

        val targetX = if (isScanEnabled && target != null) target.anchor.x else targetX
        val targetY = if (isScanEnabled && target != null) target.anchor.y else targetY

        if (isScanEnabled) {
            putVariable(schema.getString(VARIABLE_TARGETX), targetX)
            putVariable(schema.getString(VARIABLE_TARGETY), targetY)
        }

        if (mascot.anchor.x != targetX) {
            isTurning = hasTurningAnimation && (isTurning || mascot.anchor.x < targetX != mascot.isLookRight)
            mascot.isLookRight = mascot.anchor.x < targetX
        }

        val down = mascot.anchor.y < targetY

        if (isTurning && animation?.let { time >= it.duration } == true) {
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
                mascot.behavior = checkNotNull(Main.instance.getConfiguration(mascot.imageSet)).buildBehavior(behavior, mascot)
                checkNotNull(target).behavior = checkNotNull(Main.instance.getConfiguration(target.imageSet)).buildBehavior(targetBehavior, target)
                if (targetLook && target.isLookRight == mascot.isLookRight) {
                    target.isLookRight = !mascot.isLookRight
                }
            } catch (e: IllegalStateException) {
                log.log(Level.SEVERE, "Fatal Error", e)
                Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviorErrorMessage"), e)
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

        const val PARAMETER_CHARACTERISTICS = "Characteristics"
        private const val DEFAULT_CHARACTERISTICS = ""

        @get:JvmName("PARAMETER_BEHAVIOUR")
        const val PARAMETER_BEHAVIOR = "Behavior"
        private const val DEFAULT_BEHAVIOR = ""

        @get:JvmName("PARAMETER_TARGETBEHAVIOUR")
        const val PARAMETER_TARGETBEHAVIOUR = "TargetBehavior"
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
