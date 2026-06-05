/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle

class Look(
    schema: ResourceBundle,
    params: VariableMap
) : InstantAction(schema, params) {
    private val isLookRight: Boolean
        get() = eval(schema.getString(PARAMETER_LOOKRIGHT), Boolean::class.java, !mascot.isLookRight)

    override fun apply() {
        mascot.isLookRight = isLookRight
    }

    companion object {
        const val PARAMETER_LOOKRIGHT = "LookRight"
    }
}
