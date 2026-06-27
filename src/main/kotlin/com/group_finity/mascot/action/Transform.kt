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
    animations: List<Animation>,
    params: VariableMap
) : Animate(schema, animations, params) {
    private val transformBehavior: String
        get() = eval(schema.getString(PARAMETER_TRANSFORMBEHAVIOR), DEFAULT_TRANSFORMBEHAVIOR)
    private val transformMascot: String
        get() = eval(schema.getString(PARAMETER_TRANSFORMMASCOT), DEFAULT_TRANSFORMMASCOT)

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
        } catch (e: IllegalStateException) {
            log.log(Level.SEVERE, "Fatal Error", e)
            Main.showError(Main.instance.languageBundle.getString("FailedCreateNewShimejiErrorMessage"), e)
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

        @get:JvmName("PARAMETER_TRANSFORMBEHAVIOUR")
        const val PARAMETER_TRANSFORMBEHAVIOR = "TransformBehavior"
        private const val DEFAULT_TRANSFORMBEHAVIOR = ""

        const val PARAMETER_TRANSFORMMASCOT = "TransformMascot"
        private const val DEFAULT_TRANSFORMMASCOT = ""
    }
}
