/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.behavior

import com.group_finity.mascot.Mascot
import com.group_finity.mascot.action.Action
import com.group_finity.mascot.config.Configuration
import com.group_finity.mascot.environment.MascotEnvironment
import java.awt.event.MouseEvent
import java.util.logging.Level
import java.util.logging.Logger

class UserBehavior(private val name: String, private val action: Action, private val configuration: Configuration) : Behavior {
    private lateinit var mascot: Mascot

    @Synchronized
    override fun init(mascot: Mascot) {
        this.mascot = mascot

        log.log(Level.INFO, "Behavior ($mascot,$this)")
    }

    @Synchronized
    override fun next() {

    }

    @Synchronized
    override fun mousePressed(e: MouseEvent) {

    }

    @Synchronized
    override fun mouseReleased(e: MouseEvent) {

    }

    internal val environment: MascotEnvironment get() = mascot.environment

    override fun toString(): String = "Behavior ($name)"

    enum class HotspotResult { INACTIVE, INACTIVE_NULL, ACTIVE }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        const val BEHAVIORNAME_FALL = "Fall"
        const val BEHAVIORNAME_DRAGGED = "Dragged"
        const val BEHAVIORNAME_THROWN = "Thrown"
    }
}
