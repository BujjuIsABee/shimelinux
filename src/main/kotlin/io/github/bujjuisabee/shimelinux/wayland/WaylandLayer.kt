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
import com.group_finity.mascot.getProperty
import com.group_finity.mascot.localize
import io.github.bujjuisabee.shimelinux.wayland.lib.MouseEventReceiver
import io.github.bujjuisabee.shimelinux.wayland.lib.WaylandLib
import java.awt.Component
import java.awt.Cursor
import java.awt.Point
import java.awt.Window
import java.awt.event.MouseEvent
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JDialog
import kotlin.system.exitProcess

/**
 * The absolute cursor position, or an approximation of it based on the [relativeCursorPosition] if
 * [Platform.providesAbsoluteCursorPosition] is `false`.
 */
val absoluteCursorPosition = Point(0, 0)

/**
 * The relative cursor position provided by the compositor.
 */
val relativeCursorPosition = Point(0, 0)

private val lastId = AtomicInteger()

/**
 * A Wayland layer surface.
 *
 * Mouse events will be sent to [mouseEventReceiver]. If [useInputRegion] is `true`, mouse events on fully transparent
 * pixels will be ignored.
 *
 * @author Bujju
 */
class WaylandLayer(mouseEventReceiver: MouseEventReceiver, private val useInputRegion: Boolean) : Component() {
    private val id: Int = lastId.incrementAndGet()
    private var absoluteLocation: Point = location
    private var leftHeld = false
    private var isDisposed = false

    init {
        try {
            WaylandLib.createLayer(id, getProperty("MascotLayerWayland", 3), mouseEventReceiver, useInputRegion)
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
            WaylandLib.setBounds(id, x, y, width, height)
        } catch (e: Throwable) {
            Main.showError(localize("WaylandLibraryErrorMessage"), e)
            exitProcess(0)
        }
    }

    override fun setCursor(cursor: Cursor) {
        if (isDisposed) return

        try {
            WaylandLib.setCursor(cursor.type == Cursor.HAND_CURSOR)
        } catch (e: Throwable) {
            Main.showError(localize("WaylandLibraryErrorMessage"), e)
            exitProcess(0)
        }
    }

    /**
     * Displays the [rgb] pixel data on the layer surface.
     */
    fun setImage(rgb: IntArray) {
        if (isDisposed) return

        try {
            WaylandLib.setImage(id, rgb)
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
            WaylandLib.dispose(id)
        } catch (e: Throwable) {
            Main.showError(localize("WaylandLibraryErrorMessage"), e)
            exitProcess(0)
        }

        isDisposed = true
    }

    /**
     * Sends mouse events to the event listeners attached to the given [component].
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

        // If the absolute cursor position has to be estimated, stop updating absoluteLocation while the left mouse
        // button is held so mascots don't fly offscreen when dragged
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
            relativeCursorPosition.setLocation(positionX, positionY)

            // Estimate the absolute cursor position if the compositor does not provide it
            if (!Platform.providesAbsoluteCursorPosition) {
                absoluteCursorPosition.setLocation(absoluteLocation.x + positionX, absoluteLocation.y + positionY)
            }

            component.dispatchEvent(
                MouseEvent(
                    component,
                    MouseEvent.MOUSE_MOVED,
                    System.currentTimeMillis(),
                    modifiers,
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
