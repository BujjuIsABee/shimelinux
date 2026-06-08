/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.Main
import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.ResourceBundle
import java.util.logging.Logger

class ThrowIE(
    schema: ResourceBundle,
    animations: ArrayList<Animation>,
    params: VariableMap,
) : Animate(schema, animations, params) {
    private val initialVx: Int
        get() = eval(schema.getString(PARAMETER_INITIALVX), Number::class, DEFAULT_INITIALVX).toInt()
    private val initialVy: Int
        get() = eval(schema.getString(PARAMETER_INITIALVY), Number::class, DEFAULT_INITIALVY).toInt()
    private val gravity: Double
        get() = eval(schema.getString(PARAMETER_GRAVITY), Number::class, DEFAULT_GRAVITY).toDouble()

    override fun hasNext(): Boolean {
        if (!Main.instance.properties.getProperty("Throwing", "true").toBoolean()) {
            return false
        }

        return super.hasNext() && environment.activeIE.isVisible
    }

    override fun tick() {
        super.tick()

        val activeIE = environment.activeIE

        if (activeIE.isVisible) {
            if (mascot.isLookRight) {
                environment.moveActiveIE(Point(
                    activeIE.left + initialVx,
                    activeIE.top + initialVy + (time * gravity).toInt()
                ))
            } else {
                environment.moveActiveIE(Point(
                    activeIE.left - initialVx,
                    activeIE.top + initialVy + (time * gravity).toInt()
                ))
            }
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)

        const val PARAMETER_INITIALVX = "InitialVX"
        private const val DEFAULT_INITIALVX = 32

        const val PARAMETER_INITIALVY = "InitialVY"
        private const val DEFAULT_INITIALVY = -10

        const val PARAMETER_GRAVITY = "Gravity"
        private const val DEFAULT_GRAVITY = 0.5
    }
}
