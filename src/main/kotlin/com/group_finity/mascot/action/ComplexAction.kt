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

abstract class ComplexAction(
    schema: ResourceBundle,
    params: VariableMap,
    internal vararg val actions: Action
) : ActionBase(schema, listOf(), params) {
    internal open var currentAction = 0
        set(value) {
            field = value
            if (super.hasNext() && field < actions.size) {
                action.init(mascot)
            }
        }
    internal val action
        get() = actions[currentAction]

    init {
        require(actions.isNotEmpty())
    }

    override fun init(mascot: Mascot) {
        super.init(mascot)

        if (super.hasNext()) {
            currentAction = 0
            seek()
        }
    }

    override fun hasNext() = super.hasNext() && currentAction < actions.size && action.hasNext()

    override fun tick() {
        if (action.hasNext()) {
            action.next()
        }
    }

    override val isDraggable
        get() = if (currentAction < actions.size && actions[currentAction] is ActionBase) {
            (actions[currentAction] as ActionBase).isDraggable
        } else {
            true
        }

    internal fun seek() {
        if (super.hasNext()) {
            while (currentAction < actions.size) {
                if (action.hasNext()) break
                currentAction++
            }
        }
    }
}
