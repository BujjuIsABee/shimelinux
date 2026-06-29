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

package com.group_finity.mascot.virtual

import com.group_finity.mascot.image.NativeImage
import com.group_finity.mascot.image.TranslucentWindow
import javax.swing.JPanel

class VirtualTranslucentPanel : JPanel(), TranslucentWindow {
    private var image: VirtualNativeImage? = null

    override fun asComponent() = this

    override fun setImage(image: NativeImage) {
        this.image = image as VirtualNativeImage
    }

    override fun setAlwaysOnTop(onTop: Boolean) {}

    override fun dispose() {
        val parent = this.parent
        if (parent != null) {
            parent.remove(this)
            parent.repaint()
        }
    }

    override fun updateImage() {
        repaint()
    }

    override fun contains(x: Int, y: Int): Boolean {
        if (super.contains(x, y)) {
            try {
                // return image.managedImage.getRGB(x, y) and 0xff000000 >>> 24 > 0
            } catch (_: Exception) {
                return false
            }
        }
        return false
    }
}
