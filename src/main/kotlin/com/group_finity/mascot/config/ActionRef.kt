/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.config

import com.group_finity.mascot.Main
import com.group_finity.mascot.action.Action
import com.group_finity.mascot.exception.ConfigurationException
import java.util.logging.Level
import java.util.logging.Logger

class ActionRef(private val configuration: Configuration, refNode: Entry) : IActionBuilder {
    private val name = checkNotNull(refNode.getAttribute(configuration.schema.getString("Name")))
    private val params = linkedMapOf<String, String>()

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
        val newParams = params.toMutableMap()
        newParams.putAll(this.params)
        return configuration.buildAction(name, newParams)
    }

    override fun toString() = "Action ($name)"

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
    }
}
