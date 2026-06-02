/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.environment

import com.group_finity.mascot.Main
import com.group_finity.mascot.Mascot
import com.group_finity.mascot.NativeFactory
import java.awt.Point

class MascotEnvironment(private val mascot: Mascot) {
    private val impl = NativeFactory.instance.getEnvironment()
    private var currentWorkArea: Area? = null
    val workArea: Area
        get() = getWorkArea(false)
    val activeIE: Area
        get() {
            var result = impl.activeIE

            if (currentWorkArea != null &&
                !Main.instance.properties.getProperty("Multiscreen", "true").toBoolean() &&
                !currentWorkArea!!.toRectangle().intersects(result.toRectangle())
            ) {
                return Area()
            }

            return result
        }
    val activeIETitle: String
        get() = impl.activeIETitle
    val screen: Area
        get() = impl.screen
    val complexScreen: ComplexArea
        get() = impl.complexScreen
    val cursor: Location
        get() = impl.cursor
    val ceiling: Border
        get() = getCeiling(false)
    val floor: Border
        get() = getFloor(false)
    val wall: Border
        get() = getWall(false)
    val isScreenTopBottom: Boolean
        get() = impl.isScreenTopBottom(mascot.anchor)
    val isScreenLeftRight: Boolean
        get() = impl.isScreenLeftRight(mascot.anchor)

    init {
        impl.init()
    }

    fun getWorkArea(ignoreSettings: Boolean): Area {
        if (currentWorkArea != null) {
            if (ignoreSettings || Main.instance.properties.getProperty("Multiscreen", "true").toBoolean()) {
                if (currentWorkArea != impl.workArea && currentWorkArea!!.toRectangle().contains(impl.workArea.toRectangle())) {
                    if (impl.workArea.contains(mascot.anchor.x, mascot.anchor.y)) {
                        currentWorkArea = impl.workArea
                        return currentWorkArea!!
                    }
                }

                if (currentWorkArea!!.contains(mascot.anchor.x, mascot.anchor.y)) {
                    return currentWorkArea!!
                }
            } else {
                return currentWorkArea!!
            }
        }

        if (impl.workArea.contains(mascot.anchor.x, mascot.anchor.y)) {
            currentWorkArea = impl.workArea
            return currentWorkArea!!
        }

        for (area in impl.screens) {
            if (area.contains(mascot.anchor.x, mascot.anchor.y)) {
                currentWorkArea = area
                return currentWorkArea!!
            }
        }

        currentWorkArea = impl.workArea
        return currentWorkArea!!
    }

    fun getCeiling(ignoreSeparator: Boolean): Border {
        if (activeIE.bottomBorder.isOn(mascot.anchor)) {
            return activeIE.bottomBorder
        }
        if (workArea.topBorder.isOn(mascot.anchor)) {
            if (!ignoreSeparator || isScreenTopBottom) {
                return workArea.topBorder
            }
        }
        return NotOnBorder.INSTANCE
    }

    fun getFloor(ignoreSeparator: Boolean): Border {
        if (activeIE.topBorder.isOn(mascot.anchor)) {
            return activeIE.topBorder
        }
        if (workArea.bottomBorder.isOn(mascot.anchor)) {
            if (!ignoreSeparator || isScreenTopBottom) {
                return workArea.topBorder
            }
        }
        return NotOnBorder.INSTANCE
    }

    fun getWall(ignoreSeparator: Boolean): Border {
        if (mascot.isLookRight) {
            if (activeIE.leftBorder.isOn(mascot.anchor)) {
                return activeIE.leftBorder
            }
            if (workArea.rightBorder.isOn(mascot.anchor)) {
                if (!ignoreSeparator || isScreenLeftRight) {
                    return workArea.rightBorder
                }
            }
        } else {
            if (activeIE.rightBorder.isOn(mascot.anchor)) {
                return activeIE.rightBorder
            }
            if (workArea.leftBorder.isOn(mascot.anchor)) {
                if (!ignoreSeparator || isScreenLeftRight) {
                    return workArea.leftBorder
                }
            }
        }
        return NotOnBorder.INSTANCE
    }

    fun moveActiveIE(point: Point) {
        impl.moveActiveIE(point)
    }

    fun restoreIE() {
        impl.restoreIE()
    }

    fun refreshWorkArea() {
        getWorkArea(true)
    }
}
