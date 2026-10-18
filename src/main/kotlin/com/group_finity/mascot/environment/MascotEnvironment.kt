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
 * Manages the platform-specific [Environment] instance for the given [mascot].
 *
 * @author Yuki Yamada
 * @author Shimeji-ee Group
 * @author Bujju
 */
class MascotEnvironment(private val mascot: Mascot) {
    private val impl = NativeFactory.instance.environment
    private var currentWorkArea: Area? = null

    /**
     * An area representing the bounds of the primary monitor.
     */
    val workArea: Area
        get() = getWorkArea(false)

    /**
     * An area representing the combined bounds of all screens.
     */
    val screen: Area
        get() = impl.screen

    /**
     * An area representing the bounds of the active interactive window.
     */
    val activeIE: Area
        get() = if (!getProperty("Multiscreen", true) &&
            currentWorkArea?.toRectangle()?.intersects(impl.activeIE.toRectangle()) == false
        ) {
            Area()
        } else {
            impl.activeIE
        }

    /**
     * The title of the active interactive window.
     */
    val activeIETitle: String
        get() = impl.activeIETitle

    /**
     * A complex area representing the bounds of the screens.
     */
    @Suppress("unused")
    val complexScreen: ComplexArea
        get() = impl.complexScreen

    /**
     * Represents the position of the cursor on the screen.
     */
    val cursor: Location
        get() = impl.cursor

    /**
     * The interactable border on the environment's ceiling.
     */
    val ceiling: Border
        get() = getCeiling(false)

    /**
     * The interactable border on the environment's floor.
     */
    val floor: Border
        get() = getFloor(false)

    /**
     * The interactable border on the wall that the [mascot] is facing.
     */
    val wall: Border
        get() = getWall(false)

    /**
     * Gets whether the [mascot] is on one of the environment's vertical borders.
     */
    val isScreenTopBottom: Boolean
        get() = impl.isScreenTopBottom(mascot.anchor)

    /**
     * Gets whether the [mascot] is on one of the environment's horizontal borders.
     */
    val isScreenLeftRight: Boolean
        get() = impl.isScreenLeftRight(mascot.anchor)

    init {
        impl.init()
    }

    /**
     * Gets an area representing the bounds of the primary monitor. If [ignoreSettings] is `true`, the Multiscreen
     * setting will be ignored.
     */
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

    /**
     * Gets the interactable border on the environment's ceiling. If [ignoreSeparator] is `true`, the top border will be
     * returned even if [isScreenTopBottom] is `false`.
     */
    fun getCeiling(ignoreSeparator: Boolean): Border = if (activeIE.bottomBorder.isOn(mascot.anchor)) {
        activeIE.bottomBorder
    } else if (workArea.topBorder.isOn(mascot.anchor) && (!ignoreSeparator || isScreenTopBottom)) {
        workArea.topBorder
    } else {
        NotOnBorder
    }

    /**
     * Gets the interactable border on the environment's floor. If [ignoreSeparator] is `true`, the bottom border will
     * be returned even if [isScreenTopBottom] is `false`.
     */
    fun getFloor(ignoreSeparator: Boolean): Border = if (activeIE.topBorder.isOn(mascot.anchor)) {
        activeIE.topBorder
    } else if (workArea.bottomBorder.isOn(mascot.anchor) && (!ignoreSeparator || isScreenTopBottom)) {
        workArea.bottomBorder
    } else {
        NotOnBorder
    }

    /**
     * Gets the interactable border on the wall that the [mascot] is facing. If [ignoreSeparator] is `true`, the border
     * will be returned even if [isScreenLeftRight] is `false`.
     */
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
     * Moves the active interactive window to the given [point].
     */
    fun moveActiveIE(point: Point) {
        impl.moveActiveIE(point)
    }

    /**
     * Restores all interactive windows that have been thrown offscreen.
     */
    @Suppress("unused")
    fun restoreIE() {
        impl.restoreIE()
    }

    /**
     * Updates which screen is used for the [workArea].
     */
    fun refreshWorkArea() {
        getWorkArea(true)
    }
}
