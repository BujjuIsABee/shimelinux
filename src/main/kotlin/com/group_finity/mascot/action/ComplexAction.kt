/*
 * Copyright (c) 2026, Bujju
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *        following disclaimer.
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *        following disclaimer in the documentation and/or other materials provided with the distribution.
 *     3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
 *        products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
