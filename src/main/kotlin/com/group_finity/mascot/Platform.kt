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

/**
 * Provides information about the current desktop environment.
 */
object Platform {
    /**
     * The name of the current desktop environment or compositor.
     */
    val currentDesktop: String = System.getenv("XDG_CURRENT_DESKTOP")?.lowercase() ?: "unknown"

    /**
     * The name of the current display server protocol.
     */
    val sessionType: String = System.getenv("XDG_SESSION_TYPE")?.lowercase() ?: "unknown"

    /**
     * Whether [currentDesktop] is a tiling window manager.
     */
    val isTilingWindowManager: Boolean = listOf("cosmic", "hyprland", "i3", "mango", "niri", "sway").any { currentDesktop.contains(it) }

    /**
     * Whether [currentDesktop] should be assumed to support the wlr_layer_shell protocol.
     */
    val supportsWaylandLayerSurfaces: Boolean = sessionType == "wayland" && !listOf("gnome", "cinnamon").any { currentDesktop.contains(it) }

    /**
     * Whether [currentDesktop] directly provides the absolute position of the cursor.
     */
    val providesAbsoluteCursorPosition: Boolean = sessionType != "wayland" || listOf("hyprland", "kde", "mango").any { currentDesktop.contains(it) }
}
