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
import com.group_finity.mascot.getProperty
import com.group_finity.mascot.localize
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.roundToInt

private val logger: Logger = Logger.getLogger(Breed.Delegate::class.java.name)

/**
 * An action that creates a new mascot.
 *
 * @author Yuki Yamada
 * @author Shimeji-ee Group
 * @author Bujju
 */
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

    /**
     * Handles the shared functionality of breeding actions.
     *
     * @author LavenderSnek
     * @author Bujju
     */
    class Delegate(private val action: ActionBase) {
        /**
         * Whether breeding/transient behaviors are allowed.
         */
        val isEnabled: Boolean
            get() = getProperty(if (bornTransient) "Transients" else "Breeding", true)

        /**
         * Whether the current frame is a multiple of the [bornInterval].
         */
        val isIntervalFrame: Boolean
            get() = action.time % bornInterval == 0

        /**
         * Whether the current frame is the penultimate frame of the action's [animation].
         */
        val isPenultimateFrame: Boolean
            get() = action.time == checkNotNull(action.animation).duration - 1

        /**
         * The X position of the new mascot(s) relative to [mascot].
         */
        private val bornX: Int
            get() = action.eval<Number>(action.schema.getString(PARAMETER_BORNX), DEFAULT_BORNX).toInt()

        /**
         * The Y position of the new mascot(s) relative to [mascot].
         */
        private val bornY: Int
            get() = action.eval<Number>(action.schema.getString(PARAMETER_BORNY), DEFAULT_BORNY).toInt()

        /**
         * The initial behavior of the new mascot(s).
         */
        private val bornBehavior: String
            get() = action.eval(action.schema.getString(PARAMETER_BORNBEHAVIOUR), DEFAULT_BORNBEHAVIOUR)

        /**
         * The image set that will be used for the new mascot(s). If a valid image set is not provided, [mascot]'s image
         * set will be used.
         */
        private val bornMascot: String
            get() = action.eval(action.schema.getString(PARAMETER_BORNMASCOT), DEFAULT_BORNMASCOT)

        /**
         * Whether the new mascot(s) will be temporary.
         */
        private val bornTransient: Boolean
            get() = action.eval(action.schema.getString(PARAMETER_BORNTRANSIENT), DEFAULT_BORNTRANSIENT)

        /**
         * The number of ticks that must pass before a new mascot can be spawned.
         */
        private val bornInterval: Int
            get() = action.eval<Number>(action.schema.getString(PARAMETER_BORNINTERVAL), DEFAULT_BORNINTERVAL).toInt()

        /**
         * The number of mascots to spawn.
         */
        private val bornCount: Int
            get() = action.eval<Number>(action.schema.getString(PARAMETER_BORNCOUNT), DEFAULT_BORNCOUNT).toInt()

        /**
         * Spawns a new mascot.
         */
        fun breed() {
            val scaling = getProperty("Scaling", 1.0)
            val childType = bornMascot.takeIf { Main.hasConfiguration(it) } ?: action.mascot.imageSet

            repeat(bornCount) {
                val mascot = Mascot(childType)

                logger.info { "Mascot breeding (${action.mascot}, $action, $mascot)" }

                if (action.mascot.isLookRight) {
                    mascot.anchor.setLocation(
                        action.mascot.anchor.x - (bornX * scaling).roundToInt(),
                        action.mascot.anchor.y + (bornY * scaling).roundToInt()
                    )
                } else {
                    mascot.anchor.setLocation(
                        action.mascot.anchor.x + (bornX * scaling).roundToInt(),
                        action.mascot.anchor.y + (bornY * scaling).roundToInt()
                    )
                }

                mascot.isLookRight = action.mascot.isLookRight

                try {
                    mascot.behavior = Main.getConfiguration(childType).buildBehavior(bornBehavior, action.mascot)
                    checkNotNull(action.mascot.manager).add(mascot)
                } catch (e: Exception) {
                    when (e) {
                        is IllegalStateException,
                        is BehaviorInstantiationException,
                        is CantBeAliveException -> {
                            logger.log(Level.SEVERE, e) { "Failed to create new Shimeji" }
                            Main.showError(localize("FailedCreateNewShimejiErrorMessage"), e)
                            mascot.dispose()
                        }

                        else -> throw e
                    }
                }
            }
        }

        /**
         * Throws a [VariableException] if [bornCount] is not greater than zero.
         */
        fun validateBornCount() {
            if (bornCount < 1) {
                throw VariableException("BornCount must be positive")
            }
        }

        /**
         * Throws a [VariableException] if [bornInterval] is not greater than zero.
         */
        fun validateBornInterval() {
            if (bornInterval < 1) {
                throw VariableException("BornInterval must be positive")
            }
        }

        companion object {
            /**
             * The key for the name of the [bornX] parameter in the schema.
             */
            const val PARAMETER_BORNX = "BornX"
            private const val DEFAULT_BORNX = 0

            /**
             * The key for the name of the [bornY] parameter in the schema.
             */
            const val PARAMETER_BORNY = "BornY"
            private const val DEFAULT_BORNY = 0

            /**
             * The key for the name of the [bornBehavior] parameter in the schema.
             */
            const val PARAMETER_BORNBEHAVIOUR = "BornBehaviour"
            private const val DEFAULT_BORNBEHAVIOUR = ""

            /**
             * The key for the name of the [bornMascot] parameter in the schema.
             */
            const val PARAMETER_BORNMASCOT = "BornMascot"
            private const val DEFAULT_BORNMASCOT = ""

            /**
             * The key for the name of the [bornInterval] parameter in the schema.
             */
            const val PARAMETER_BORNINTERVAL = "BornInterval"
            private const val DEFAULT_BORNINTERVAL = 1

            /**
             * The key for the name of the [bornTransient] parameter in the schema.
             */
            const val PARAMETER_BORNTRANSIENT = "BornTransient"
            private const val DEFAULT_BORNTRANSIENT = false

            /**
             * The key for the name of the [bornCount] parameter in the schema.
             */
            const val PARAMETER_BORNCOUNT = "BornCount"
            private const val DEFAULT_BORNCOUNT = 1
        }
    }
}
