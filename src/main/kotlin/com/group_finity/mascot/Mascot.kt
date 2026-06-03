/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot

import com.group_finity.mascot.behavior.Behavior
import com.group_finity.mascot.environment.MascotEnvironment
import com.group_finity.mascot.hotspot.Hotspot
import com.group_finity.mascot.image.MascotImage
import java.awt.Point
import java.awt.Rectangle
import java.util.concurrent.atomic.AtomicInteger

class Mascot {
    private val id = lastId.incrementAndGet()
    private val window = NativeFactory.instance.newTransparentWindow()
    private var imageSet = ""
    var manager: Manager? = null
    var anchor = Point(0, 0)
    var image: MascotImage? = null
    var isLookRight = false
    var behavior: Behavior? = null
    private var time = 0
    private var isAnimating = true
    private var isPaused = false
    private var isDragging = false
    var environment = MascotEnvironment(this)
    var sound: String? = null
    private var affordances = ArrayList<String>(5)
    private var hotspots = ArrayList<Hotspot>(5)
    val bounds: Rectangle
        get() {
            if (image != null) {
                val top = anchor.y - image!!.center.y
                val left = anchor.x - image!!.center.x
                return Rectangle(left, top, image!!.size.width, image!!.size.height)
            } else {
                return window.asComponent().bounds
            }
        }

    companion object {
        private val lastId = AtomicInteger()
    }
}
