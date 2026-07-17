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

package com.group_finity.mascot.config

import com.group_finity.mascot.behavior.Behavior
import com.group_finity.mascot.behavior.UserBehavior
import com.group_finity.mascot.exception.ActionInstantiationException
import com.group_finity.mascot.exception.BehaviorInstantiationException
import com.group_finity.mascot.exception.ConfigurationException
import com.group_finity.mascot.localize
import com.group_finity.mascot.script.Variable
import com.group_finity.mascot.script.VariableMap
import java.util.logging.Level
import java.util.logging.Logger

class BehaviorBuilder(
    private val configuration: Configuration,
    behaviorNode: Entry,
    private var conditions: MutableList<String?>
) {
    val name = requireNotNull(behaviorNode.getAttribute(configuration.schema.getString("Name"))) { "Behavior requires Name attribute." }
    private val actionName = behaviorNode.getAttribute(configuration.schema.getString("Action")) ?: name
    val frequency = requireNotNull(behaviorNode.getAttribute(configuration.schema.getString("Frequency"))) { "Behavior requires Frequency attribute." }.toInt()
    val isHidden = behaviorNode.getAttribute(configuration.schema.getString("Hidden")).toBoolean()
    val isToggleable: Boolean
    val isNextAdditive: Boolean
    val nextBehaviorBuilders = mutableListOf<BehaviorBuilder>()
    private val params = linkedMapOf<String, String>()

    init {
        log.log(Level.INFO, "Loading behavior: $this")

        conditions = conditions.toMutableList()
        conditions.add(behaviorNode.getAttribute(configuration.schema.getString("Condition")))

        isToggleable = when (name) {
            UserBehavior.BEHAVIOR_FALL, UserBehavior.BEHAVIOR_DRAGGED, UserBehavior.BEHAVIOR_THROWN -> false
            else -> behaviorNode.getAttribute(configuration.schema.getString("Toggleable")).toBoolean()
        }

        params.putAll(behaviorNode.attributes)
        params.keys.removeAll(
            setOf(
                configuration.schema.getString("Name"),
                configuration.schema.getString("Action"),
                configuration.schema.getString("Frequency"),
                configuration.schema.getString("Hidden"),
                configuration.schema.getString("Condition"),
                configuration.schema.getString("Toggleable")
            )
        )

        var isNextAdditive = true

        for (nextList in behaviorNode.selectChildren(configuration.schema.getString("NextBehaviorList"))) {
            log.log(Level.INFO, "Lists the following behaviors...")

            isNextAdditive = nextList.getAttribute(configuration.schema.getString("Add")).toBoolean()
            loadBehaviors(nextList, mutableListOf())
        }

        this.isNextAdditive = isNextAdditive

        log.log(Level.INFO, "Finished loading behavior: $this")
    }

    private fun loadBehaviors(list: Entry, conditions: MutableList<String?>) {
        for (node in list.children) {
            if (node.name == configuration.schema.getString("Condition")) {
                val newConditions = conditions.toMutableList()
                newConditions.add(node.getAttribute(configuration.schema.getString("Condition")))
                loadBehaviors(node, newConditions)
            } else if (node.name == configuration.schema.getString("BehaviorReference")) {
                val behavior = BehaviorBuilder(configuration, node, conditions)
                nextBehaviorBuilders.add(behavior)
            }
        }
    }

    fun validate() {
        if (!configuration.hasAction(actionName)) {
            log.log(Level.SEVERE, "There is no corresponding action ($this)")
            throw ConfigurationException("NoActionFoundErrorMessage".localize() + " ($this)")
        }
    }

    fun buildBehavior(): Behavior {
        try {
            return UserBehavior(name, configuration.buildAction(actionName, params), configuration)
        } catch (e: ActionInstantiationException) {
            log.log(Level.SEVERE, "Failed to initialize the corresponding action ($this)", e)
            throw BehaviorInstantiationException("FailedInitializeCorrespondingActionErrorMessage".localize() + " ($this)", e)
        }
    }

    fun isEffective(context: VariableMap): Boolean {
        if (frequency == 0) return false
        return conditions.none {
            Variable.parse(it)?.get(context) as? Boolean == false
        }
    }

    override fun toString() = "Behavior ($name, $frequency, $actionName)"

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
    }
}
