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

import com.group_finity.mascot.Main
import com.group_finity.mascot.Mascot
import com.group_finity.mascot.NativeFactory
import java.awt.Point

class MascotEnvironment(private val mascot: Mascot) {
    private val impl = NativeFactory.instance.environment
    private var currentWorkArea: Area? = null

    val workArea
        get() = getWorkArea(false)
    val activeIE
        get() = if (!Main.instance.properties.getProperty("Multiscreen", "true").toBoolean() &&
            currentWorkArea?.toRectangle()?.intersects(impl.activeIE.toRectangle()) == false
        ) {
            Area()
        } else {
            impl.activeIE
        }
    val activeIETitle
        get() = impl.activeIETitle
    val screen
        get() = impl.screen
    val complexScreen
        get() = impl.complexScreen
    val cursor
        get() = impl.cursor

    val ceiling
        get() = getCeiling(false)
    val floor
        get() = getFloor(false)
    val wall
        get() = getWall(false)

    val isScreenTopBottom
        get() = impl.isScreenTopBottom(mascot.anchor)
    val isScreenLeftRight
        get() = impl.isScreenLeftRight(mascot.anchor)

    init {
        impl.init()
    }

    fun getWorkArea(ignoreSettings: Boolean): Area {
        var currentWorkArea = currentWorkArea

        if (currentWorkArea != null) {
            if (ignoreSettings || Main.instance.properties.getProperty("Multiscreen", "true").toBoolean()) {
                if (currentWorkArea != impl.workArea &&
                    currentWorkArea.toRectangle().contains(impl.workArea.toRectangle()) &&
                    impl.workArea.contains(mascot.anchor.x, mascot.anchor.y)
                ) {
                    currentWorkArea = impl.workArea
                    return currentWorkArea.also { this.currentWorkArea = it }
                } else if (currentWorkArea.contains(mascot.anchor.x, mascot.anchor.y)) {
                    return currentWorkArea
                }
            } else {
                return currentWorkArea
            }
        }

        if (impl.workArea.contains(mascot.anchor.x, mascot.anchor.y)) {
            currentWorkArea = impl.workArea
            return currentWorkArea.also { this.currentWorkArea = it }
        }

        for (area in impl.screens) {
            if (area.contains(mascot.anchor.x, mascot.anchor.y)) {
                currentWorkArea = area
                return currentWorkArea.also { this.currentWorkArea = it }
            }
        }

        currentWorkArea = impl.workArea
        return currentWorkArea.also { this.currentWorkArea = it }
    }

    fun getCeiling(ignoreSeparator: Boolean) = if (activeIE.bottomBorder.isOn(mascot.anchor)) {
        activeIE.bottomBorder
    } else if (workArea.topBorder.isOn(mascot.anchor) && (!ignoreSeparator || isScreenTopBottom)) {
        workArea.topBorder
    } else {
        NotOnBorder.INSTANCE
    }

    fun getFloor(ignoreSeparator: Boolean) = if (activeIE.topBorder.isOn(mascot.anchor)) {
        activeIE.topBorder
    } else if (workArea.bottomBorder.isOn(mascot.anchor) && (!ignoreSeparator || isScreenTopBottom)) {
        workArea.bottomBorder
    } else {
        NotOnBorder.INSTANCE
    }

    fun getWall(ignoreSeparator: Boolean): Border {
        if (mascot.isLookRight) {
            if (activeIE.leftBorder.isOn(mascot.anchor)) return activeIE.leftBorder
            if (workArea.rightBorder.isOn(mascot.anchor) && (!ignoreSeparator || isScreenLeftRight)) return workArea.rightBorder
        } else {
            if (activeIE.rightBorder.isOn(mascot.anchor)) return activeIE.rightBorder
            if (workArea.leftBorder.isOn(mascot.anchor) && (!ignoreSeparator || isScreenLeftRight)) return workArea.leftBorder
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
