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
import com.group_finity.mascot.desktopType
import java.awt.Component
import java.awt.Cursor
import java.awt.Point
import java.awt.event.MouseEvent
import kotlin.system.exitProcess

/**
 * Creates a Wayland layer surface via [WaylandLib].
 *
 * @param mouseEventReceiver The object that will receive mouse events from the layer surface.
 * @param useMask Whether the layer surface's input region should be updated whenever [setImage] is called.
 *
 * @author Bujju
 */
class WaylandLayer(mouseEventReceiver: WaylandLib.MouseEventReceiver, private val useMask: Boolean) : Component() {
    private val senderPtr: Long = try {
        WaylandLib.createLayer(mouseEventReceiver)
    } catch (e: Throwable) {
        Main.showError("Fatal error in libshimelinux_wayland.", e)
        exitProcess(0)
    }

    private var absoluteLocation: Point = location
    private var isDragging = false
    private var isDisposed = false

    override fun isVisible() = true

    override fun setVisible(b: Boolean) {}

    override fun isShowing() = true

    override fun getLocationOnScreen(): Point = absoluteLocation

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        if (isDisposed) return

        super.setBounds(x, y, width, height)

        try {
            WaylandLib.setBounds(senderPtr, x, y, width, height)
        } catch (e: Throwable) {
            Main.showError("Fatal error in libshimelinux_wayland.", e)
            exitProcess(0)
        }
    }

    override fun setCursor(cursor: Cursor) {
        if (isDisposed) return

        try {
            WaylandLib.setCursor(senderPtr, cursor.type == Cursor.HAND_CURSOR)
        } catch (e: Throwable) {
            Main.showError("Fatal error in libshimelinux_wayland.", e)
            exitProcess(0)
        }
    }

    /**
     * Displays an image on the layer surface, and updates its input region if [useMask] is true.
     *
     * @param rgb The image to display in ARGB8888 format.
     */
    fun setImage(rgb: IntArray) {
        if (isDisposed) return

        try {
            WaylandLib.setImage(senderPtr, rgb, useMask)
        } catch (e: Throwable) {
            Main.showError("Fatal error in libshimelinux_wayland.", e)
            exitProcess(0)
        }
    }

    /**
     * Destroys the layer surface.
     */
    fun dispose() {
        if (isDisposed) return

        try {
            WaylandLib.dispose(senderPtr)
        } catch (e: Throwable) {
            Main.showError("Fatal error in libshimelinux_wayland.", e)
            exitProcess(0)
        }

        isDisposed = true // prevents segmentation fault
    }

    /**
     * Sends `MOUSE_PRESSED`, `MOUSE_RELEASED`, `MOUSE_MOVED`, and `MOUSE_DRAGGED` events to the event listeners attached to [component].
     *
     * @param component The AWT component that the events will be dispatched to.
     * @see WaylandLib.MouseEventReceiver.updateCursor
     */
    fun dispatchEvents(
        component: Component,
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
            modifiers = MouseEvent.BUTTON1_DOWN_MASK
            button = MouseEvent.BUTTON1
        }
        if (rightPressed || rightReleased) {
            modifiers = modifiers or MouseEvent.BUTTON3_DOWN_MASK
            button = button or MouseEvent.BUTTON3
        }

        /*
         * If absoluteLocation is being used to calculate the global cursor position, it should not be updated while the
         * left mouse button is pressed so that mascots will not fly offscreen while being dragged with the cursor.
         *
         * When the global cursor position is provided directly by the compositor, like on KDE Plasma and Hyprland,
         * absoluteLocation should always be updated so that popup menus will be displayed in the correct location.
         */
        isDragging = leftPressed || (isDragging && !leftReleased)
        if (!isDragging || desktopType == "KDE" || desktopType == "Hyprland") {
            absoluteLocation = location
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

        val newCursorPosition = Point(positionX + absoluteLocation.x, positionY + absoluteLocation.y)
        if (WaylandEnvironment.absoluteCursorPosition != newCursorPosition) {
            WaylandEnvironment.absoluteCursorPosition = newCursorPosition

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
