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
import java.awt.Rectangle
import java.awt.event.MouseListener

class WaylandLayer : TranslucentWindow, Component() {
    private val component = object : Component() {
        override fun isVisible(): Boolean {
            return true
        }

        override fun setVisible(b: Boolean) {
        }

        override fun getBounds(): Rectangle {
            return Rectangle(0, 0, 0, 0)
        }

        override fun setBounds(r: Rectangle) {
            lib.setBounds(sender, r.x, r.y, r.width, r.height)
        }

        override fun addMouseListener(l: MouseListener?) {
        }

        override fun requestFocus() {
        }
    }

    private val lib = WaylandLib.INSTANCE
    private val sender: Int = lib.createMascot()
    private var image: LinuxNativeImage? = null
    private var imageChanged = false

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
            lib.updateImage(sender, image!!.rgb)
        }
    }

    override fun setAlwaysOnTop(onTop: Boolean) {

    }

    override fun dispose() {

    }
}
