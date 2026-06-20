/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
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
        timer("UpdateScreenRect", true, period = 5000L) {
            updateScreenRect()
        }

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

        if (count == 0 && (workArea.topBorder.isOn(location) || workArea.bottomBorder.isOn(location))) {
            return true
        }

        return count == 1
    }

    fun isScreenLeftRight(location: Point): Boolean {
        var count = 0

        for (area in screens) {
            if (area.leftBorder.isOn(location)) count++
            if (area.rightBorder.isOn(location)) count++
        }

        if (count == 0 && (workArea.leftBorder.isOn(location) || workArea.rightBorder.isOn(location))) {
            return true
        }

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
            val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
            val devices = environment.screenDevices

            for (device in devices) {
                screenRects[device.iDstring] = device.defaultConfiguration.bounds
                virtualBounds = virtualBounds.union(device.defaultConfiguration.bounds)
            }

            this.screenRects = screenRects
            screenRect = virtualBounds
        }
    }
}
