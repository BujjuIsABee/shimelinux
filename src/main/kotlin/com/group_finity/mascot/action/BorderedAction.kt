/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.Mascot
import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.environment.Border
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle

abstract class BorderedAction(
    schema: ResourceBundle,
    animations: ArrayList<Animation>,
    context: VariableMap,
) : ActionBase(schema, animations, context) {
    internal var border: Border? = null
        private set

    private val borderType: String
        get() = eval(schema.getString(PARAMETER_BORDERTYPE), String::class, DEFAULT_BORDERTYPE)


    override fun init(mascot: Mascot) {
        super.init(mascot)

        border = when (borderType) {
            schema.getString(BORDERTYPE_CEILING) -> environment.ceiling
            schema.getString(BORDERTYPE_WALL) -> environment.wall
            schema.getString(BORDERTYPE_FLOOR) -> environment.floor
            else -> null
        }
    }

    override fun tick() {
        if (border != null) {
            mascot.anchor = border!!.move(mascot.anchor)
        }
    }

    companion object {
        const val PARAMETER_BORDERTYPE = "BorderType"
        private const val DEFAULT_BORDERTYPE: String = ""

        const val BORDERTYPE_CEILING = "Ceiling"
        const val BORDERTYPE_WALL = "Wall"
        const val BORDERTYPE_FLOOR = "Floor"
    }
}
