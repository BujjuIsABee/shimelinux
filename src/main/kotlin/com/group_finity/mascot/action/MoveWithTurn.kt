/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle

@Deprecated("Deprecated in Shimeji-ee")
class MoveWithTurn(
    schema: ResourceBundle,
    animations: ArrayList<Animation>,
    params: VariableMap,
) : Move(schema, animations, params) {
    override val animation: Animation?
        get() {
            if (isTurning) {
                return animations[animations.size - 1]
            } else {
                for (index in 0 until animations.size - 1) {
                    if (animations[index].isEffective(variables)) {
                        return animations[index]
                    }
                }
            }
            return null
        }
    override val hasTurningAnimation: Boolean
        get() = true

    init {
        if (animations.size < 2) {
            throw IllegalArgumentException("Not enough animations")
        }
    }
}
