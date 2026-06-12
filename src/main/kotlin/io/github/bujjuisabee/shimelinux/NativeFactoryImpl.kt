/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package io.github.bujjuisabee.shimelinux

import com.group_finity.mascot.NativeFactory
import com.group_finity.mascot.image.TranslucentWindow
import java.awt.image.BufferedImage
import javax.swing.UIManager

class NativeFactoryImpl : NativeFactory() {
    private val environment = LinuxEnvironment()

    override fun getEnvironment() = environment

    override fun newNativeImage(src: BufferedImage) = LinuxNativeImage(src)

    override fun newTransparentWindow(): TranslucentWindow {
        // Create the window using the default LaF because the GTK LaF breaks transparency
        val previousLaf = UIManager.getLookAndFeel()
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())
        val window = LinuxTranslucentWindow()
        UIManager.setLookAndFeel(previousLaf)
        return window
    }
}
