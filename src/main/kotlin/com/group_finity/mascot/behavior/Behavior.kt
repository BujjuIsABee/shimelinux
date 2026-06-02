package com.group_finity.mascot.behavior

import com.group_finity.mascot.Mascot
import java.awt.event.MouseEvent

interface Behavior {
    fun init(mascot: Mascot)

    fun next()

    fun mousePressed(e: MouseEvent)

    fun mouseReleased(e: MouseEvent)
}