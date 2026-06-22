/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.Mascot
import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle

class BreedMove(
    schema: ResourceBundle,
    animations: List<Animation>,
    context: VariableMap
) : Move(schema, animations, context) {
    private val delegate = Breed.Delegate(this)

    override fun init(mascot: Mascot) {
        super.init(mascot)

        delegate.validateBornCount()
        delegate.validateBornInterval()
    }

    override fun tick() {
        super.tick()

        if (delegate.isIntervalFrame && !isTurning && delegate.isEnabled) {
            delegate.breed()
        }
    }
}
