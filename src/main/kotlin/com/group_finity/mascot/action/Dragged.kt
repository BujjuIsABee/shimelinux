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
import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle
import kotlin.math.abs
import kotlin.math.roundToInt

class Dragged(
    schema: ResourceBundle,
    animations: List<Animation>,
    context: VariableMap
) : ActionBase(schema, animations, context) {
    private var footX = 0.0
    private var footDx = 0.0
    private var scaling = 0.0
    var timeToRegist = 0

    private val offsetX
        get() = eval(schema.getString(PARAMETER_OFFSETX), Number::class, DEFAULT_OFFSETX).toInt()
    private val offsetY
        get() = eval(schema.getString(PARAMETER_OFFSETY), Number::class, DEFAULT_OFFSETY).toInt()
    private val offsetType
        get() = eval(schema.getString(PARAMETER_OFFSETTYPE), String::class, DEFAULT_OFFSETTYPE)

    override fun init(mascot: Mascot) {
        super.init(mascot)

        scaling = Main.instance.properties.getProperty("Scaling", "1.0").toDouble()

        footX = (environment.cursor.x + (offsetX * scaling).roundToInt()).toDouble()
        timeToRegist = 250
    }

    override fun hasNext() = super.hasNext() && time < timeToRegist

    override fun tick() {
        mascot.isLookRight = false
        mascot.isDragging = true
        environment.refreshWorkArea()

        val cursor = environment.cursor

        var offsetX = (offsetX * scaling).roundToInt()
        var offsetY = (offsetY * scaling).roundToInt()
        if (offsetType == schema.getString("Origin")) {
            offsetX = 0 - offsetX + checkNotNull(mascot.image).center.x
            offsetY = 0 - offsetY + checkNotNull(mascot.image).center.y
        }

        if (abs(cursor.x - mascot.anchor.x + offsetX) >= 5) {
            time = 0
        }

        val newX = cursor.x

        footDx += ((newX - footX) * 0.1) * 0.8
        footX += footDx

        putVariable(schema.getString(VARIABLE_FOOTDX), footDx)
        putVariable(schema.getString(VARIABLE_FOOTX), footX)

        animation?.next(mascot, time)

        mascot.anchor = Point(
            cursor.x + offsetX,
            cursor.y + offsetY
        )

        if (time == timeToRegist - 1 && Math.random() >= 0.1) {
            timeToRegist++
        }
    }

    override fun refreshHotspots() {
        // Action does not support hotspots
        mascot.hotspots.clear()
    }

    companion object {
        const val PARAMETER_OFFSETX = "OffsetX"
        private const val DEFAULT_OFFSETX = 0

        const val PARAMETER_OFFSETY = "OffsetY"
        private const val DEFAULT_OFFSETY = 120

        const val PARAMETER_OFFSETTYPE = "OffsetType"
        private const val DEFAULT_OFFSETTYPE = "ImageAnchor"

        const val VARIABLE_FOOTX = "FootX"
        const val VARIABLE_FOOTDX = "FootDX"
    }
}
