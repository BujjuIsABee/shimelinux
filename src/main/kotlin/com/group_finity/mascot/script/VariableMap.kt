/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.script

import javax.script.Bindings

class VariableMap : AbstractMap<String, Any?>(), Bindings {
    val rawMap = LinkedHashMap<String, Variable>()

    override val keys: MutableSet<String>
        get() = rawMap.keys
    @Suppress("UNCHECKED_CAST")
    override val values: MutableSet<Any?>
        get() = rawMap.values as MutableSet<Any?>
    @Suppress("UNCHECKED_CAST")
    override val entries: MutableSet<MutableMap.MutableEntry<String, Any>>
        get() = rawMap as MutableSet<MutableMap.MutableEntry<String, Any>>

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

    override fun clear() {
        rawMap.clear()
    }

    override fun put(key: String, value: Any?): Any? = if (value is Variable) {
        rawMap.put(key, value)
    } else {
        rawMap.put(key, Constant(value))
    }

    override fun putAll(toMerge: Map<out String, Any?>) {
        for (entry in toMerge) {
            put(entry.key, entry.value)
        }
    }

    override fun remove(key: String?): Any? = rawMap.remove(key)
}
