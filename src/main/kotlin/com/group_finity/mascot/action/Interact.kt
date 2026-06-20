/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.Main
import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.exception.BehaviorInstantiationException
import com.group_finity.mascot.exception.CantBeAliveException
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

@Suppress("UNUSED")
class Interact(
    schema: ResourceBundle,
    animations: List<Animation>,
    context: VariableMap,
) : Animate(schema, animations, context) {
    private val behavior
        get() = eval(schema.getString(PARAMETER_BEHAVIOUR), String::class, DEFAULT_BEHAVIOUR)

    override fun hasNext() = super.hasNext() && checkNotNull(mascot.manager).hasOverlappingMascotsAtPoint(mascot.anchor)

    override fun tick() {
        super.tick()

        animation?.let { animation ->
            if ((time == animation.duration - 1 || animation.duration == 1) && (!behavior.trim().isEmpty())) {
                try {
                    mascot.behavior = checkNotNull(Main.instance.getConfiguration(mascot.imageSet)).buildBehavior(
                        behavior,
                        mascot
                    )
                } catch (e: BehaviorInstantiationException) {
                    log.log(Level.SEVERE, "Fatal Error", e)
                    Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviourErrorMessage"), e)
                } catch (e: CantBeAliveException) {
                    log.log(Level.SEVERE, "Fatal Error", e)
                    Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviourErrorMessage"), e)
                }
            }
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        const val PARAMETER_BEHAVIOUR = "Behaviour"
        private const val DEFAULT_BEHAVIOUR = ""
    }
}
