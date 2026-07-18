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
import java.awt.Point
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities
import javax.swing.UIManager

@Suppress("unused")
class NativeFactoryImpl : NativeFactory() {
    override val environment = when (desktop) {
        KDE -> KdeEnvironment()
        HYPRLAND, NIRI -> WaylandEnvironment()
        else -> GenericLinuxEnvironment()
    }

    override fun newNativeImage(src: BufferedImage) = LinuxNativeImage(src)

    override fun newTransparentWindow() = when (desktop) {
        HYPRLAND, NIRI -> WaylandTranslucentLayer()
        else -> {
            // Create the window with a LaF that supports transparency
            val previousLaf = UIManager.getLookAndFeel()
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())
            LinuxTranslucentWindow().also { UIManager.setLookAndFeel(previousLaf) }
        }
    }

    override fun getPopupMenu() = when(desktop) {
        HYPRLAND, NIRI -> object : JPopupMenu() {
            override fun setVisible(b: Boolean) {
                if (!b) {
                    // Manually close the popup menu
                    SwingUtilities.getWindowAncestor(this).dispose()
                    firePopupMenuWillBecomeInvisible()
                } else {
                    super.setVisible(true)
                }
            }
        }

        else -> JPopupMenu()
    }

    private fun getHyprlandCursorPos(): Point? = runCatching {
        val process = ProcessBuilder("hyprctl", "cursorpos").start()
        val result = process.inputStream.use {
            val reader = BufferedReader(InputStreamReader(it))
            reader.readLine()
        }

        val parts = result.split(", ")
        return Point(parts[0].toInt(), parts[1].toInt())
    }.getOrNull()

    companion object {
        const val KDE = "KDE"
        const val HYPRLAND = "Hyprland"
        const val NIRI = "niri"

        val desktop: String? = System.getenv("XDG_CURRENT_DESKTOP")
    }
}
