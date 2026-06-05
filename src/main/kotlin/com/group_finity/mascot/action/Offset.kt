/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle

class Offset(
    schema: ResourceBundle,
    params: VariableMap,
) : InstantAction(schema, params) {
    private val offsetX: Int
        get() = eval(schema.getString(PARAMETER_OFFSETX), Number::class.java, DEFAULT_OFFSETX).toInt()
    private val offsetY: Int
        get() = eval(schema.getString(PARAMETER_OFFSETY), Number::class.java, DEFAULT_OFFSETY).toInt()

    override fun apply() {
        mascot.anchor = Point(
            mascot.anchor.x + offsetX,
            mascot.anchor.y + offsetY
        )
    }

    companion object {
        const val PARAMETER_OFFSETX = "X"
        private const val DEFAULT_OFFSETX = 0

        const val PARAMETER_OFFSETY = "Y"
        private const val DEFAULT_OFFSETY = 0
    }
}
