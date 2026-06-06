/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.behavior

import com.group_finity.mascot.Main
import com.group_finity.mascot.Mascot
import com.group_finity.mascot.action.Action
import com.group_finity.mascot.action.ActionBase
import com.group_finity.mascot.config.Configuration
import com.group_finity.mascot.environment.MascotEnvironment
import com.group_finity.mascot.exception.BehaviorInstantiationException
import com.group_finity.mascot.exception.CantBeAliveException
import com.group_finity.mascot.exception.LostGroundException
import com.group_finity.mascot.exception.VariableException
import java.awt.Point
import java.awt.event.MouseEvent
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.SwingUtilities

class UserBehavior(private val name: String, private val action: Action, private val configuration: Configuration) : Behavior {
    private lateinit var mascot: Mascot
    internal val environment: MascotEnvironment
        get() = mascot.environment

    @Synchronized
    override fun init(mascot: Mascot) {
        this.mascot = mascot

        log.log(Level.INFO, "Behavior ($mascot,$this)")

        try {
            action.init(mascot)
            if (!action.hasNext()) {
                try {
                    mascot.behavior = configuration.buildNextBehavior(name, mascot)
                } catch (e: BehaviorInstantiationException) {
                    throw CantBeAliveException(Main.instance.languageBundle.getString("FailedInitialiseFollowingBehaviourErrorMessage"), e)
                }
            }
        } catch (e: VariableException) {
            throw CantBeAliveException(Main.instance.languageBundle.getString("VariableEvaluationErrorMessage"), e)
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
                for (hotspot in mascot.hotspots) {
                    if (hotspot.contains(mascot, checkNotNull(mascot.cursorPosition))) {
                        hotspotResult = HotspotResult.ACTIVE_NULL
                        try {
                            if (hotspot.behavior != null) {
                                hotspotResult = HotspotResult.ACTIVE
                                mascot.behavior = configuration.buildBehavior(hotspot.behavior, mascot)
                            }
                        } catch (e: BehaviorInstantiationException) {
                            throw CantBeAliveException(Main.instance.languageBundle.getString("FailedInitialiseFollowingBehaviourErrorMessage"), e)
                        }
                        break
                    }
                }

                if (hotspotResult == HotspotResult.INACTIVE) {
                    mascot.cursorPosition = null
                }
            }

            if (hotspotResult != HotspotResult.ACTIVE) {
                if (action.hasNext()) {
                    if ((mascot.bounds.x + mascot.bounds.width <= environment.screen.left) ||
                        (environment.screen.right <= mascot.bounds.x) ||
                        (environment.screen.bottom <= mascot.bounds.y)
                    ) {
                        log.log(Level.INFO, "Out of the screen bounds ($mascot,$this)")

                        if (Main.instance.properties.getProperty("Multiscreen", "true").toBoolean()) {
                            mascot.anchor = Point(
                                (Math.random() * (mascot.environment.screen.right - mascot.environment.screen.left)).toInt() + mascot.environment.screen.left,
                                mascot.environment.screen.top - 256
                            )
                        } else {
                            mascot.anchor = Point(
                                (Math.random() * (mascot.environment.workArea.right - mascot.environment.workArea.left)).toInt() + mascot.environment.workArea.left,
                                mascot.environment.workArea.top - 256
                            )
                        }

                        try {
                            mascot.behavior = configuration.buildBehavior((configuration.schema.getString(BEHAVIOURNAME_FALL)))
                        } catch (e: BehaviorInstantiationException) {
                            throw CantBeAliveException(Main.instance.languageBundle.getString("FailedFallingActionInitialiseErrorMessage"), e)
                        }
                    }
                } else {
                    log.log(Level.INFO, "Completed behavior ($mascot,$this)")

                    try {
                        mascot.behavior = configuration.buildNextBehavior(name, mascot)
                    } catch (e: BehaviorInstantiationException) {
                        throw CantBeAliveException(Main.instance.languageBundle.getString("FailedInitialiseFollowingActionsErrorMessage"), e)
                    }
                }
            }
        } catch (_: LostGroundException) {
            log.log(Level.INFO, "Lost ground ($mascot,$this)")

            try {
                mascot.cursorPosition = null
                mascot.isDragging = false
                mascot.behavior = configuration.buildBehavior(configuration.schema.getString(BEHAVIOURNAME_FALL))
            } catch (e: BehaviorInstantiationException) {
                throw CantBeAliveException(Main.instance.languageBundle.getString("FailedFallingActionInitialiseErrorMessage"), e)
            }
        } catch (e: VariableException) {
            throw CantBeAliveException(Main.instance.languageBundle.getString("VariableEvaluationErrorMessage"), e)
        }
    }

    @Synchronized
    override fun mousePressed(e: MouseEvent) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            var handled = false

            for (hotspot in mascot.hotspots) {
                if (hotspot.contains(mascot, e.point) &&
                    checkNotNull(Main.instance.getConfiguration(mascot.imageSet)).isBehaviorEnabled(hotspot.behavior, mascot)
                ) {
                    handled = true
                    try {
                        mascot.cursorPosition = e.point
                        if (hotspot.behavior != null) {
                            mascot.behavior = configuration.buildBehavior(hotspot.behavior, mascot)
                        }
                    } catch (e: BehaviorInstantiationException) {
                        throw CantBeAliveException(Main.instance.languageBundle.getString("FailedInitialiseFollowingBehaviourErrorMessage"), e)
                    }
                    break
                }
            }

            if (!handled && action is ActionBase) {
                try {
                    handled = !action.isDraggable
                } catch (e: VariableException) {
                    throw CantBeAliveException(Main.instance.languageBundle.getString("FailedDragActionInitialiseErrorMessage"), e)
                }
            }

            if (!handled) {
                try {
                    mascot.behavior = configuration.buildBehavior(configuration.schema.getString(BEHAVIOURNAME_DRAGGED))
                } catch (e: BehaviorInstantiationException) {
                    throw CantBeAliveException(Main.instance.languageBundle.getString("FailedDragActionInitialiseErrorMessage"), e)
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
                try {
                    mascot.isDragging = false
                    mascot.behavior = configuration.buildBehavior(configuration.schema.getString(BEHAVIOURNAME_THROWN))
                } catch (e: BehaviorInstantiationException) {
                    throw CantBeAliveException(Main.instance.languageBundle.getString("FailedDropActionInitialiseErrorMessage"), e)
                }
            }
        }
    }

    override fun toString(): String = "Behavior ($name)"

    enum class HotspotResult { INACTIVE, ACTIVE_NULL, ACTIVE }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        const val BEHAVIOURNAME_FALL = "Fall"
        const val BEHAVIOURNAME_DRAGGED = "Dragged"
        const val BEHAVIOURNAME_THROWN = "Thrown"
    }
}
