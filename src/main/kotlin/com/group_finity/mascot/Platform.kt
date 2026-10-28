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

import io.github.bujjuisabee.shimelinux.BuildConfig

/**
 * Provides information about the current desktop environment.
 *
 * @author Bujju
 */
object Platform {
    /**
     * The operating system's name.
     */
    val osName: String = System.getProperty("os.name").orEmpty().lowercase()

    /**
     * The operating system's architecture.
     */
    val osArch: String = System.getProperty("os.arch").orEmpty().lowercase()

    /**
     * The name of the current desktop environment.
     */
    val currentDesktop: String = (System.getenv("XDG_CURRENT_DESKTOP") ?: System.getenv("DESKTOP_SESSION")).orEmpty().lowercase()

    /**
     * The name of the current display server protocol.
     */
    val sessionType: String = System.getenv("XDG_SESSION_TYPE").orEmpty().lowercase()

    /**
     * Whether the program is running inside the Flatpak sandbox.
     */
    val isFlatpak: Boolean = System.getenv("container") == "flatpak"

    /**
     * Whether [currentDesktop] is a tiling window manager.
     */
    val isTilingWindowManager: Boolean by lazy {
        val tilingWMs = listOf("awesome", "cosmic", "dwm", "hyprland", "i3", "mango", "niri", "river", "sway", "xmonad")
        tilingWMs.any { currentDesktop.contains(it) }
    }

    /**
     * Whether [currentDesktop] should be assumed to support the wlr-layer-shell protocol.
     */
    val supportsWaylandLayerSurfaces: Boolean by lazy {
        val unsupportedDE = listOf("cinnamon", "gnome").any { currentDesktop.contains(it) }
        val unsupportedOS = osName != BuildConfig.TARGET_OS || osArch != BuildConfig.TARGET_ARCH
        val isSandboxed = isFlatpak && !currentDesktop.contains("kde")
        sessionType == "wayland" && !unsupportedDE && !unsupportedOS && !isSandboxed
    }

    /**
     * Whether [currentDesktop] provides the absolute cursor position.
     */
    val providesAbsoluteCursorPosition: Boolean by lazy {
        val supportedDE = listOf("hyprland", "kde", "mango").any { currentDesktop.contains(it) }
        sessionType != "wayland" || supportedDE
    }
}
