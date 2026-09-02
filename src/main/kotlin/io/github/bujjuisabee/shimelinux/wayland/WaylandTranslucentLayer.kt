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

import com.group_finity.mascot.image.NativeImage
import com.group_finity.mascot.image.TranslucentWindow
import io.github.bujjuisabee.shimelinux.generic.GenericNativeImage

/**
 * A translucent window that displays a mascot on a [WaylandLayer]
 *
 * @author Bujju
 */
class WaylandTranslucentLayer : TranslucentWindow, WaylandLib.MouseEventReceiver {
    private val layer = WaylandLayer(this, true)
    private var image: GenericNativeImage? = null
    private var imageChanged = false

    override fun asComponent() = layer

    override fun setImage(image: NativeImage) {
        if (this.image != image) {
            imageChanged = true
            this.image = image as GenericNativeImage
        }
    }

    override fun updateImage() {
        val image = image
        if (imageChanged && image != null) {
            layer.setImage(image.rgb)
            imageChanged = false
        }
    }

    override fun setAlwaysOnTop(onTop: Boolean) {}

    override fun dispose() {
        layer.dispose()
    }

    @Suppress("unused")
    override fun updateCursor(
        leftPressed: Boolean,
        rightPressed: Boolean,
        leftReleased: Boolean,
        rightReleased: Boolean,
        positionX: Int,
        positionY: Int
    ) {
        layer.dispatchEvents(
            layer,
            leftPressed,
            rightPressed,
            leftReleased,
            rightReleased,
            positionX,
            positionY
        )
    }
}
