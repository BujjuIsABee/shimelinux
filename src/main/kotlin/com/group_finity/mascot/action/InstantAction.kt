/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.Mascot
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle

abstract class InstantAction(
    schema: ResourceBundle,
    params: VariableMap
) : ActionBase(schema, ArrayList(), params) {
    override fun init(mascot: Mascot) {
        super.init(mascot)
        if (super.hasNext()) {
            apply()
        }
    }

    override fun hasNext(): Boolean = false

    override fun tick() {
    }

    internal abstract fun apply()
}
