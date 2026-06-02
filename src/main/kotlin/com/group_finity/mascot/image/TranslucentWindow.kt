/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.image

import java.awt.Component

interface TranslucentWindow {
    fun asComponent(): Component

    fun setImage(image: NativeImage)

    fun updateImage()

    fun setAlwaysOnTop(onTop: Boolean)

    fun dispose()
}
