/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.environment

import java.awt.Point

class NotOnBorder : Border {
    private constructor()

    override fun isOn(location: Point) = false

    override fun move(location: Point) = location

    companion object {
        val INSTANCE = NotOnBorder()
    }
}
