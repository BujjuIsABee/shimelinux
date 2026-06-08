/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.Mascot
import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.environment.MascotEnvironment
import com.group_finity.mascot.exception.VariableException
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle
import java.util.logging.Logger
import kotlin.reflect.KClass
import kotlin.reflect.cast

abstract class ActionBase(
    internal val schema: ResourceBundle,
    internal val animations: ArrayList<Animation>,
    internal val variables: VariableMap,
) : Action {
    internal lateinit var mascot: Mascot
        private set

    private var startTime = 0
    var time: Int
        get() = mascot.time - startTime
        set(value) {
            startTime = mascot.time - value
        }

    open val isDraggable: Boolean
        get() = eval(schema.getString(PARAMETER_DRAGGABLE), Boolean::class, DEFAULT_DRAGGABLE)
    private val isEffective: Boolean
        get() = eval(schema.getString(PARAMETER_CONDITION), Boolean::class, DEFAULT_CONDITION)
    private val duration: Int
        get() = eval(schema.getString(PARAMETER_DURATION), Number::class, DEFAULT_DURATION).toInt()
    internal val affordance: String
        get() = eval(schema.getString(PARAMETER_AFFORDANCE), String::class, DEFAULT_AFFORDANCE)

    private val name: String?
        get() {
            val result = eval(schema.getString("Name"), String::class, "null")
            return if (result == "null") null else result
        }

    internal open val animation: Animation?
        get() {
            for (animation in animations) {
                if (animation.isEffective(variables)) {
                    return animation
                }
            }
            return null
        }

    internal val environment: MascotEnvironment
        get() = mascot.environment

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

    override fun hasNext(): Boolean {
        val isEffective = this.isEffective
        val isInTime = time < duration
        return isEffective && isInTime
    }

    override fun next() {
        initFrame()

        mascot.affordances.clear()
        if (!affordance.trim().isEmpty()) {
            mascot.affordances.add(affordance)
        }

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
            if (animation != null) {
                for (hotspot in animation!!.hotspots) {
                    mascot.hotspots.add(hotspot)
                }
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

    internal fun <T : Any> eval(name: String, type: KClass<T>, defaultValue: T) : T {
        synchronized(variables) {
            val variable = variables.rawMap[name]
            if (variable != null) {
                return type.cast(variable.get(variables))
            }
        }
        return defaultValue
    }

    override fun toString(): String = "Action (${this::class.java.simpleName},$name)"

    companion object {
        const val PARAMETER_DURATION = "Duration"
        private const val DEFAULT_DURATION = Int.MAX_VALUE

        const val PARAMETER_CONDITION = "Condition"
        private const val DEFAULT_CONDITION = true

        const val PARAMETER_DRAGGABLE = "Draggable"
        private const val DEFAULT_DRAGGABLE = true

        const val PARAMETER_AFFORDANCE = "Affordance"
        private const val DEFAULT_AFFORDANCE = ""
    }
}
