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
import com.group_finity.mascot.Platform
import com.group_finity.mascot.localize
import io.github.bujjuisabee.shimelinux.wayland.lib.MouseEventReceiver
import io.github.bujjuisabee.shimelinux.wayland.lib.WaylandLib
import java.awt.Component
import java.awt.Cursor
import java.awt.Point
import java.awt.Window
import java.awt.event.MouseEvent
import javax.swing.JDialog
import kotlin.system.exitProcess

/**
 * The absolute cursor position, or an approximation of it based on the [relativeCursorPosition] if
 * [Platform.providesAbsoluteCursorPosition] is `false`.
 */
var absoluteCursorPosition = Point(0, 0)

/**
 * The relative cursor position provided by the compositor.
 */
var relativeCursorPosition = Point(0, 0)

/**
 * Creates a Wayland layer surface.
 *
 * Mouse events will be sent to [mouseEventReceiver]. If [useInputRegion] is `true`, mouse events on fully transparent
 * pixels will be ignored.
 *
 * @author Bujju
 */
class WaylandLayer(mouseEventReceiver: MouseEventReceiver, private val useInputRegion: Boolean) : Component() {
    private val layerIndex: Int
    private var absoluteLocation: Point = location
    private var leftHeld = false
    private var isDisposed = false

    init {
        try {
            layerIndex = WaylandLib.createLayer(WaylandLib.senderPtr, mouseEventReceiver, useInputRegion)
        } catch (e: Throwable) {
            Main.showError(localize("WaylandLibraryErrorMessage"), e)
            exitProcess(0)
        }
    }

    override fun isVisible() = true

    override fun setVisible(b: Boolean) {}

    override fun isShowing() = true

    override fun getLocationOnScreen(): Point = absoluteLocation

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        if (isDisposed) return

        super.setBounds(x, y, width, height)

        try {
            WaylandLib.setBounds(WaylandLib.senderPtr, layerIndex, x, y, width, height)
        } catch (e: Throwable) {
            Main.showError(localize("WaylandLibraryErrorMessage"), e)
            exitProcess(0)
        }
    }

    override fun setCursor(cursor: Cursor) {
        if (isDisposed) return

        try {
            WaylandLib.setCursor(WaylandLib.senderPtr, cursor.type == Cursor.HAND_CURSOR)
        } catch (e: Throwable) {
            Main.showError(localize("WaylandLibraryErrorMessage"), e)
            exitProcess(0)
        }
    }

    /**
     * Displays the given [rgb] pixel data on the layer surface.
     */
    fun setImage(rgb: IntArray) {
        if (isDisposed) return

        try {
            WaylandLib.setImage(WaylandLib.senderPtr, layerIndex, rgb)
        } catch (e: Throwable) {
            Main.showError(localize("WaylandLibraryErrorMessage"), e)
            exitProcess(0)
        }
    }

    /**
     * Destroys the layer surface.
     */
    fun dispose() {
        if (isDisposed) return

        try {
            WaylandLib.dispose(WaylandLib.senderPtr, layerIndex)
        } catch (e: Throwable) {
            Main.showError(localize("WaylandLibraryErrorMessage"), e)
            exitProcess(0)
        }

        isDisposed = true // prevents segfault
    }

    /**
     * Sends mouse events to the event listeners attached to [component].
     *
     * If [leftPressed] or [rightPressed] is `true`, a `MOUSE_PRESSED` event will be sent. If [leftReleased] or
     * [rightReleased] is `true`, a `MOUSE_RELEASED` event will be sent. If [positionX] or [positionY] is different from
     * [relativeCursorPosition], a `MOUSE_MOVED` event will be sent.
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
        // Block mouse events when modal JDialog menus are open
        if (Window.getWindows().any { it is JDialog && it.isModal && it.isVisible }) return

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

        // If the compositor does not provide the absolute cursor position, stop updating absoluteLocation while the
        // left mouse button is held down.
        leftHeld = leftPressed || (leftHeld && !leftReleased)
        if (!leftHeld || Platform.providesAbsoluteCursorPosition) {
            absoluteLocation = location
        }

        if (leftPressed || rightPressed) {
            component.dispatchEvent(
                MouseEvent(
                    component,
                    MouseEvent.MOUSE_PRESSED,
                    System.currentTimeMillis(),
                    modifiers,
                    if (component is WaylandLayer) absoluteCursorPosition.x - absoluteLocation.x else relativeCursorPosition.x,
                    if (component is WaylandLayer) absoluteCursorPosition.y - absoluteLocation.y else relativeCursorPosition.y,
                    1,
                    rightPressed,
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
                    if (component is WaylandLayer) absoluteCursorPosition.x - absoluteLocation.x else relativeCursorPosition.x,
                    if (component is WaylandLayer) absoluteCursorPosition.y - absoluteLocation.y else relativeCursorPosition.y,
                    1,
                    false,
                    button
                )
            )
        }

        if (relativeCursorPosition.x != positionX || relativeCursorPosition.y != positionY) {
            relativeCursorPosition = Point(positionX, positionY)

            // If the compositor does not provide the absolute cursor position, estimate it by adding the relative
            // cursor position to the absolute position of the layer surface.
            if (!Platform.providesAbsoluteCursorPosition) {
                absoluteCursorPosition = Point(absoluteLocation.x + positionX, absoluteLocation.y + positionY)
            }

            component.dispatchEvent(
                MouseEvent(
                    component,
                    MouseEvent.MOUSE_MOVED,
                    System.currentTimeMillis(),
                    modifiers,
                    // If component is a mascot, use the cursor position relative to absoluteLocation. If component is a
                    // popup menu element, use the relative cursor position from the compositor.
                    if (component is WaylandLayer) absoluteCursorPosition.x - absoluteLocation.x else relativeCursorPosition.x,
                    if (component is WaylandLayer) absoluteCursorPosition.y - absoluteLocation.y else relativeCursorPosition.y,
                    0,
                    false,
                    button
                )
            )
        }
    }
}
