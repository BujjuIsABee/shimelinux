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
import com.group_finity.mascot.exception.VariableException
import com.group_finity.mascot.tryGetConfiguration
import com.group_finity.mascot.getConfiguration
import com.group_finity.mascot.getProperty
import com.group_finity.mascot.localize
import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.roundToInt

class Breed(
    schema: ResourceBundle,
    animations: List<Animation>,
    context: VariableMap
) : Animate(schema, animations, context) {
    private val delegate = Delegate(this)

    override fun init(mascot: Mascot) {
        super.init(mascot)

        delegate.validateBornCount()
    }

    override fun tick() {
        super.tick()

        if (delegate.isPenultimateFrame && delegate.isEnabled) {
            delegate.breed()
        }
    }

    class Delegate(private val action: ActionBase) {
        val isEnabled: Boolean
            get() = getProperty(if (bornTransient) "Transients" else "Breeding", true)
        val isIntervalFrame: Boolean
            get() = action.time % bornInterval == 0
        val isPenultimateFrame: Boolean
            get() = action.animation?.let { action.time == it.duration - 1 } == true

        private val bornX: Int
            get() = action.eval<Number>(action.schema.getString(PARAMETER_BORNX), DEFAULT_BORNX).toInt()
        private val bornY: Int
            get() = action.eval<Number>(action.schema.getString(PARAMETER_BORNY), DEFAULT_BORNY).toInt()
        private val bornBehavior: String
            get() = action.eval(action.schema.getString(PARAMETER_BORNBEHAVIOR), DEFAULT_BORNBEHAVIOR)
        private val bornMascot: String
            get() = action.eval(action.schema.getString(PARAMETER_BORNMASCOT), DEFAULT_BORNMASCOT)
        private val bornTransient: Boolean
            get() = action.eval(action.schema.getString(PARAMETER_BORNTRANSIENT), DEFAULT_BORNTRANSIENT)
        private val bornInterval: Int
            get() = action.eval<Number>(action.schema.getString(PARAMETER_BORNINTERVAL), DEFAULT_BORNINTERVAL).toInt()
        private val bornCount: Int
            get() = action.eval<Number>(action.schema.getString(PARAMETER_BORNCOUNT), DEFAULT_BORNCOUNT).toInt()

        fun breed() {
            val scaling = getProperty("Scaling", 1.0)
            val childType = bornMascot.takeUnless { tryGetConfiguration(it) == null } ?: action.mascot.imageSet

            repeat(bornCount) {
                val mascot = Mascot(childType)

                log.log(Level.INFO, "Mascot breeding (${action.mascot}, $action, $mascot)")

                mascot.anchor = if (action.mascot.isLookRight) {
                    Point(
                        action.mascot.anchor.x - (bornX * scaling).roundToInt(),
                        action.mascot.anchor.y + (bornY * scaling).roundToInt()
                    )
                } else {
                    Point(
                        action.mascot.anchor.x + (bornX * scaling).roundToInt(),
                        action.mascot.anchor.y + (bornY * scaling).roundToInt()
                    )
                }

                mascot.isLookRight = action.mascot.isLookRight

                try {
                    mascot.behavior = getConfiguration(childType).buildBehavior(bornBehavior, action.mascot)
                    checkNotNull(action.mascot.manager).add(mascot)
                } catch (e: IllegalStateException) {
                    log.log(Level.SEVERE, "Fatal Error", e)
                    Main.showError("FailedCreateNewShimejiErrorMessage".localize(), e)
                    mascot.dispose()
                } catch (e: BehaviorInstantiationException) {
                    log.log(Level.SEVERE, "Fatal Error", e)
                    Main.showError("FailedCreateNewShimejiErrorMessage".localize(), e)
                    mascot.dispose()
                } catch (e: CantBeAliveException) {
                    log.log(Level.SEVERE, "Fatal Error", e)
                    Main.showError("FailedCreateNewShimejiErrorMessage".localize(), e)
                    mascot.dispose()
                }
            }
        }

        fun validateBornCount() {
            if (bornCount < 1) {
                throw VariableException("BornCount must be positive")
            }
        }

        fun validateBornInterval() {
            if (bornInterval < 1) {
                throw VariableException("BornInterval must be positive")
            }
        }

        companion object {
            private val log = Logger.getLogger(this::class.java.name)

            const val PARAMETER_BORNX = "BornX"
            private const val DEFAULT_BORNX = 0

            const val PARAMETER_BORNY = "BornY"
            private const val DEFAULT_BORNY = 0

            const val PARAMETER_BORNBEHAVIOR = "BornBehavior"
            private const val DEFAULT_BORNBEHAVIOR = ""

            const val PARAMETER_BORNMASCOT = "BornMascot"
            private const val DEFAULT_BORNMASCOT = ""

            const val PARAMETER_BORNINTERVAL = "BornInterval"
            private const val DEFAULT_BORNINTERVAL = 1

            const val PARAMETER_BORNTRANSIENT = "BornTransient"
            private const val DEFAULT_BORNTRANSIENT = false

            const val PARAMETER_BORNCOUNT = "BornCount"
            private const val DEFAULT_BORNCOUNT = 1
        }
    }
}
