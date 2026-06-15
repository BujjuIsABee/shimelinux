/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
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
