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

import com.group_finity.mascot.Mascot
import com.group_finity.mascot.NativeFactory
import com.group_finity.mascot.getProperty
import java.awt.Point

/**
 * Exposes the [Environment] instance to mascots and variables
 *
 * @author Yuki Yamada
 * @author Kilkakon
 * @author Bujju
 */
class MascotEnvironment(private val mascot: Mascot) {
    private val impl = NativeFactory.instance.environment
    private var currentWorkArea: Area? = null

    /**
     * An [Area] representing the bounds of the screen; used when Multiscreen is disabled
     */
    val workArea: Area
        get() = getWorkArea(false)

    /**
     * An [Area] representing the bounds of the screen; used when Multiscreen is enabled
     */
    val screen: Area
        get() = impl.screen

    /**
     * An [Area] representing the bounds of the active interactive window
     */
    val activeIE: Area
        get() = if (!getProperty("Multiscreen", true) && currentWorkArea?.toRectangle()?.intersects(impl.activeIE.toRectangle()) == false) {
            Area()
        } else {
            impl.activeIE
        }

    /**
     * The title of the active interactive window
     */
    val activeIETitle: String
        get() = impl.activeIETitle

    @Suppress("unused")
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
        currentWorkArea?.let { area ->
            if (!ignoreSettings && !getProperty("Multiscreen", true)) {
                return area
            }
            if (currentWorkArea != impl.workArea && area.toRectangle().contains(impl.workArea.toRectangle())) {
                if (impl.workArea.contains(mascot.anchor.x, mascot.anchor.y)) {
                    return impl.workArea.also { currentWorkArea = it }
                }
            }
            if (area.contains(mascot.anchor.x, mascot.anchor.y)) {
                return area
            }
        }

        if (impl.workArea.contains(mascot.anchor.x, mascot.anchor.y)) {
            return impl.workArea.also { currentWorkArea = it }
        }

        (impl.screens.firstOrNull { it.contains(mascot.anchor.x, mascot.anchor.y) } ?: impl.workArea).let {
            return it.also { currentWorkArea = it }
        }
    }

    fun getCeiling(ignoreSeparator: Boolean) = if (activeIE.bottomBorder.isOn(mascot.anchor)) {
        activeIE.bottomBorder
    } else if (workArea.topBorder.isOn(mascot.anchor) && (!ignoreSeparator || isScreenTopBottom)) {
        workArea.topBorder
    } else {
        NotOnBorder
    }

    fun getFloor(ignoreSeparator: Boolean) = if (activeIE.topBorder.isOn(mascot.anchor)) {
        activeIE.topBorder
    } else if (workArea.bottomBorder.isOn(mascot.anchor) && (!ignoreSeparator || isScreenTopBottom)) {
        workArea.bottomBorder
    } else {
        NotOnBorder
    }

    fun getWall(ignoreSeparator: Boolean): Border {
        if (mascot.isLookRight) {
            if (activeIE.leftBorder.isOn(mascot.anchor)) {
                return activeIE.leftBorder
            }
            if (workArea.rightBorder.isOn(mascot.anchor) && (!ignoreSeparator || isScreenLeftRight)) {
                return workArea.rightBorder
            }
        } else {
            if (activeIE.rightBorder.isOn(mascot.anchor)) {
                return activeIE.rightBorder
            }
            if (workArea.leftBorder.isOn(mascot.anchor) && (!ignoreSeparator || isScreenLeftRight)) {
                return workArea.leftBorder
            }
        }
        return NotOnBorder
    }

    /**
     * Moves the active interactive window to [point]
     */
    fun moveActiveIE(point: Point) {
        impl.moveActiveIE(point)
    }

    /**
     * Restores all interactive windows that have been thrown offscreen
     */
    @Suppress("unused")
    fun restoreIE() {
        impl.restoreIE()
    }

    /**
     * Refreshes which screen is used for the work area
     */
    fun refreshWorkArea() {
        getWorkArea(true)
    }
}
