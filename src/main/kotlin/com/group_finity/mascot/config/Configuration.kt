/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.config

import com.group_finity.mascot.Main
import com.group_finity.mascot.Mascot
import com.group_finity.mascot.action.Action
import com.group_finity.mascot.behavior.Behavior
import com.group_finity.mascot.behavior.UserBehavior
import com.group_finity.mascot.exception.ActionInstantiationException
import com.group_finity.mascot.exception.BehaviorInstantiationException
import com.group_finity.mascot.exception.ConfigurationException
import com.group_finity.mascot.exception.VariableException
import com.group_finity.mascot.script.VariableMap
import java.awt.Point
import java.util.Locale
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

class Configuration {
    private val constants = linkedMapOf<String, String>()
    val actionBuilders = linkedMapOf<String, ActionBuilder>()
    private val behaviorBuilders = linkedMapOf<String, BehaviorBuilder>()
    private val information = linkedMapOf<String, String>()
    lateinit var schema: ResourceBundle

    val behaviorNames
        get() = behaviorBuilders.keys

    fun load(configurationNode: Entry, imageSet: String) {
        log.log(Level.INFO, "Reading configuration file")

        // Check for Japanese XML tag
        val locale = if (
            configurationNode.hasChild("\u52D5\u4F5C\u30EA\u30B9\u30C8") ||
            configurationNode.hasChild("\u884C\u52D5\u30EA\u30B9\u30C8")
        ) {
            log.log(Level.INFO, "Using ja-JP schema")
            Locale.forLanguageTag("ja-JP")
        } else {
            log.log(Level.INFO, "Using en-US schema")
            Locale.forLanguageTag("en-US")
        }

        schema = ResourceBundle.getBundle("conf.schema", locale)

        for (constant in configurationNode.selectChildren(schema.getString("Constant"))) {
            val key = checkNotNull(constant.getAttribute(schema.getString("Name")))
            val value = checkNotNull(constant.getAttribute(schema.getString("Value")))
            constants[key] = value
        }

        for (list in configurationNode.selectChildren(schema.getString("ActionList"))) {
            log.log(Level.INFO, "Reading action list")

            loadActions(list, imageSet)
        }

        for (list in configurationNode.selectChildren(schema.getString("BehaviourList"))) {
            log.log(Level.INFO, "Reading behavior list")

            loadBehaviors(list, ArrayList())
        }

        for (list in configurationNode.selectChildren(schema.getString("Information"))) {
            log.log(Level.INFO, "Reading information list")

            loadInformation(list)
        }
    }

    private fun loadActions(list: Entry, imageSet: String) {
        for (node in list.selectChildren(schema.getString("Action"))) {
            val action = ActionBuilder(this, node, imageSet)

            if (actionBuilders.containsKey(action.name)) {
                throw ConfigurationException(Main.instance.languageBundle.getString("DuplicateActionErrorMessage") + ": ${action.name}")
            }

            actionBuilders[checkNotNull(action.name)] = action
        }
    }

    private fun loadBehaviors(list: Entry, conditions: MutableList<String?>) {
        for (node in list.children) {
            if (node.name == schema.getString("Condition")) {
                val newConditions = conditions.toMutableList()
                newConditions.add(node.getAttribute(schema.getString("Condition")))

                loadBehaviors(node, newConditions)
            } else if (node.name == schema.getString("Behaviour")) {
                val behavior = BehaviorBuilder(this, node, conditions)
                behaviorBuilders[behavior.name] = behavior
            }
        }
    }

    private fun loadInformation(list: Entry) {
        for (node in list.children) {
            if (node.name == schema.getString("Name") ||
                node.name == schema.getString("PreviewImage") ||
                node.name == schema.getString("SplashImage")
            ) {
                information[node.name] = node.text
            } else if (
                node.name == schema.getString("Artist") ||
                node.name == schema.getString("Scripter") ||
                node.name == schema.getString("Commissioner") ||
                node.name == schema.getString("Support")
            ) {
                val nameText = node.getAttribute(schema.getString("Name"))
                val linkText = node.getAttribute(schema.getString("URL"))

                if (nameText != null) {
                    information[node.name + schema.getString("Name")] = nameText
                    if (linkText != null) {
                        information[node.name + schema.getString("URL")] = linkText
                    }
                }
            }
        }
    }

    fun validate() {
        for (builder in actionBuilders.values) {
            builder.validate()
        }
        for (builder in behaviorBuilders.values) {
            builder.validate()
        }
    }

    fun buildAction(name: String, params: Map<String, String>): Action {
        val factory = actionBuilders[name]
            ?: throw ActionInstantiationException(Main.instance.languageBundle.getString("NoCorrespondingActionFoundErrorMessage") + ": $name")
        return factory.buildAction(params)
    }

