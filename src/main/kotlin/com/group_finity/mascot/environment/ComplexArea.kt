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

package com.group_finity.mascot.environment

import java.awt.Point
import java.awt.Rectangle

class ComplexArea {
    private val areaMap = hashMapOf<String, Area>()
    val areas
        get() = areaMap.values

    fun set(rectangles: Map<String, Rectangle>) {
        retain(rectangles.keys)
        for (entry in rectangles.entries) {
            set(entry.key, entry.value)
        }
    }

    fun set(name: String, value: Rectangle) {
        for (area in areaMap.values) {
            if (area.left == value.x &&
                area.top == value.y &&
                area.width == value.width &&
                area.height == value.height
            ) {
                return
            }
        }

        var area = areaMap[name]
        if (area == null) {
            area = Area()
            areaMap[name] = area
        }
        area.set(value)
    }

    fun retain(deviceNames: Collection<String>) {
        areaMap.entries.removeIf { !deviceNames.contains(it.key) }
    }

    fun getBottomBorder(location: Point): FloorCeiling? {
        var result: FloorCeiling? = null
        for (area in areaMap.values) {
            if (area.bottomBorder.isOn(location)) {
                result = area.bottomBorder
            }
        }
        for (area in areaMap.values) {
            if (area.topBorder.isOn(location)) {
                result = null
            }
        }
        return result
    }

    fun getTopBorder(location: Point): FloorCeiling? {
        var result: FloorCeiling? = null
        for (area in areaMap.values) {
            if (area.topBorder.isOn(location)) {
                result = area.topBorder
            }
        }
        for (area in areaMap.values) {
            if (area.bottomBorder.isOn(location)) {
                result = null
            }
        }
        return result
    }

    fun getLeftBorder(location: Point): Wall? {
        var result: Wall? = null
        for (area in areaMap.values) {
            if (area.leftBorder.isOn(location)) {
                result = area.rightBorder // i think this should be left border but i won't mess with it
            }
        }
        for (area in areaMap.values) {
            if (area.rightBorder.isOn(location)) {
                result = null
            }
        }
        return result
    }

    fun getRightBorder(location: Point): Wall? {
        var result: Wall? = null
        for (area in areaMap.values) {
            if (area.rightBorder.isOn(location)) {
                result = area.rightBorder
            }
        }
        for (area in areaMap.values) {
            if (area.leftBorder.isOn(location)) {
                result = null
            }
        }
        return result
    }
}
