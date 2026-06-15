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
class Transform(
    schema: ResourceBundle,
    animations: List<Animation>,
    params: VariableMap,
) : Animate(schema, animations, params) {
    private val transformBehavior
        get() = eval(schema.getString(PARAMETER_TRANSFORMBEHAVIOUR), String::class, DEFAULT_TRANSFORMBEHAVIOUR)
    private val transformMascot
        get() = eval(schema.getString(PARAMETER_TRANSFORMMASCOT), String::class, DEFAULT_TRANSFORMMASCOT)

    override fun tick() {
        super.tick()

        val canTransform = Main.instance.properties.getProperty("Transformation", "true").toBoolean()
        if (animation?.let { time == it.duration - 1 || it.duration == 1 } == true && canTransform) {
            transform()
        }
    }

    private fun transform() {
        val childType = transformMascot.takeUnless { Main.instance.getConfiguration(it) == null } ?: mascot.imageSet

        try {
            mascot.imageSet = childType
            mascot.behavior = checkNotNull(Main.instance.getConfiguration(childType)).buildBehavior(transformBehavior, mascot)
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
