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

import java.awt.GraphicsEnvironment
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Rectangle
import java.awt.Toolkit
import kotlin.concurrent.timer

abstract class Environment {
    internal abstract val workArea: Area
    abstract val activeIE: Area
    abstract val activeIETitle: String

    var complexScreen = ComplexArea()
    var screen = Area()
    var cursor = Location()
    val screens
        get() = complexScreen.areas

    fun init() {
        timer("UpdateScreenRect", true, period = 5000L) { updateScreenRect() }
        tick()
    }

    open fun tick() {
        screen.set(screenRect)
        complexScreen.set(screenRects)
        cursor.set(cursorPos)
    }

    fun isScreenTopBottom(location: Point): Boolean {
        var count = 0

        for (area in screens) {
            if (area.topBorder.isOn(location)) count++
            if (area.bottomBorder.isOn(location)) count++
        }

        if (count == 0 && (workArea.topBorder.isOn(location) || workArea.bottomBorder.isOn(location))) return true
        return count == 1
    }

    fun isScreenLeftRight(location: Point): Boolean {
        var count = 0

        for (area in screens) {
            if (area.leftBorder.isOn(location)) count++
            if (area.rightBorder.isOn(location)) count++
        }

        if (count == 0 && (workArea.leftBorder.isOn(location) || workArea.rightBorder.isOn(location))) return true
        return count == 1
    }

    abstract fun moveActiveIE(point: Point)

    abstract fun restoreIE()

    abstract fun refreshCache()

    abstract fun dispose()

    companion object {
        internal var screenRect = Rectangle(Point(0, 0), Toolkit.getDefaultToolkit().screenSize)
        internal var screenRects = hashMapOf<String, Rectangle>()
        private val cursorPos
            get() = MouseInfo.getPointerInfo()?.location ?: Point(0, 0)

        fun updateScreenRect() {
            var virtualBounds = Rectangle()
            val screenRects = hashMapOf<String, Rectangle>()
            val devices = GraphicsEnvironment.getLocalGraphicsEnvironment().screenDevices

            for (device in devices) {
                screenRects[device.iDstring] = device.defaultConfiguration.bounds
                virtualBounds = virtualBounds.union(device.defaultConfiguration.bounds)
            }

            this.screenRects = screenRects
            screenRect = virtualBounds
        }
    }
}
