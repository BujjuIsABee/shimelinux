/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
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

class BehaviorBuilder(private val configuration: Configuration, behaviorNode: Entry, private val conditions: MutableList<String?>) {
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

        isToggleable = if (
            name == UserBehavior.BEHAVIOURNAME_FALL ||
            name == UserBehavior.BEHAVIOURNAME_THROWN ||
            name == UserBehavior.BEHAVIOURNAME_DRAGGED
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
            loadBehaviors(nextList, ArrayList())
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
        } catch (_: ActionInstantiationException) {
            log.log(Level.SEVERE, "Failed to initialize the corresponding action ($this)")
            throw BehaviorInstantiationException(Main.instance.languageBundle.getString("FailedInitialiseCorrespondingActionErrorMessage") + " ($this)")
        }
    }

    fun isEffective(context: VariableMap): Boolean {
        if (frequency == 0) return false
        for (condition in conditions) {
            if (condition != null && !(Variable.parse(condition)!!.get(context) as Boolean)) {
                return false
            }
        }
        return true
    }

    override fun toString() = "Behavior ($name,$frequency,$actionName)"

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
    }
}
