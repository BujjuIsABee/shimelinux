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

package com.group_finity.mascot.animation

import com.group_finity.mascot.Mascot
import com.group_finity.mascot.hotspot.Hotspot
import com.group_finity.mascot.script.Variable
import com.group_finity.mascot.script.VariableMap

/**
 * An animation for a mascot that can be played by an action.
 *
 * @param condition The condition that must be met for the animation to be played.
 * @param poses The frames of the animation.
 * @param hotspots The hotspots that can be interacted with during the animation.
 * @param isTurn Whether the animation is a turning animation.
 *
 * @author Yuki Yamada
 * @author Kilkakon
 * @author Bujju
 */
class Animation(
    private val condition: Variable,
    private val poses: Array<Pose>,
    val hotspots: Array<Hotspot>,
    val isTurn: Boolean
) {
    /**
     * The total duration of all the animation's poses.
     */
    val duration = poses.sumOf { it.duration }

    init {
        require(poses.isNotEmpty()) { "Animation requires at least one pose" }
    }

    /**
     * Whether the conditions for the animation to play are currently met.
     */
    fun isEffective(variables: VariableMap) = condition.get(variables) as Boolean

    /**
     * Initializes the animation.
     */
    fun init() {
        condition.init()
    }

    /**
     * Initializes the first frame of the animation.
     */
    fun initFrame() {
        condition.initFrame()
    }

    /**
     * Progresses the animation.
     */
    fun next(mascot: Mascot, time: Int) {
        checkNotNull(getPoseAt(time)).next(mascot)
    }

    /**
     * Gets which pose should be displayed at [time].
     */
    fun getPoseAt(time: Int): Pose? {
        var t = time % duration
        return poses.find {
            t -= it.duration
            t < 0
        }
    }
}
