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
import com.group_finity.mascot.exception.LostGroundException
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle
import java.util.logging.Logger

private val logger = Logger.getLogger(Move::class.java.name)

/**
 * An action that causes the mascot to move
 *
 * @author Yuki Yamada
 * @author Kilkakon
 * @author Bujju
 */
open class Move(
    schema: ResourceBundle,
    animations: List<Animation>,
    context: VariableMap
) : BorderedAction(schema, animations, context) {
    internal open val hasTurningAnimation = animations.any { it.isTurn }
    internal var isTurning = false

    override val animation: Animation?
        get() = animations.firstOrNull { it.isEffective(variables) && isTurning == it.isTurn }

    /**
     * The X-position that the mascot moves towards
     */
    private val targetX: Int
        get() = eval<Number>(schema.getString(PARAMETER_TARGETX), DEFAULT_TARGETX).toInt()

    /**
     * The Y-position that the mascot moves towards
     */
    private val targetY: Int
        get() = eval<Number>(schema.getString(PARAMETER_TARGETY), DEFAULT_TARGETY).toInt()

    override fun hasNext(): Boolean {
        val targetX = targetX
        val targetY = targetY

        val hasReached = (targetX == Int.MIN_VALUE || mascot.anchor.x != targetX) && (targetY == Int.MIN_VALUE || mascot.anchor.y != targetY)

        return super.hasNext() && (isTurning || hasReached)
    }

    override fun tick() {
        super.tick()

        if (border?.isOn(mascot.anchor) == false) {
            logger.info { "Lost ground ($mascot, $this)" }
            throw LostGroundException()
        }

        val targetX = targetX
        val targetY = targetY

        var isDown = false

        if (targetX != DEFAULT_TARGETX && mascot.anchor.x != targetX) {
            // Activate turn animation when direction changes
            isTurning = hasTurningAnimation && (isTurning || mascot.anchor.x < targetX != mascot.isLookRight)
            mascot.isLookRight = mascot.anchor.x < targetX
        }

        if (targetY != DEFAULT_TARGETY) {
            isDown = mascot.anchor.y < targetY
        }

        val animation = checkNotNull(animation)

        // Check if turning animation has finished
        if (isTurning && time >= animation.duration) {
            isTurning = false
        }

        animation.next(mascot, time)

        if (targetX != DEFAULT_TARGETX) {
            if (mascot.isLookRight && mascot.anchor.x >= targetX || !mascot.isLookRight && mascot.anchor.x <= targetX) {
                mascot.anchor.x = targetX
            }
        }

        if (targetY != DEFAULT_TARGETY) {
            if (isDown && mascot.anchor.y >= targetY || !isDown && mascot.anchor.y <= targetY) {
                mascot.anchor.y = targetY
            }
        }
    }

    companion object {
        const val PARAMETER_TARGETX = "TargetX"
        private const val DEFAULT_TARGETX = Int.MAX_VALUE

        const val PARAMETER_TARGETY = "TargetY"
        private const val DEFAULT_TARGETY = Int.MAX_VALUE
    }
}
