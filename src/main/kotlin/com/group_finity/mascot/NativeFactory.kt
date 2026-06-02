/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot

import com.group_finity.mascot.environment.Environment
import com.group_finity.mascot.image.NativeImage
import com.group_finity.mascot.image.TranslucentWindow
import java.awt.image.BufferedImage

abstract class NativeFactory {
    abstract fun getEnvironment(): Environment

    abstract fun newNativeImage(src: BufferedImage): NativeImage

    abstract fun newTransparentWindow(): TranslucentWindow

    companion object {
        lateinit var instance: NativeFactory

        init {
            resetInstance()
        }

        fun resetInstance() {
            instance = io.github.bujjuisabee.shimelinux.NativeFactoryImpl()
        }
    }
}
