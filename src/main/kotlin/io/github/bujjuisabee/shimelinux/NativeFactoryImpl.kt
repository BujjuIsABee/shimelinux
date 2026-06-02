/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package io.github.bujjuisabee.shimelinux

import com.group_finity.mascot.NativeFactory
import com.group_finity.mascot.environment.Environment
import com.group_finity.mascot.image.NativeImage
import com.group_finity.mascot.image.TranslucentWindow
import java.awt.image.BufferedImage

class NativeFactoryImpl : NativeFactory() {
    private val environment = LinuxEnvironment()

    override fun getEnvironment(): Environment {
        return environment
    }

    override fun newNativeImage(src: BufferedImage): NativeImage {
        return LinuxNativeImage(src)
    }

    override fun newTransparentWindow(): TranslucentWindow {
        return LinuxTranslucentWindow()
    }
}
