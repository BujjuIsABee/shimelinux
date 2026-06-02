package com.group_finity.mascot.environment

import java.awt.Point
import java.awt.Rectangle

class ComplexArea {
    private val areaMap = HashMap<String, Area>()
    val areas: Collection<Area>
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
        val iterator = areaMap.keys.iterator()
        while (iterator.hasNext()) {
            val key = iterator.next()
            if (!deviceNames.contains(key)) {
                iterator.remove()
            }
        }
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