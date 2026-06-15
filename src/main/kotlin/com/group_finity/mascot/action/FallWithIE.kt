/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.Main
import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.exception.LostGroundException
import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

class FallWithIE(
    schema: ResourceBundle,
    animations: List<Animation>,
    context: VariableMap,
) : Fall(schema, animations, context) {
    private val offsetX
        get() = eval(schema.getString(PARAMETER_IEOFFSETX), Number::class, DEFAULT_IEOFFSETX).toInt()
    private val offsetY
        get() = eval(schema.getString(PARAMETER_IEOFFSETY), Number::class, DEFAULT_IEOFFSETY).toInt()

    override fun hasNext() = if (!Main.instance.properties.getProperty("Throwing", "true").toBoolean()) {
        false
    } else {
        super.hasNext()
    }

    override fun tick() {
        val activeIE = environment.activeIE
        if (!activeIE.isVisible) {
            log.log(Level.INFO, "IE not visible ($mascot,$this)")
            throw LostGroundException()
        }

        if (mascot.isLookRight) {
            if (mascot.anchor.x - offsetX != activeIE.left ||
                mascot.anchor.y + offsetY != activeIE.bottom
            ) {
                log.log(Level.INFO, "Lost ground ($mascot,$this)")
                throw LostGroundException()
            }
        } else {
            if (mascot.anchor.x + offsetX != activeIE.right ||
                mascot.anchor.y + offsetY != activeIE.bottom
            ) {
                log.log(Level.INFO, "Lost ground ($mascot,$this)")
                throw LostGroundException()
            }
        }

        super.tick()

        if (activeIE.isVisible) {
            if (mascot.isLookRight) {
                environment.moveActiveIE(Point(
                    mascot.anchor.x - offsetX,
                    mascot.anchor.y + offsetY - activeIE.height
                ))
            } else {
                environment.moveActiveIE(Point(
                    mascot.anchor.x + offsetX - activeIE.width,
                    mascot.anchor.y + offsetY - activeIE.height
                ))
            }
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        const val PARAMETER_IEOFFSETX = "IeOffsetX"
        private const val DEFAULT_IEOFFSETX = 0

        const val PARAMETER_IEOFFSETY = "IeOffsetY"
        private const val DEFAULT_IEOFFSETY = 0
    }
}
