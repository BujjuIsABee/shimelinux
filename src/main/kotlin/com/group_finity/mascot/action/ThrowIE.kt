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

import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.getProperty
import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle

class ThrowIE(
    schema: ResourceBundle,
    animations: List<Animation>,
    params: VariableMap
) : Animate(schema, animations, params) {
    private val initialVx: Int
        get() = eval<Number>(schema.getString(PARAMETER_INITIALVX), DEFAULT_INITIALVX).toInt()
    private val initialVy: Int
        get() = eval<Number>(schema.getString(PARAMETER_INITIALVY), DEFAULT_INITIALVY).toInt()
    private val gravity: Double
        get() = eval<Number>(schema.getString(PARAMETER_GRAVITY), DEFAULT_GRAVITY).toDouble()

    override fun hasNext(): Boolean {
        val canThrow = getProperty("Throwing", true)
        return super.hasNext() && environment.activeIE.isVisible && canThrow
    }

    override fun tick() {
        super.tick()

        val activeIE = environment.activeIE
        if (activeIE.isVisible) {
            environment.moveActiveIE(
                if (mascot.isLookRight) {
                    Point(
                        activeIE.left + initialVx,
                        activeIE.top + initialVy + (time * gravity).toInt()
                    )
                } else {
                    Point(
                        activeIE.left - initialVx,
                        activeIE.top + initialVy + (time * gravity).toInt()
                    )
                }
            )
        }
    }

    companion object {
        const val PARAMETER_INITIALVX = "InitialVX"
        private const val DEFAULT_INITIALVX = 32

        const val PARAMETER_INITIALVY = "InitialVY"
        private const val DEFAULT_INITIALVY = -10

        const val PARAMETER_GRAVITY = "Gravity"
        private const val DEFAULT_GRAVITY = 0.5
    }
}
