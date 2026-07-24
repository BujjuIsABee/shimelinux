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

package com.group_finity.mascot.behavior

import com.group_finity.mascot.Mascot
import com.group_finity.mascot.action.Action
import com.group_finity.mascot.action.ActionBase
import com.group_finity.mascot.config.Configuration
import com.group_finity.mascot.exception.BehaviorInstantiationException
import com.group_finity.mascot.exception.CantBeAliveException
import com.group_finity.mascot.exception.LostGroundException
import com.group_finity.mascot.exception.VariableException
import com.group_finity.mascot.getConfiguration
import com.group_finity.mascot.localize
import java.awt.event.MouseEvent
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.SwingUtilities

class UserBehavior(
    private val name: String,
    private val action: Action,
    private val configuration: Configuration
) : Behavior {
    private lateinit var mascot: Mascot
    internal val environment
        get() = mascot.environment

    @Synchronized
    override fun init(mascot: Mascot) {
        this.mascot = mascot

        log.log(Level.INFO, "Behavior ($mascot, $this)")

        try {
            action.init(mascot)
            if (!action.hasNext()) {
                mascot.behavior = configuration.buildNextBehavior(name, mascot)
            }
        } catch (e: VariableException) {
            throw CantBeAliveException("VariableEvaluationErrorMessage".localize(), e)
        } catch (e: BehaviorInstantiationException) {
            throw CantBeAliveException("FailedInitializeFollowingBehaviorErrorMessage".localize(), e)
        }
    }

    @Synchronized
    override fun next() {
        try {
            if (action.hasNext()) {
                action.next()
            }

            var hotspotResult = HotspotResult.INACTIVE
            if (mascot.isHotspotClicked) {
                val cursorPosition = checkNotNull(mascot.cursorPosition)
                for (hotspot in mascot.hotspots.filter { it.contains(mascot, cursorPosition) }) {
                    hotspotResult = if (hotspot.behavior == null) HotspotResult.ACTIVE else HotspotResult.ACTIVE_NULL

                    if (hotspot.behavior != null) {
                        try {
                            mascot.behavior = configuration.buildBehavior(hotspot.behavior, mascot)
                        } catch (e: BehaviorInstantiationException) {
                            throw CantBeAliveException("FailedInitializeFollowingBehaviorErrorMessage".localize(), e)
                        }
                    }

                    break
                }

                if (hotspotResult == HotspotResult.INACTIVE) {
                    mascot.cursorPosition = null
                }
            }

            if (hotspotResult != HotspotResult.ACTIVE) {
                if (action.hasNext()) {
                    if (mascot.bounds.x + mascot.bounds.width <= environment.screen.left ||
                        environment.screen.right <= mascot.bounds.x ||
                        environment.screen.bottom <= mascot.bounds.y
                    ) {
                        log.log(Level.INFO, "Out of the screen bounds ($mascot, $this)")

                        mascot.resetAnchor()

                        try {
                            mascot.behavior = configuration.buildBehavior(BEHAVIOR_FALL)
                        } catch (e: BehaviorInstantiationException) {
                            throw CantBeAliveException("FailedFallingActionInitializeErrorMessage".localize(), e)
                        }
                    }
                } else {
                    log.log(Level.INFO, "Completed behavior ($mascot, $this)")

                    try {
                        mascot.behavior = configuration.buildNextBehavior(name, mascot)
                    } catch (e: BehaviorInstantiationException) {
                        throw CantBeAliveException("FailedInitializeFollowingActionsErrorMessage".localize(), e)
                    }
                }
            }
        } catch (_: LostGroundException) {
            mascot.cursorPosition = null
            mascot.isDragging = false

            try {
                mascot.behavior = configuration.buildBehavior(configuration.schema.getString(BEHAVIOR_FALL))
            } catch (e: BehaviorInstantiationException) {
                throw CantBeAliveException("FailedFallingActionInitializeErrorMessage".localize(), e)
            }
        } catch (e: VariableException) {
            throw CantBeAliveException("VariableEvaluationErrorMessage".localize(), e)
        }
    }

    @Synchronized
    override fun mousePressed(e: MouseEvent) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            var handled = false

            for (hotspot in mascot.hotspots) {
                val behaviorEnabled = getConfiguration(mascot.imageSet).isBehaviorEnabled(hotspot.behavior, mascot)
                if (hotspot.contains(mascot, e.point) && behaviorEnabled) {
                    handled = true
                    mascot.cursorPosition = e.point

                    if (hotspot.behavior != null) {
                        try {
                            mascot.behavior = configuration.buildBehavior(hotspot.behavior, mascot)
                        } catch (e: BehaviorInstantiationException) {
                            throw CantBeAliveException("FailedInitializeFollowingBehaviorErrorMessage".localize(), e)
                        }
                    }

                    break
                }
            }

            if (!handled && action is ActionBase) {
                try {
                    handled = !action.isDraggable
                } catch (e: VariableException) {
                    throw CantBeAliveException("FailedDragActionInitializeErrorMessage".localize(), e)
                }
            }

            if (!handled) {
                try {
                    mascot.behavior = configuration.buildBehavior(configuration.schema.getString(BEHAVIOR_DRAGGED))
                } catch (e: BehaviorInstantiationException) {
                    throw CantBeAliveException("FailedDragActionInitializeErrorMessage".localize(), e)
                }
            }
        }
    }

    @Synchronized
    override fun mouseReleased(e: MouseEvent) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (mascot.isHotspotClicked) {
                mascot.cursorPosition = null
            }

            if (mascot.isDragging) {
                mascot.isDragging = false

                try {
                    mascot.behavior = configuration.buildBehavior(configuration.schema.getString(BEHAVIOR_THROWN))
                } catch (e: BehaviorInstantiationException) {
                    throw CantBeAliveException("FailedDropActionInitializeErrorMessage".localize(), e)
                }
            }
        }
    }

    override fun toString() = "Behavior ($name)"

    enum class HotspotResult { INACTIVE, ACTIVE_NULL, ACTIVE }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        const val BEHAVIOR_FALL = "Fall"
        const val BEHAVIOR_DRAGGED = "Dragged"
        const val BEHAVIOR_THROWN = "Thrown"
    }
}
