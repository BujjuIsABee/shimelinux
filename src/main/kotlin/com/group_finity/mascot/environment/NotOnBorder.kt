/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.environment

import java.awt.Point

class NotOnBorder : Border {
    private constructor()

    override fun isOn(location: Point): Boolean = false

    override fun move(location: Point): Point = location

    companion object {
        val INSTANCE = NotOnBorder()
    }
}
