/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
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
        @JvmStatic
        var instance = io.github.bujjuisabee.shimelinux.NativeFactoryImpl()

        @JvmStatic
        @Deprecated("ShimeLinux only uses LinuxNativeFactory")
        fun resetInstance() { }
    }
}
