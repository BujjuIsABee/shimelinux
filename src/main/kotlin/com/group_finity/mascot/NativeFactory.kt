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

import com.group_finity.mascot.environment.Environment
import com.group_finity.mascot.image.NativeImage
import com.group_finity.mascot.image.TranslucentWindow
import io.github.bujjuisabee.shimelinux.wayland.WaylandPopupFactory
import java.awt.image.BufferedImage
import javax.swing.PopupFactory

/**
 * A factory for platform-specific objects
 *
 * @author Kilkakon
 * @author Bujju
 */
abstract class NativeFactory {
    abstract val environment: Environment

    abstract fun newTranslucentWindow(): TranslucentWindow

    abstract fun newNativeImage(src: BufferedImage): NativeImage

    companion object {
        lateinit var instance: NativeFactory

        /**
         * Gets whether mascots can interact with windows
         */
        val interactiveWindowsSupported: Boolean
            get() = instance::class.java.name.endsWith("kde.NativeFactoryImpl")

        /**
         * Gets whether the Wayland library is being used to display mascots
         */
        val usingWaylandLibrary: Boolean
            get() = instance::class.java.name.endsWith("wayland.NativeFactoryImpl")

        /**
         * Gets the default environment, which is chosen based on the value of `XDG_CURRENT_DESKTOP`
         */
        val defaultEnvironment = when (System.getenv("XDG_CURRENT_DESKTOP")) {
            "KDE" -> "kde"
            "Hyprland", "niri", "sway" -> "wayland"
            else -> "generic"
        }

        init {
            resetInstance()
        }

        fun resetInstance() {
            val basepkg = "io.github.bujjuisabee.shimelinux"
            val subpkg = when (getProperty("Environment", "linux")) {
                "linux" -> defaultEnvironment
                "kde" if (System.getenv("XDG_CURRENT_DESKTOP") == "KDE") -> "kde"
                "wayland" if (System.getenv("XDG_SESSION_TYPE") == "wayland") -> "wayland"
                "virtual" -> "virtual"
                else -> "generic"
            }

            @Suppress("UNCHECKED_CAST")
            val impl = Class.forName("$basepkg.$subpkg.NativeFactoryImpl") as Class<NativeFactory>
            instance = impl.getConstructor().newInstance()

            if (usingWaylandLibrary) {
                PopupFactory.setSharedInstance(WaylandPopupFactory)
            } else {
                PopupFactory.setSharedInstance(PopupFactory())
            }
        }
    }
}
