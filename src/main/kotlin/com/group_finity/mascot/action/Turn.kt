/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.exception.LostGroundException
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

class Turn(
    schema: ResourceBundle,
    animations: ArrayList<Animation>,
    params: VariableMap,
) : BorderedAction(schema, animations, params) {
    private var isTurning = false

    private val isLookRight: Boolean
        get() = eval(schema.getString(PARAMETER_LOOKRIGHT), Boolean::class.java, !mascot.isLookRight)

    override fun tick() {
        mascot.isLookRight = isLookRight

        super.tick()

        if (border != null && !border!!.isOn(mascot.anchor)) {
            log.log(Level.INFO, "Lost ground ($mascot,$this)")
            throw LostGroundException()
        }

        checkNotNull(animation).next(mascot, time)
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        const val PARAMETER_LOOKRIGHT = "LookRight"
    }
}
