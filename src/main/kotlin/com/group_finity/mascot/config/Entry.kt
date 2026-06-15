/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.config

import org.w3c.dom.Attr
import org.w3c.dom.Element

class Entry(private val element: Element) {
    private val selected = hashMapOf<String, List<Entry>>()

    val name: String
        get() = element.tagName
    val text: String
        get() = element.textContent

    val attributes by lazy {
        val result = linkedMapOf<String, String>()
        val attrs = element.attributes
        for (i in 0 until attrs.length) {
            val attr = attrs.item(i) as Attr
            result[attr.name] = attr.value
        }
        return@lazy result
    }
    val children by lazy {
        val result = mutableListOf<Entry>()
        val childNodes = element.childNodes
        for (i in 0 until childNodes.length) {
            val childNode = childNodes.item(i)
            if (childNode is Element) {
                result.add(Entry(childNode))
            }
        }
        return@lazy result
    }

    fun getAttribute(name: String) = element.getAttributeNode(name)?.value

    fun hasChild(name: String) = children.any { it.name == name }

    fun selectChildren(name: String): List<Entry> {
        var result = selected[name]
        if (result != null) return result

        result = mutableListOf()
        for (child in children) {
            if (child.name == name) {
                result.add(child)
            }
        }

        selected[name] = result
        return result
    }
}
