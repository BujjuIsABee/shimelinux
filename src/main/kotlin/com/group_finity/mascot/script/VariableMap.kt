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

package com.group_finity.mascot.script

import java.util.AbstractMap
import javax.script.Bindings

class VariableMap : Bindings {
    val rawMap = linkedMapOf<String, Variable>()

    fun init() {
        for (variable in rawMap.values) {
            variable.init()
        }
    }

    fun initFrame() {
        for (variable in rawMap.values) {
            variable.initFrame()
        }
    }

    //region Bindings Implementation
    override val keys
        get() = rawMap.keys
    override val values
        get() = rawMap.keys.mapTo(mutableSetOf()) { get(it) }
    override val entries: MutableSet<MutableMap.MutableEntry<String, Any?>>
        get() = rawMap.entries.mapTo(mutableSetOf()) { AbstractMap.SimpleEntry(it.key, get(it.key)) }
    override val size
        get() = rawMap.size

    override fun isEmpty() = rawMap.isEmpty()

    override fun containsValue(value: Any?) = rawMap.containsValue(value)

    override fun containsKey(key: String) = rawMap.containsKey(key)

    override fun get(key: String) = rawMap[key]?.get(this)

    override fun remove(key: String) = rawMap.remove(key)

    override fun clear() {
        rawMap.clear()
    }

    override fun put(key: String, value: Any?) = if (value is Variable) {
        rawMap.put(key, value)
    } else {
        rawMap.put(key, Constant(value))
    }

    override fun putAll(entries: Map<out String, Any?>) {
        for (entry in entries) {
            put(entry.key, entry.value)
        }
    }

    operator fun set(key: String, value: Any?) {
        put(key, value)
    }
    //endregion
}
