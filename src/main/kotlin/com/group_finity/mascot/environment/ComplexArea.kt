/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
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
