/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.config

import com.group_finity.mascot.Main
import com.group_finity.mascot.action.Action
import com.group_finity.mascot.exception.ConfigurationException
import java.util.logging.Level
import java.util.logging.Logger

class ActionRef(private val configuration: Configuration, refNode: Entry) : IActionBuilder {
    private val name = checkNotNull(refNode.getAttribute(configuration.schema.getString("Name")))
    private val params = LinkedHashMap<String, String>()

    init {
        params.putAll(refNode.attributes)
    }

    override fun validate() {
        if (!configuration.actionBuilders.containsKey(name)) {
            log.log(Level.SEVERE, "There is no corresponding behavior: $this")
            throw ConfigurationException(Main.instance.languageBundle.getString("NoBehaviourFoundErrorMessage") + ": $this")
        }
    }

    override fun buildAction(params: Map<String, String>): Action {
        val newParams = LinkedHashMap<String, String>(params)
        newParams.putAll(this.params)
        return configuration.buildAction(name, newParams)
    }

    override fun toString(): String = "Action ($name)"

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
    }
}
