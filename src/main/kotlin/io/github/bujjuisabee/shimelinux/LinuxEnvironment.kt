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
        get() = TODO("Not yet implemented")
    override val activeIE: Area
        get() = TODO("Not yet implemented")
    override val activeIETitle: String
        get() = TODO("Not yet implemented")

    override fun moveActiveIE(point: Point) {
        TODO("Not yet implemented")
    }

    override fun restoreIE() {
        TODO("Not yet implemented")
    }

    override fun refreshCache() {
        TODO("Not yet implemented")
    }

    override fun dispose() {
        TODO("Not yet implemented")
    }
}
