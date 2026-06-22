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

import com.group_finity.mascot.Mascot
import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.exception.VariableException
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle
import kotlin.reflect.KClass
import kotlin.reflect.cast

abstract class ActionBase(
    internal val schema: ResourceBundle,
    internal val animations: List<Animation>,
    internal val variables: VariableMap
) : Action {
    internal lateinit var mascot: Mascot
        private set
    internal open val animation
        get() = animations.firstOrNull { it.isEffective(variables) }
    internal val environment
        get() = mascot.environment

    private var startTime = 0
    var time
        get() = mascot.time - startTime
        set(value) {
            startTime = mascot.time - value
        }

    open val isDraggable
        get() = eval(schema.getString(PARAMETER_DRAGGABLE), Boolean::class, DEFAULT_DRAGGABLE)
    private val isEffective
        get() = eval(schema.getString(PARAMETER_CONDITION), Boolean::class, DEFAULT_CONDITION)
    private val duration
        get() = eval(schema.getString(PARAMETER_DURATION), Number::class, DEFAULT_DURATION).toInt()
    internal val affordance
        get() = eval(schema.getString(PARAMETER_AFFORDANCE), String::class, DEFAULT_AFFORDANCE)
    private val name
        get() = eval(schema.getString(PARAMETER_NAME), String::class, DEFAULT_NAME).takeUnless { it == DEFAULT_NAME }

    override fun init(mascot: Mascot) {
        this.mascot = mascot
        time = 0

        variables["mascot"] = mascot
        variables["action"] = this
        variables.init()

        for (animation in animations) {
            animation.init()
        }
    }

    override fun hasNext() = isEffective && time < duration

    override fun next() {
        initFrame()

        // Update affordances
        mascot.affordances.clear()
        if (affordance.isNotBlank()) {
            mascot.affordances.add(affordance)
        }

        // Update hotspots
        refreshHotspots()

        tick()
    }

    private fun initFrame() {
        variables.initFrame()

        for (animation in animations) {
            animation.initFrame()
        }
    }

    internal open fun refreshHotspots() {
        mascot.hotspots.clear()
        try {
            for (hotspot in animation?.hotspots.orEmpty()) {
                mascot.hotspots.add(hotspot)
            }
        } catch (_: VariableException) {
            mascot.hotspots.clear()
        }
    }

    internal abstract fun tick()

    internal fun putVariable(key: String, value: Any?) {
        synchronized(variables) {
            variables[key] = value
        }
    }

    internal fun <T : Any> eval(name: String, type: KClass<T>, defaultValue: T): T {
        synchronized(variables) {
            val variable = variables.rawMap[name]
            if (variable != null) return type.cast(variable.get(variables))
        }
        return defaultValue
    }

    override fun toString() = "Action (${this::class.java.simpleName}, $name)"

    companion object {
        const val PARAMETER_DURATION = "Duration"
        private const val DEFAULT_DURATION = Int.MAX_VALUE

        const val PARAMETER_CONDITION = "Condition"
        private const val DEFAULT_CONDITION = true

        const val PARAMETER_DRAGGABLE = "Draggable"
        private const val DEFAULT_DRAGGABLE = true

        const val PARAMETER_AFFORDANCE = "Affordance"
        private const val DEFAULT_AFFORDANCE = ""

        const val PARAMETER_NAME = "Name"
        private const val DEFAULT_NAME = "null"
    }
}
