package com.group_finity.mascot.environment

import java.awt.GraphicsEnvironment
import java.awt.MouseInfo
import java.awt.Point
import java.awt.Rectangle
import java.awt.Toolkit

abstract class Environment {
    internal abstract val workArea: Area
    abstract val activeIE: Area
    abstract val activeIETitle: String
    abstract fun moveActiveIE(point: Point)
    abstract fun restoreIE()
    abstract fun refreshCache()
    abstract fun dispose()

    var complexScreen = ComplexArea()
    var screen = Area()
    var cursor = Location()
    val screens: Collection<Area>
        get() = complexScreen.areas

    fun init() {
        if (!thread.isAlive) {
            thread.isDaemon = true
            thread.priority = Thread.MIN_PRIORITY
            thread.start()
        }

        tick()
    }

    fun tick() {
        screen.set(screenRect)
        complexScreen.set(screenRects)
        cursor.set(cursorPos)
    }

    fun isScreenTopBottom(location: Point): Boolean {
        var count = 0

        for (area in screens) {
            if (area.topBorder.isOn(location)) {
                ++count
            }
            if (area.bottomBorder.isOn(location)) {
                ++count
            }
        }

        if (count == 0) {
            if (workArea.topBorder.isOn(location)) {
                return true
            }
            if (workArea.bottomBorder.isOn(location)) {
                return true
            }
        }

        return count == 1
    }

    fun isScreenLeftRight(location: Point): Boolean {
        var count = 0

        for (area in screens) {
            if (area.leftBorder.isOn(location)) {
                ++count
            }
            if (area.rightBorder.isOn(location)) {
                ++count
            }
        }

        if (count == 0) {
            if (workArea.leftBorder.isOn(location)) {
                return true
            }
            if (workArea.rightBorder.isOn(location)) {
                return true
            }
        }

        return count == 1
    }

    companion object {
        internal var screenRect = Rectangle(Point(0, 0), Toolkit.getDefaultToolkit().screenSize)
        internal var screenRects = HashMap<String, Rectangle>()
        private val thread = Thread {
            try {
                while (true) {
                    updateScreenRect()
                    Thread.sleep(5000)
                }
            } catch (_: InterruptedException) {
            }
        }
        private val cursorPos: Point
            get() {
                return MouseInfo.getPointerInfo()?.location ?: Point(0, 0)
            }

        fun updateScreenRect() {
            var virtualBounds = Rectangle()
            val screenRects = HashMap<String, Rectangle>()

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