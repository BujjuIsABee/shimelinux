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

class Transform(
    schema: ResourceBundle,
    animations: ArrayList<Animation>,
    params: VariableMap,
) : Animate(schema, animations, params) {
    private val transformBehavior: String
        get() = eval(schema.getString(PARAMETER_TRANSFORMBEHAVIOUR), String::class, DEFAULT_TRANSFORMBEHAVIOUR)
    private val transformMascot: String
        get() = eval(schema.getString(PARAMETER_TRANSFORMMASCOT), String::class, DEFAULT_TRANSFORMMASCOT)

    override fun tick() {
        super.tick()

        if ((time == checkNotNull(animation).duration - 1 || checkNotNull(animation).duration == 1) && Main.instance.properties.getProperty("Transformation", "true").toBoolean()) {
            transform()
        }
    }

    private fun transform() {
        val childType = if (Main.instance.getConfiguration(transformMascot) != null) transformMascot else mascot.imageSet

        try {
            mascot.imageSet = childType
            mascot.behavior = checkNotNull(Main.instance.getConfiguration(childType)).buildBehavior(transformMascot, mascot)
        } catch (e: BehaviorInstantiationException) {
            log.log(Level.SEVERE, "Fatal Error", e)
            Main.showError(Main.instance.languageBundle.getString("FailedCreateNewShimejiErrorMessage"), e)
        } catch (e: CantBeAliveException) {
            log.log(Level.SEVERE, "Fatal Error", e)
            Main.showError(Main.instance.languageBundle.getString("FailedCreateNewShimejiErrorMessage"), e)
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        const val PARAMETER_TRANSFORMBEHAVIOUR = "TransformBehaviour"
        private const val DEFAULT_TRANSFORMBEHAVIOUR = ""

        const val PARAMETER_TRANSFORMMASCOT = "TransformMascot"
        private const val DEFAULT_TRANSFORMMASCOT = ""
    }
}
