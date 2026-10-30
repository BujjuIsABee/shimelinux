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

package io.github.bujjuisabee.shimelinux.wayland

import com.group_finity.mascot.Platform
import com.group_finity.mascot.environment.Area
import com.group_finity.mascot.environment.Environment
import com.group_finity.mascot.execute
import com.group_finity.mascot.getProperty
import io.github.bujjuisabee.shimelinux.kde.KWin
import io.github.bujjuisabee.shimelinux.wayland.lib.WaylandLib
import java.awt.Point
import javax.swing.PopupFactory
import kotlin.collections.map

/**
 * An environment that displays mascots on native Wayland surfaces.
 *
 * @author Bujju
 */
class WaylandEnvironment : Environment() {
    override val workArea: Area
        get() = screen

    override val activeIE = Area()
    override val activeIETitle = ""

    init {
        PopupFactory.setSharedInstance(WaylandPopupFactory)
        activeIE.isVisible = false
    }

    override fun tick() {
        // Update screen bounds
        val (screenX, screenY, screenWidth, screenHeight) = WaylandLib.getScreenRect()

        if (getProperty("OverrideScreenDimensions", false)) {
            screenRect.setBounds(
                getProperty("ScreenX", screen.left),
                getProperty("ScreenY", screen.top),
                getProperty("ScreenWidth", screen.width),
                getProperty("ScreenHeight", screen.height)
            )
        } else {
            screenRect.setBounds(0, 0, screenWidth, screenHeight)
            applyInsets()
        }

        screen.set(screenRect)

        // Update cursor position
        when (Platform.currentDesktop) {
            "hyprland" -> runCatching {
                val (x, y) = execute("hyprctl", "cursorpos").split(", ").map { it.toInt() }
                absoluteCursorPosition.setLocation(x - screenX, y - screenY)
            }

            "mango" -> runCatching {
                val (x, y) = execute("mmsg", "get", "cursorpos").split(',').map { it.substringAfter(':') }.take(2).map { it.toDouble().toInt() }
                absoluteCursorPosition.setLocation(x - screenX, y - screenY)
            }

            "kde" -> runCatching {
                val cursorPos = checkNotNull(KWin.cursorPosition)
                absoluteCursorPosition.setLocation(cursorPos.x - screenX, cursorPos.y - screenY)
            }
        }

        cursor.set(absoluteCursorPosition)
    }

    override fun moveActiveIE(point: Point) {}

    override fun restoreIE() {}

    override fun refreshCache() {}

    override fun dispose() {}
}
