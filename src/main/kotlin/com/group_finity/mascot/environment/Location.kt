/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.environment

import java.awt.Point

class Location {
    var x = 0
    var y = 0
    var dx = 0
    var dy = 0

    fun set(value: Point) {
        dx = (dx + value.x - x) / 2
        dy = (dy + value.y - y) / 2

        x = value.x
        y = value.y
    }
}
