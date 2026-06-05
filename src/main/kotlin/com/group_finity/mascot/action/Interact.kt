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

class Interact(
    schema: ResourceBundle,
    animations: ArrayList<Animation>,
    context: VariableMap,
) : Animate(schema, animations, context) {
    @Suppress("UNCHECKED_CAST")
    private val behavior: String?
        get() = eval(schema.getString(PARAMETER_BEHAVIOUR), String::class.java as Class<String?>, DEFAULT_BEHAVIOUR)

    override fun hasNext(): Boolean {
        return super.hasNext() && checkNotNull(mascot.manager).hasOverlappingMascotsAtPoint(mascot.anchor)
    }

    override fun tick() {
        super.tick()

        if ((time == checkNotNull(animation).duration - 1 || checkNotNull(animation).duration == 1) && (!checkNotNull(behavior).trim().isEmpty())) {
            try {
                mascot.behavior = checkNotNull(Main.instance.getConfiguration(mascot.imageSet)).buildBehavior(behavior!!, mascot)
            } catch (e: BehaviorInstantiationException) {
                log.log(Level.SEVERE, "Fatal Error", e)
                Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviourErrorMessage"), e)
            } catch (e: CantBeAliveException) {
                log.log(Level.SEVERE, "Fatal Error", e)
                Main.showError(Main.instance.languageBundle.getString("FailedSetBehaviourErrorMessage"), e)
            }
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        const val PARAMETER_BEHAVIOUR = "Behaviour"
        private val DEFAULT_BEHAVIOUR: String? = null
    }
}
