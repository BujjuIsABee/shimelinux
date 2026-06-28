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
import com.group_finity.mascot.environment.MascotEnvironment
import com.group_finity.mascot.exception.VariableException
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle

abstract class ActionBase(
    internal val schema: ResourceBundle,
    internal val animations: List<Animation>,
    internal val variables: VariableMap
) : Action {
    internal lateinit var mascot: Mascot
        private set
    internal val environment: MascotEnvironment
        get() = mascot.environment
    internal open val animation: Animation?
        get() = animations.firstOrNull { it.isEffective(variables) }
    var time = 0
        get() = mascot.time - field
        set(value) {
            field = mascot.time - value
        }

    open val isDraggable: Boolean
        get() = eval(schema.getString(PARAMETER_DRAGGABLE), DEFAULT_DRAGGABLE)
    private val isEffective: Boolean
        get() = eval(schema.getString(PARAMETER_CONDITION), DEFAULT_CONDITION)
    private val duration: Int
        get() = eval<Number>(schema.getString(PARAMETER_DURATION), DEFAULT_DURATION).toInt()
    internal val affordance: String
        get() = eval(schema.getString(PARAMETER_AFFORDANCE), DEFAULT_AFFORDANCE)
    private val name: String?
        get() = eval(schema.getString(PARAMETER_NAME), DEFAULT_NAME)

    override fun init(mascot: Mascot) {
        this.mascot = mascot
        this.time = 0

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
            mascot.hotspots.addAll(animation?.hotspots.orEmpty())
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

    internal inline fun <reified T> eval(name: String, defaultValue: T): T {
        synchronized(variables) {
            return variables.rawMap[name]?.let { it.get(variables) as T } ?: defaultValue
        }
    }

    override fun toString() = try {
        "Action (${this::class.java.simpleName}, $name)"
    } catch (_: VariableException) {
        "Action (${this::class.java.simpleName}, null)"
    }

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
        private val DEFAULT_NAME = null
    }
}
