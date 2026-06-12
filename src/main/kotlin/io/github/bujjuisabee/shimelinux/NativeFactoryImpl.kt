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
    private var environment: Environment

    init {
        // Get window manager
        val wm = try {
            when (System.getenv("XDG_SESSION_TYPE").lowercase()) {
                "x11" -> SessionType.X11
                "wayland" -> SessionType.WAYLAND
                else -> SessionType.OTHER
            }
        } catch (_: Exception) {
            SessionType.OTHER
        }

        // Get desktop environment
        val de = try {
            when (System.getenv("XDG_CURRENT_DESKTOP").lowercase()) {
                "kde" -> DesktopType.KDE
                "gnome", "ubuntu:gnome" -> DesktopType.GNOME
                else -> DesktopType.OTHER
            }
        } catch (_: Exception) {
            DesktopType.OTHER
        }

        environment = when (wm) {
            SessionType.X11 -> X11Environment()
            SessionType.WAYLAND if (de == DesktopType.KDE) -> KdeEnvironment()
            SessionType.WAYLAND if (de == DesktopType.GNOME) -> GnomeEnvironment()
            else -> GenericLinuxEnvironment()
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

    enum class SessionType { X11, WAYLAND, OTHER }
    enum class DesktopType { KDE, GNOME, OTHER }
}
