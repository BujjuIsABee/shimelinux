/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package io.github.bujjuisabee.shimelinux

import com.group_finity.mascot.NativeFactory
import com.group_finity.mascot.environment.Environment
import com.group_finity.mascot.image.TranslucentWindow
import java.awt.image.BufferedImage
import javax.swing.UIManager

class NativeFactoryImpl : NativeFactory() {
    private val environment: Environment

    init {
        // Get window manager
        val wm = try {
            when (System.getenv("XDG_SESSION_TYPE").lowercase()) {
                "x11" -> WindowManager.X11
                "wayland" -> WindowManager.WAYLAND
                else -> WindowManager.OTHER
            }
        } catch (_: Exception) {
            WindowManager.OTHER
        }

        // Get desktop environment
        val de = try {
            when (System.getenv("XDG_CURRENT_DESKTOP").lowercase()) {
                "kde" -> DesktopEnvironment.KDE
                else -> DesktopEnvironment.OTHER
            }
        } catch (_: Exception) {
            DesktopEnvironment.OTHER
        }

        environment = if (wm == WindowManager.X11) {
            X11Environment()
        } else if (de == DesktopEnvironment.KDE) {
            KdeEnvironment()
        } else {
            GenericLinuxEnvironment()
        }
    }

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

    enum class WindowManager { X11, WAYLAND, OTHER }
    enum class DesktopEnvironment { KDE, OTHER }
}
