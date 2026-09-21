/*
 * Copyright (c) 2026, Bujju
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *        following disclaimer.
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *        following disclaimer in the documentation and/or other materials provided with the distribution.
 *     3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
 *        products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.group_finity.mascot

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.ui.FlatPopupFactory
import com.group_finity.mascot.environment.Environment
import com.group_finity.mascot.image.NativeImage
import com.group_finity.mascot.image.TranslucentWindow
import io.github.bujjuisabee.shimelinux.wayland.WaylandPopupFactory
import java.awt.image.BufferedImage
import javax.swing.PopupFactory
import javax.swing.UIManager

import io.github.bujjuisabee.shimelinux.generic.NativeFactoryImpl as GenericNativeFactory
import io.github.bujjuisabee.shimelinux.kde.NativeFactoryImpl as KdeNativeFactory
import io.github.bujjuisabee.shimelinux.wayland.NativeFactoryImpl as WaylandNativeFactory
import io.github.bujjuisabee.shimelinux.virtual.NativeFactoryImpl as VirtualNativeFactory

/**
 * Gets the name of the current desktop environment.
 */
val desktopType: String? = System.getenv("XDG_CURRENT_DESKTOP")

/**
 * Gets the name of the current display server protocol (i.e., Wayland or X11).
 */
val sessionType: String? = System.getenv("XDG_SESSION_TYPE")

/**
 * Gets the name of the environment chosen automatically based on the [desktopType].
 */
val defaultEnvironment: String = when (desktopType) {
    "KDE" -> "kde"
    "COSMIC", "Hyprland", "niri", "sway" -> "wayland"
    else -> "generic"
}

/**
 * Gets the name of the current environment.
 */
val activeEnvironment: String
    get() = when (getProperty("Environment", "")) {
        "kde" if (desktopType == "KDE") -> "kde"
        "wayland" if (sessionType == "wayland") -> "wayland"
        "generic" -> "generic"
        "virtual" -> "virtual"
        else -> defaultEnvironment
    }

/**
 * A factory for platform-specific objects.
 *
 * @author Kilkakon
 * @author Bujju
 */
abstract class NativeFactory {
    /**
     * Gets a platform-specific [Environment].
     */
    abstract val environment: Environment

    /**
     * Gets a platform-specific [TranslucentWindow].
     */
    abstract fun newTranslucentWindow(): TranslucentWindow

    /**
     * Gets a platform-specific [NativeImage].
     */
    abstract fun newNativeImage(src: BufferedImage): NativeImage

    companion object {
        lateinit var instance: NativeFactory

        init {
            resetInstance()
        }

        /**
         * Resets [instance] based on [activeEnvironment].
         */
        fun resetInstance() {
            instance = when (activeEnvironment) {
                "kde" -> KdeNativeFactory()
                "wayland" -> WaylandNativeFactory()
                "generic" -> GenericNativeFactory()
                "virtual" -> VirtualNativeFactory()
                else -> GenericNativeFactory()
            }

            resetPopupFactory()
        }

        /**
         * Resets the shared popup factory instance based on [activeEnvironment].
         */
        fun resetPopupFactory() {
            if (activeEnvironment == "wayland") {
                PopupFactory.setSharedInstance(WaylandPopupFactory)
            } else if (UIManager.getLookAndFeel() is FlatLaf) {
                PopupFactory.setSharedInstance(FlatPopupFactory())
            } else {
                PopupFactory.setSharedInstance(PopupFactory())
            }
        }
    }
}
