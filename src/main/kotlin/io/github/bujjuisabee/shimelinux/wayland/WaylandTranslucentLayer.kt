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

import com.group_finity.mascot.Main
import com.group_finity.mascot.execute
import com.group_finity.mascot.image.NativeImage
import com.group_finity.mascot.image.TranslucentWindow
import io.github.bujjuisabee.shimelinux.generic.GenericNativeImage
import java.awt.Component
import java.awt.Cursor
import java.awt.Point
import java.awt.event.MouseEvent
import kotlin.system.exitProcess

/**
 * A window that displays a mascot with the Wayland library
 *
 * @author Bujju
 */
class WaylandTranslucentLayer : TranslucentWindow {
    private val component = object : Component() {
        override fun isVisible() = true

        override fun isShowing() = true

        override fun setVisible(b: Boolean) {}

        override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
            super.setBounds(x, y, width, height)
            try {
                WaylandLib.setBounds(senderPtr, x, y, width, height)
            } catch (_: Exception) {
                Main.showError("An error occurred in the Wayland library")
                exitProcess(0)
            }
        }

        override fun getLocationOnScreen() = when (System.getenv("XDG_CURRENT_DESKTOP")) {
            "Hyprland", "KDE" -> location // if a command is being used to get the cursor position, always use the mascot's on-screen position
            else if (isDragging) -> grabStart // if the mascot is being dragged and grabStart is used to estimate its on-screen position, use grabStart
            else -> location // if the mascot is not being dragged, use its on-screen position
        }

        override fun setCursor(cursor: Cursor) {
            try {
                WaylandLib.setCursor(senderPtr, cursor.type == Cursor.HAND_CURSOR)
            } catch (_: Exception) {
                Main.showError("An error occurred in the Wayland library")
                exitProcess(0)
            }
        }
    }

    private val senderPtr = try {
        WaylandLib.createLayer(this)
    } catch (_: Exception) {
        Main.showError("An error occurred in the Wayland library")
        exitProcess(0)
    }

    private var image: GenericNativeImage? = null
    private var imageChanged = false
    private var previousCursorPosition = Point(0, 0)
    private var grabStart = Point(0, 0)
    private var isDragging = false

    override fun asComponent() = component

    override fun setImage(image: NativeImage) {
        if (this.image != image) {
            imageChanged = true
            this.image = image as GenericNativeImage
        }
    }

    override fun updateImage() {
        image?.let {
            imageChanged = false
            try {
                WaylandLib.setImage(senderPtr, it.rgb)
            } catch (_: Exception) {
                Main.showError("An error occurred in the Wayland library")
                exitProcess(0)
            }
        }
    }

    override fun setAlwaysOnTop(onTop: Boolean) {}

    override fun dispose() {
        try {
            WaylandLib.dispose(senderPtr)
        } catch (_: Exception) {
            Main.showError("An error occurred in the Wayland library")
            exitProcess(0)
        }
    }

    @Suppress("unused", "KotlinConstantConditions")
    fun updateCursor(
        leftPressed: Boolean,
        rightPressed: Boolean,
        leftReleased: Boolean,
        rightReleased: Boolean,
        positionX: Int,
        positionY: Int
    ) {
        var modifiers = MouseEvent.NOBUTTON
        var button = MouseEvent.NOBUTTON
        if (leftPressed || leftReleased) {
            modifiers = modifiers or MouseEvent.BUTTON1_DOWN_MASK
            button = button or MouseEvent.BUTTON1
        }
        if (rightPressed || rightReleased) {
            modifiers = modifiers or MouseEvent.BUTTON3_DOWN_MASK
            button = button or MouseEvent.BUTTON3
        }

        if (leftPressed) {
            isDragging = true
            grabStart = component.location
        }

        if (leftReleased) {
            isDragging = false
        }

        if (leftPressed || rightPressed) {
            component.dispatchEvent(
                MouseEvent(
                    component,
                    MouseEvent.MOUSE_PRESSED,
                    System.currentTimeMillis(),
                    modifiers,
                    positionX,
                    positionY,
                    1,
                    false,
                    button
                )
            )
        }

        if (leftReleased || rightReleased) {
            component.dispatchEvent(
                MouseEvent(
                    component,
                    MouseEvent.MOUSE_RELEASED,
                    System.currentTimeMillis(),
                    modifiers,
                    positionX,
                    positionY,
                    1,
                    rightReleased,
                    button
                )
            )
        }

        val newCursorPosition = Point(positionX + grabStart.x, positionY + grabStart.y)
        if (previousCursorPosition != newCursorPosition) {
            previousCursorPosition = newCursorPosition

            // Update cursor position
            when (System.getenv("XDG_CURRENT_DESKTOP")) {
                "Hyprland" -> {
                    WaylandEnvironment.cursorPosition = runCatching {
                        val (x, y) = execute("hyprctl", "cursorpos").split(", ").map { it.toIntOrNull() ?: 0 }
                        return@runCatching Point(x, y)
                    }.getOrNull()
                }

                "KDE" -> {
                    WaylandEnvironment.cursorPosition = runCatching {
                        val result = execute("kdotool", "getmouselocation")
                        val x = result.substringAfter("x:").substringBefore(" ").toIntOrNull() ?: 0
                        val y = result.substringAfter("y:").substringBefore(" ").toIntOrNull() ?: 0
                        return@runCatching Point(x, y)
                    }.getOrNull()
                }

                else -> {
                    WaylandEnvironment.cursorPosition = newCursorPosition // only an estimate of the global cursor position; accuracy varies by compositor
                }
            }

            component.dispatchEvent(
                MouseEvent(
                    component,
                    if (leftPressed || rightPressed) MouseEvent.MOUSE_DRAGGED else MouseEvent.MOUSE_MOVED,
                    System.currentTimeMillis(),
                    modifiers,
                    positionX,
                    positionY,
                    0,
                    rightReleased,
                    button
                )
            )
        }
    }
}
