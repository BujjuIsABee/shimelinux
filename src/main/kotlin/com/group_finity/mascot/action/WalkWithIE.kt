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

import com.group_finity.mascot.Main
import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.exception.LostGroundException
import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

class WalkWithIE(
    schema: ResourceBundle,
    animations: List<Animation>,
    params: VariableMap
) : Move(schema, animations, params) {
    private val offsetX
        get() = eval(schema.getString(PARAMETER_IEOFFSETX), Number::class, DEFAULT_IEOFFSETX).toInt()
    private val offsetY
        get() = eval(schema.getString(PARAMETER_IEOFFSETY), Number::class, DEFAULT_IEOFFSETY).toInt()

    override fun hasNext(): Boolean {
        if (!Main.instance.properties.getProperty("Throwing", "true").toBoolean()) return false
        return super.hasNext()
    }

    override fun tick() {
        val activeIE = environment.activeIE

        if (!activeIE.isVisible) {
            log.log(Level.INFO, "IE not visible ($mascot, $this)")
            throw LostGroundException()
        }

        if (mascot.isLookRight) {
            if (mascot.anchor.x - offsetX != activeIE.left ||
                mascot.anchor.y + offsetY != activeIE.bottom
            ) {
                log.log(Level.INFO, "Lost ground ($mascot, $this)")
                throw LostGroundException()
            }
        } else {
            if (mascot.anchor.x + offsetX != activeIE.right ||
                mascot.anchor.y + offsetY != activeIE.bottom
            ) {
                log.log(Level.INFO, "Lost ground ($mascot, $this)")
                throw LostGroundException()
            }
        }

        super.tick()

        if (activeIE.isVisible) {
            environment.moveActiveIE(
                if (mascot.isLookRight) {
                    Point(
                        mascot.anchor.x - offsetX,
                        mascot.anchor.y + offsetY - activeIE.height
                    )
                } else {
                    Point(
                        mascot.anchor.x + offsetX - activeIE.width,
                        mascot.anchor.y + offsetY - activeIE.height
                    )
                }
            )
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        const val PARAMETER_IEOFFSETX = "IeOffsetX"
        private const val DEFAULT_IEOFFSETX = 0

        const val PARAMETER_IEOFFSETY = "IeOffsetY"
        private const val DEFAULT_IEOFFSETY = 0
    }
}
