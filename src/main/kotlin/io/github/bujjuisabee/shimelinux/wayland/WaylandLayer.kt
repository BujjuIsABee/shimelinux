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
import com.group_finity.mascot.localize
import java.awt.Component
import java.awt.Cursor
import java.awt.Point
import java.awt.event.MouseEvent
import kotlin.system.exitProcess

/**
 * Creates a Wayland layer surface via [WaylandLib]
 *
 * @see WaylandLib.createLayer
 */
class WaylandLayer(receiver: WaylandLib.MouseEventReceiver, private val useMask: Boolean) : Component() {
    private val senderPtr: Long
    private var isDisposed = false
    private var previousCursorPosition = Point(0, 0)
    private var absoluteLocation = location
    private var isDragging = false

    init {
        try {
            senderPtr = WaylandLib.createLayer(receiver)
        } catch (e: Exception) {
            Main.showError(localize("SevereShimejiErrorErrorMessage"), e)
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
            WaylandLib.setBounds(senderPtr, x, y, width, height)
        } catch (e: Exception) {
            Main.showError(localize("SevereShimejiErrorErrorMessage"), e)
            exitProcess(0)
        }
    }

    override fun setCursor(cursor: Cursor) {
        if (isDisposed) return

        try {
            WaylandLib.setCursor(senderPtr, cursor.type == Cursor.HAND_CURSOR)
        } catch (e: Exception) {
            Main.showError(localize("SevereShimejiErrorErrorMessage"), e)
            exitProcess(0)
        }
    }

    /**
     * Displays an image on the layer
     *
     * @see WaylandLib.setImage
     */
    fun setImage(rgb: IntArray) {
        if (isDisposed) return

        try {
            WaylandLib.setImage(senderPtr, rgb, useMask)
        } catch (e: Exception) {
            Main.showError(localize("SevereShimejiErrorErrorMessage"), e)
            exitProcess(0)
        }
    }

    /**
     * Destroys the layer surface
     *
     * The event sender will be freed and any function that uses it will not execute
     */
    fun dispose() {
        if (isDisposed) return

        try {
            WaylandLib.dispose(senderPtr)
        } catch (e: Exception) {
            Main.showError(localize("SevereShimejiErrorErrorMessage"), e)
            exitProcess(0)
        }

        isDisposed = true // prevents segmentation fault
    }

    /**
     * Sends `MOUSE_PRESSED`, `MOUSE_RELEASED`, `MOUSE_MOVED`, and `MOUSE_DRAGGED` events to the event listeners of the [component]
     */
    @Suppress("KotlinConstantConditions")
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
            modifiers = modifiers or MouseEvent.BUTTON1_DOWN_MASK
            button = button or MouseEvent.BUTTON1
        }
        if (rightPressed || rightReleased) {
            modifiers = modifiers or MouseEvent.BUTTON3_DOWN_MASK
            button = button or MouseEvent.BUTTON3
        }

        if (leftPressed) {
            isDragging = true
        } else if (leftReleased) {
            isDragging = false
        }

        if (!isDragging) {
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
        if (previousCursorPosition != newCursorPosition) {
            previousCursorPosition = newCursorPosition
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