    fun buildBehavior(name: String, mascot: Mascot): Behavior {
        val factory = behaviorBuilders[name]
        if (factory != null) {
            if (isBehaviorEnabled(name, mascot)) {
                return factory.buildBehavior()
            } else {
                if (Main.instance.properties.getProperty("Multiscreen", "true").toBoolean()) {
                    mascot.anchor = Point(
                        (Math.random() * mascot.environment.screen.width).toInt() + mascot.environment.screen.left,
                        mascot.environment.screen.top - 256
                    )
                } else {
                    mascot.anchor = Point(
                        (Math.random() * mascot.environment.workArea.width).toInt() + mascot.environment.workArea.left,
                        mascot.environment.workArea.top - 256
                    )
                }
                return buildBehavior(schema.getString(UserBehavior.BEHAVIOURNAME_FALL))
            }
        } else {
            throw BehaviorInstantiationException(Main.instance.languageBundle.getString("NoBehaviourFoundErrorMessage") + " ($name)")
        }
    }

    fun buildBehavior(name: String) = behaviorBuilders[name]?.buildBehavior()
        ?: throw BehaviorInstantiationException(Main.instance.languageBundle.getString("NoBehaviourFoundErrorMessage") + " ($name)")

    fun buildNextBehavior(previousName: String?, mascot: Mascot): Behavior? {
        val context = VariableMap()
        context.putAll(constants)
        context["mascot"] = mascot

        val candidates = mutableListOf<BehaviorBuilder>()
        var totalFrequency = 0L
        for (behaviorFactory in behaviorBuilders.values) {
            try {
                if (behaviorFactory.isEffective(context) && isBehaviorEnabled(behaviorFactory, mascot)) {
                    candidates.add(behaviorFactory)
                    totalFrequency += behaviorFactory.frequency
                }
            } catch (e: VariableException) {
                log.log(Level.WARNING, "An error occurred calculating the frequency of the action", e)
            }
        }

        if (previousName != null) {
            val previousBehaviorFactory = checkNotNull(behaviorBuilders[previousName])
            if (!previousBehaviorFactory.isNextAdditive) {
                totalFrequency = 0L
                candidates.clear()
            }

            for (behaviorFactory in previousBehaviorFactory.nextBehaviorBuilders) {
                try {
                    if (behaviorFactory.isEffective(context) && isBehaviorEnabled(behaviorFactory, mascot)) {
                        candidates.add(behaviorFactory)
                        totalFrequency += behaviorFactory.frequency
                    }
                } catch (e: VariableException) {
                    log.log(Level.WARNING, "An error occurred calculating the frequency of the action", e)
                }
            }
        }

        if (totalFrequency == 0L) {
            val fallbackCandidates = behaviorBuilders.values.filter {
                it.isEffective(context) && isBehaviorEnabled(it, mascot)
            }

            if (fallbackCandidates.isNotEmpty()) {
                candidates.addAll(fallbackCandidates)
            } else {
                if (Main.instance.properties.getProperty("Multiscreen", "true").toBoolean()) {
                    mascot.anchor = Point(
                        (Math.random() * (mascot.environment.screen.right - mascot.environment.screen.left)).toInt() + mascot.environment.screen.left,
                        mascot.environment.screen.top - 256
                    )
                } else {
                    mascot.anchor = Point(
                        (Math.random() * (mascot.environment.workArea.right - mascot.environment.workArea.left)).toInt() + mascot.environment.workArea.left,
                        mascot.environment.workArea.top - 256
                    )
                }
                return buildBehavior(schema.getString(UserBehavior.BEHAVIOURNAME_FALL))
            }
        }

        var random = Math.random() * totalFrequency

        for (behaviorFactory in candidates) {
            random -= behaviorFactory.frequency
            if (random < 0) {
                return behaviorFactory.buildBehavior()
            }
        }

        return null
    }

    fun isBehaviorEnabled(builder: BehaviorBuilder, mascot: Mascot): Boolean {
        if (builder.isToggleable) {
            for (behavior in Main.instance.properties.getProperty("DisabledBehaviours." + mascot.imageSet, "").split("/")) {
                if (behavior == builder.name) {
                    return false
                }
            }
        }
        return true
    }

    fun isBehaviorEnabled(name: String?, mascot: Mascot) = behaviorBuilders[name]?.let { isBehaviorEnabled(it, mascot) } == true

    fun isBehaviorHidden(name: String?) = behaviorBuilders[name]?.isHidden == true

    fun isBehaviorToggleable(name: String?) = behaviorBuilders[name]?.isToggleable == true

    fun containsInformationKey(key: String?) = information.containsKey(key)

    fun getInformation(key: String) = information[key]

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
    }
}
