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
