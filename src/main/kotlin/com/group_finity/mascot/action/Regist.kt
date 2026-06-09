/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.Main
import com.group_finity.mascot.Mascot
import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.exception.LostGroundException
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.math.abs
import kotlin.math.roundToInt

class Regist(
    schema: ResourceBundle,
    animations: ArrayList<Animation>,
    context: VariableMap,
) : ActionBase(schema, animations, context) {
    private var scaling = 0.0

    private val offsetX: Int
        get() = eval(schema.getString(PARAMETER_OFFSETX), Number::class, DEFAULT_OFFSETX).toInt()
    private val offsetType: String
        get() = eval(schema.getString(PARAMETER_OFFSETTYPE), String::class, DEFAULT_OFFSETTYPE)

    override fun init(mascot: Mascot) {
        super.init(mascot)

        scaling = Main.instance.properties.getProperty("Scaling", "1.0").toDouble()
    }

    override fun hasNext(): Boolean {
        val offsetX = if (offsetType == schema.getString("Origin")) {
            0 - offsetX + checkNotNull(mascot.image).center.x
        } else {
            (offsetX * scaling).roundToInt()
        }
        return super.hasNext() && abs(environment.cursor.x - mascot.anchor.x + offsetX) < 5
    }

    override fun tick() {
        mascot.isDragging = true

        animation?.next(mascot, time)

        if (time + 1 >= checkNotNull(animation).duration) {
            mascot.isLookRight = Math.random() < 0.5

            log.log(Level.INFO, "Lost ground ($mascot,$this)")
            throw LostGroundException()
        }
    }

    override fun refreshHotspots() {
        // Action does not support hotspots
        mascot.hotspots.clear()
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        const val PARAMETER_OFFSETX = "OffsetX"
        private const val DEFAULT_OFFSETX = 0

        const val PARAMETER_OFFSETTYPE = "OffsetType"
        private const val DEFAULT_OFFSETTYPE = "ImageAnchor"
    }
}
