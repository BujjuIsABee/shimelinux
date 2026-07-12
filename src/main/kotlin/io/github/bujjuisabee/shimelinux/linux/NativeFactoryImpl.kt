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

package io.github.bujjuisabee.shimelinux.linux

import com.group_finity.mascot.NativeFactory
import com.group_finity.mascot.environment.Environment
import com.group_finity.mascot.image.TranslucentWindow
import java.awt.image.BufferedImage
import javax.swing.UIManager

@Suppress("unused")
class NativeFactoryImpl : NativeFactory() {
    val waylandLibExists = this::class.java.getResource("/lib/libshimelinux_wayland.so") != null
    val isTilingWM = when (System.getenv("XDG_CURRENT_DESKTOP")) {
        "Hyprland", "niri" -> true
        else -> false
    }
    override val environment: Environment = when (System.getenv("XDG_CURRENT_DESKTOP")) {
        "KDE" -> KdeEnvironment()
        else -> GenericLinuxEnvironment()
    }

    override fun newNativeImage(src: BufferedImage) = LinuxNativeImage(src)

    override fun newTransparentWindow(): TranslucentWindow {
        if (usingWaylandLayers) {
            return WaylandTranslucentWindow()
        } else {
            // Create the window with a LaF that supports transparency
            val previousLaf = UIManager.getLookAndFeel()
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())
            return LinuxTranslucentWindow().also { UIManager.setLookAndFeel(previousLaf) }
        }
    }
}
