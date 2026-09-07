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

import org.w3c.dom.Attr
import org.w3c.dom.Element

/**
 * An XML node
 *
 * @author Yuki Yamada
 * @author Kilkakon
 * @author Bujju
 */
class Entry(private val element: Element) {
    /**
     * The name of the node
     *
     * @return "Node" for the following XML node:
     *
     * ```xml
     * <Node>Text</Node>
     * ```
     */
    val name: String
        get() = element.tagName

    /**
     * The text inside the node
     *
     * @return "Text" for the following XML node:
     *
     * ```xml
     * <Node>Text</Node>
     * ```
     */
    val text: String
        get() = element.textContent

    /**
     * The attributes of the node
     *
     * @return A [Map] where the key "name" corresponds to the value "value" for the following XML node:
     *
     * ```xml
     * <Node name="value" />
     * ```
     */
    val attributes = buildMap {
        val attributes = element.attributes
        for (i in 0 until attributes.length) {
            (attributes.item(i) as Attr).let { put(it.name, it.value) }
        }
    }

    /**
     * The child nodes inside the node
     *
     * @return A [List] with an [Entry] for the "Child" node:
     *
     * ```xml
     * <Node>
     *     <Child />
     * </Node>
     * ```
     */
    val children = buildList {
        val children = element.childNodes
        for (i in 0 until children.length) {
            (children.item(i) as? Element)?.let { add(Entry(it)) }
        }
    }

    private val childMap = children.groupBy { it.name }

    fun getAttribute(name: String) = element.getAttributeNode(name)?.value

    fun hasChild(name: String) = children.any { it.name == name }

    fun selectChildren(name: String) = childMap[name].orEmpty()
}
