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

import com.group_finity.mascot.Main
import com.group_finity.mascot.behavior.Behavior
import com.group_finity.mascot.behavior.UserBehavior
import com.group_finity.mascot.exception.ActionInstantiationException
import com.group_finity.mascot.exception.BehaviorInstantiationException
import com.group_finity.mascot.exception.ConfigurationException
import com.group_finity.mascot.script.Variable
import com.group_finity.mascot.script.VariableMap
import java.util.logging.Level
import java.util.logging.Logger

class BehaviorBuilder(
    private val configuration: Configuration,
    behaviorNode: Entry,
    private val conditions: MutableList<String?>
) {
    val name = checkNotNull(behaviorNode.getAttribute(configuration.schema.getString("Name")))
    private val actionName = behaviorNode.getAttribute(configuration.schema.getString("Action")) ?: name
    val frequency = checkNotNull(behaviorNode.getAttribute(configuration.schema.getString("Frequency"))).toInt()
    val isHidden = behaviorNode.getAttribute(configuration.schema.getString("Hidden")).toBoolean()
    val isToggleable: Boolean
    val isNextAdditive: Boolean
    val nextBehaviorBuilders = mutableListOf<BehaviorBuilder>()
    private val params = linkedMapOf<String, String>()

    init {
        log.log(Level.INFO, "Loading behavior: $this")

        conditions.add(behaviorNode.getAttribute(configuration.schema.getString("Condition")))

        isToggleable = if (name == UserBehavior.BEHAVIOR_FALL ||
            name == UserBehavior.BEHAVIOR_THROWN ||
            name == UserBehavior.BEHAVIOR_DRAGGED
        ) {
            false
        } else {
            behaviorNode.getAttribute(configuration.schema.getString("Toggleable")).toBoolean()
        }

        params.putAll(behaviorNode.attributes)
        params.remove(configuration.schema.getString("Name"))
        params.remove(configuration.schema.getString("Action"))
        params.remove(configuration.schema.getString("Frequency"))
        params.remove(configuration.schema.getString("Hidden"))
        params.remove(configuration.schema.getString("Condition"))
        params.remove(configuration.schema.getString("Toggleable"))

        var isNextAdditive = true

        for (nextList in behaviorNode.selectChildren(configuration.schema.getString("NextBehaviourList"))) {
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
            } else if (node.name == configuration.schema.getString("BehaviourReference")) {
                val behavior = BehaviorBuilder(configuration, node, conditions)
                nextBehaviorBuilders.add(behavior)
            }
        }
    }

    fun validate() {
        if (!configuration.actionBuilders.containsKey(actionName)) {
            log.log(Level.SEVERE, "There is no corresponding action ($this)")
            throw ConfigurationException(Main.instance.languageBundle.getString("NoActionFoundErrorMessage") + " ($this)")
        }
    }

    fun buildBehavior(): Behavior {
        try {
            return UserBehavior(name, configuration.buildAction(actionName, params), configuration)
        } catch (e: ActionInstantiationException) {
            log.log(Level.SEVERE, "Failed to initialize the corresponding action ($this)", e)
            throw BehaviorInstantiationException(Main.instance.languageBundle.getString("FailedInitialiseCorrespondingActionErrorMessage") + " ($this)", e)
        }
    }

    fun isEffective(context: VariableMap): Boolean {
        if (frequency == 0) return false
        for (condition in conditions) {
            if (condition != null && Variable.parse(condition)?.get(context) as? Boolean == false) return false
        }
        return true
    }

    override fun toString() = "Behavior ($name, $frequency, $actionName)"

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
    }
}
