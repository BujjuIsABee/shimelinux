/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.Mascot

interface Action {
    fun init(mascot: Mascot)

    fun hasNext(): Boolean

    fun next()
}
