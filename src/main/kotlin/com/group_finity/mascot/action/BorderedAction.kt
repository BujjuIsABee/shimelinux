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
import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.environment.Border
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle

abstract class BorderedAction(
    schema: ResourceBundle,
    animations: List<Animation>,
    context: VariableMap
) : ActionBase(schema, animations, context) {
    internal var border: Border? = null
        private set

    private val borderType
        get() = eval(schema.getString(PARAMETER_BORDERTYPE), String::class, DEFAULT_BORDERTYPE).takeUnless { it == DEFAULT_BORDERTYPE }

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
        border?.let {
            mascot.anchor = it.move(mascot.anchor)
        }
    }

    companion object {
        const val PARAMETER_BORDERTYPE = "BorderType"
        private const val DEFAULT_BORDERTYPE = "null"

        const val BORDERTYPE_CEILING = "Ceiling"
        const val BORDERTYPE_WALL = "Wall"
        const val BORDERTYPE_FLOOR = "Floor"
    }
}
