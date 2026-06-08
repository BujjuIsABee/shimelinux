/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package io.github.bujjuisabee.shimelinux

import com.group_finity.mascot.environment.Area
import com.group_finity.mascot.environment.Environment
import java.awt.Point

class LinuxEnvironment : Environment() {
    override val workArea: Area
        get() = screen
    override val activeIE = Area()
    override val activeIETitle: String
        get() = ""

    init {
        activeIE.isVisible = false
    }

    override fun moveActiveIE(point: Point) {
    }

    override fun restoreIE() {
    }

    override fun refreshCache() {
        // i feel so refreshed!!!
    }

    override fun dispose() {
    }
}
