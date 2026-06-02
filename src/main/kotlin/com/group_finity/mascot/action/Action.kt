package com.group_finity.mascot.action

import com.group_finity.mascot.Mascot

interface Action {
    fun init(mascot: Mascot)

    fun hasNext(): Boolean

    fun next()
}