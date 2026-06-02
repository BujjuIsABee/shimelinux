/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.animation

import com.group_finity.mascot.Mascot
import com.group_finity.mascot.hotspot.Hotspot
import com.group_finity.mascot.script.Variable
import com.group_finity.mascot.script.VariableMap

class Animation(
    private val condition: Variable,
    private val poses: Array<Pose>,
    val hotspots: Array<Hotspot>,
    val isTurn: Boolean,
) {
    val duration: Int
        get() {
            var result = 0
            for (pose in poses) {
                result += pose.duration
            }
            return result
        }

    init {
        if (poses.isEmpty()) {
            throw IllegalArgumentException("Poses is empty")
        }
    }

    fun isEffective(variables: VariableMap): Boolean = condition.get(variables) as Boolean

    fun init() {
        condition.init()
    }

    fun initFrame() {
        condition.initFrame()
    }

    fun next(mascot: Mascot, time: Int) {
        checkNotNull(getPoseAt(time)).next(mascot)
    }

    fun getPoseAt(time: Int): Pose? {
        var t = time % duration
        for (pose in poses) {
            t -= pose.duration
            if (t < 0) {
                return pose
            }
        }
        return null
    }
}
