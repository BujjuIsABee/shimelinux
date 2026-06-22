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

import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle

class Offset(
    schema: ResourceBundle,
    params: VariableMap
) : InstantAction(schema, params) {
    private val offsetX
        get() = eval(schema.getString(PARAMETER_OFFSETX), Number::class, DEFAULT_OFFSETX).toInt()
    private val offsetY
        get() = eval(schema.getString(PARAMETER_OFFSETY), Number::class, DEFAULT_OFFSETY).toInt()

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
