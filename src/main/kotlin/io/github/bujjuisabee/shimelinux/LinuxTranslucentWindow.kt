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

package io.github.bujjuisabee.shimelinux

import com.group_finity.mascot.NativeFactory
import com.group_finity.mascot.image.NativeImage
import com.group_finity.mascot.image.TranslucentWindow
import java.awt.Color
import java.awt.Graphics
import java.awt.GraphicsConfiguration
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import javax.swing.JPanel
import javax.swing.JWindow

class LinuxTranslucentWindow : TranslucentWindow, JWindow() {
    // Get a translucency capable graphics configuration
    private val gc: GraphicsConfiguration = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.configurations.first { it.isTranslucencyCapable }

    private var image: BufferedImage? = null
    private var imageChanged = false
    private var offset = Point(0, 0)

    private val maskCache = mutableMapOf<BufferedImage, Area>()

    init {
        background = Color(0, 0, 0, 0)
        contentPane = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                if (image != null) {
                    setWindowMask()
                    g.drawImage(image, offset.x, offset.y, null)
                }
            }
        }
    }

    override fun getGraphicsConfiguration() = gc

    override fun asComponent() = this

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        val screenBounds = NativeFactory.instance.getEnvironment().screen.toRectangle()
        val windowBounds = Rectangle(x, y, width, height)
        val newBounds = screenBounds.intersection(windowBounds)

        // Allow mascots to go partially offscreen by resizing the window and offsetting the image
        super.setBounds(newBounds.x, newBounds.y, newBounds.width, newBounds.height)
        offset = Point(windowBounds.x - newBounds.x, windowBounds.y - newBounds.y)
    }

    override fun setImage(image: NativeImage) {
        val newImage = (image as LinuxNativeImage).managedImage
        if (this.image != newImage) {
            imageChanged = true
            this.image = newImage
        }
    }

    override fun updateImage() {
        // Only repaint when the image has changed to reduce flickering
        if (imageChanged) {
            repaint()
            imageChanged = false
        }
    }

    private fun setWindowMask() {
        val image = image ?: return

        if (!maskCache.containsKey(image)) {
            val width = image.width
            val height = image.height
            val mask = Path2D.Double()

            for (x in 0 until width) {
                for (y in 0 until height) {
                    val color = Color(image.getRGB(x, y), true)
                    if (color.alpha > 0) {
                        mask.append(Rectangle(x, y, 1, 1), false)
                    }
                }
            }

            maskCache[image] = Area(mask)
        }

        maskCache[image]?.let {
            shape = if (!it.isEmpty) {
                it.createTransformedArea(
                    AffineTransform.getTranslateInstance(
                        offset.x.toDouble(),
                        offset.y.toDouble()
                    )
                )
            } else {
                null
            }
        }
    }
}
