package com.group_finity.mascot.config

import com.group_finity.mascot.Main
import com.group_finity.mascot.action.Action
import com.group_finity.mascot.exception.ActionInstantiationException
import com.group_finity.mascot.exception.ConfigurationException
import java.util.Locale
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

class Configuration {
    private val constants = LinkedHashMap<String, String>(2)
    val actionBuilders = LinkedHashMap<String, ActionBuilder>()
    private val behaviorBuilders = LinkedHashMap<String, BehaviorBuilder>()
    private val information = LinkedHashMap<String, String>(8)
    lateinit var schema: ResourceBundle

    fun load(configurationNode: Entry, imageSet: String) {
        log.log(Level.INFO, "Reading configuration file")

        // Check for Japanese XML tag
        val locale = if (configurationNode.hasChild("\u52D5\u4F5C\u30EA\u30B9\u30C8") ||
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
            constants[constant.getAttribute(schema.getString("Name")) ?: throw ConfigurationException("Name is null")] =
                constant.getAttribute(schema.getString("Value")) ?: throw ConfigurationException("Value is null")
        }

        for (list in configurationNode.selectChildren(schema.getString("ActionList"))) {
            log.log(Level.INFO, "Reading action list")

            loadActions(list, imageSet)
        }

        for (list in configurationNode.selectChildren(schema.getString("BehaviourList"))) {
            log.log(Level.INFO, "Reading behavior list")

            loadBehaviors(list, ArrayList<String>())
        }

        for (list in configurationNode.selectChildren(schema.getString("Information"))) {
            log.log(Level.INFO, "Reading information list")

            loadInformation(list)
        }
    }

    private fun loadInformation(list: Entry) {

    }

    private fun loadBehaviors(list: Entry, conditions: List<String>) {

    }

    private fun loadActions(list: Entry, imageSet: String) {
        for (node in list.selectChildren(schema.getString("Action"))) {
            val action = ActionBuilder(this, node, imageSet)

            if (actionBuilders.containsKey(action.name)) {
                throw ConfigurationException(Main.instance.languageBundle.getString("DuplicateActionErrorMessage") + ": ${action.name}")
            }

            actionBuilders[action.name] = action
        }
    }

    fun buildAction(name: String, params: Map<String, String>): Action {
        val factory = actionBuilders[name] ?: throw ActionInstantiationException(Main.instance.languageBundle.getString("NoCorrespondingActionFoundErrorMessage") + ": $name")
        return factory.buildAction(params)
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
    }
}