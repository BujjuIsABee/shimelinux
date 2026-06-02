package com.group_finity.mascot.environment

import java.awt.Point

interface Border {
    fun isOn(location: Point): Boolean

    fun move(location: Point): Point
}