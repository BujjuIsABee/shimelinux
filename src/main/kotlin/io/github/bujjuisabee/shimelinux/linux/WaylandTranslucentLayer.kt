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

import com.group_finity.mascot.image.NativeImage
import com.group_finity.mascot.image.TranslucentWindow
import java.awt.Component
import java.awt.Cursor
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseEvent

class WaylandTranslucentLayer : TranslucentWindow {
    private val component = object : Component() {
        private var bounds = super.bounds

        override fun isVisible() = true

        override fun setVisible(b: Boolean) {}

        override fun getBounds() = bounds

        override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
            bounds = Rectangle(x, y, width, height)
            lib.setBounds(senderIndex, x, y, width, height)
        }

        override fun isShowing() = true

        override fun getLocationOnScreen() = Point(bounds.x, bounds.y)

        override fun setCursor(cursor: Cursor) {
            lib.setCursor(senderIndex, cursor.type == Cursor.HAND_CURSOR)
        }
    }

    private val lib = requireNotNull(WaylandLib.instance)
    private val senderIndex: Int = lib.createMascot(this)
    private var image: LinuxNativeImage? = null
    private var imageChanged = false
    private var previousCursorPosition = Point(0, 0)

    override fun asComponent() = component

    override fun setImage(image: NativeImage) {
        if (image is LinuxNativeImage && this.image != image) {
            this.image = image
            imageChanged = true
        }
    }

    override fun updateImage() {
        if (image != null) {
            imageChanged = false
            lib.setImage(senderIndex, image!!.rgb)
        }
    }

    override fun setAlwaysOnTop(onTop: Boolean) {}

    override fun dispose() {
        lib.dispose(senderIndex)
    }

    @Suppress("unused")
    fun updateCursor(
        leftPressed: Boolean,
        rightPressed: Boolean,
        leftReleased: Boolean,
        rightReleased: Boolean,
        positionX: Int,
        positionY: Int,
    ) {
        val newCursorPosition = Point(positionX, positionY)
        val (modifiers, button) = if (leftPressed || leftReleased) {
            Pair(MouseEvent.BUTTON1_DOWN_MASK, MouseEvent.BUTTON1)
        } else if (rightPressed || rightReleased) {
            Pair(MouseEvent.BUTTON3_DOWN_MASK, MouseEvent.BUTTON3)
        } else {
            Pair(MouseEvent.NOBUTTON, MouseEvent.NOBUTTON)
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

        if (previousCursorPosition != newCursorPosition) {
            previousCursorPosition = newCursorPosition
            component.dispatchEvent(
                MouseEvent(
                    component,
                    if (leftPressed || rightPressed) MouseEvent.MOUSE_DRAGGED else MouseEvent.MOUSE_MOVED,
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
    }
}
