/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle

class Sequence(
    schema: ResourceBundle,
    params: VariableMap,
    vararg actions: Action,
) : ComplexAction(schema, params, *actions) {
    private val isLoop
        get() = eval(schema.getString(PARAMETER_LOOP), Boolean::class, DEFAULT_LOOP)

    override var currentAction
        get() = super.currentAction
        set(value) { super.currentAction = if (isLoop) value % actions.size else value }

    override fun hasNext(): Boolean {
        seek()
        return super.hasNext()
    }

    companion object {
        const val PARAMETER_LOOP = "Loop"
        private const val DEFAULT_LOOP = false
    }
}
