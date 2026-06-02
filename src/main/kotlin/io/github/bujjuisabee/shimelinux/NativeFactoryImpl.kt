/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package io.github.bujjuisabee.shimelinux

import com.group_finity.mascot.NativeFactory
import com.group_finity.mascot.environment.Environment
import com.group_finity.mascot.image.NativeImage
import com.group_finity.mascot.image.TranslucentWindow
import java.awt.image.BufferedImage

class NativeFactoryImpl : NativeFactory() {
    private val environment = LinuxEnvironment()

    override fun getEnvironment(): Environment = environment

    override fun newNativeImage(src: BufferedImage): NativeImage = LinuxNativeImage(src)

    override fun newTransparentWindow(): TranslucentWindow = LinuxTranslucentWindow()
}
