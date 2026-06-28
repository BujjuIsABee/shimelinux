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
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsConfiguration
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JWindow

private val frame: JFrame by lazy {
    JFrame().apply {
        // Use blank image for the icon
        iconImage = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
    }
}

class LinuxTranslucentWindow : TranslucentWindow, JWindow(frame) {
    private var image: LinuxNativeImage? = null
    private var imageChanged = false
    private var offset = Point(0, 0)
    private val maskCache = mutableMapOf<LinuxNativeImage, Area>()

    // Get a graphics configuration that supports transparency
    private val gc: GraphicsConfiguration =
        GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.configurations.first { it.isTranslucencyCapable }

    init {
        name = ""

        // Make the window translucent
        background = Color(0, 0, 0, 0)

        // Prevents flickering when the background is repainted
        System.setProperty("sun.awt.noerasebackground", "true")
    }

    override fun getGraphicsConfiguration() = gc

    override fun asComponent() = this

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        val screenBounds = NativeFactory.instance.environment.screen.toRectangle()
        val windowBounds = Rectangle(x, y, width, height)
        val newBounds = screenBounds.intersection(windowBounds)

        // Allow mascots to go partially offscreen by resizing the window and offsetting the image
        super.setBounds(newBounds.x, newBounds.y, newBounds.width, newBounds.height)
        offset = Point(windowBounds.x - newBounds.x, windowBounds.y - newBounds.y)
    }

    override fun setImage(image: NativeImage) {
        if (image is LinuxNativeImage && this.image != image) {
            imageChanged = true
            this.image = image
        }
    }

    override fun updateImage() {
        // Redraw the image if it has been changed
        if (imageChanged) {
            repaint()
            imageChanged = false
        }
    }

    override fun paint(g: Graphics) {
        val image = image?.managedImage
        if (image != null) {
            setWindowMask()

            val g2d = g as Graphics2D
            g2d.composite = AlphaComposite.Src
            g2d.drawImage(image, offset.x, offset.y, null)
            g2d.dispose()
        }
    }

    private fun setWindowMask() {
        val nativeImage = image ?: return
        val mask = maskCache.getOrPut(nativeImage) {
            val image = nativeImage.managedImage
            val width = image.width
            val height = image.height
            val rgb = image.getRGB(0, 0, width, height, null, 0, width)

            val rect = Rectangle(0, 0, 1, 1)
            val mask = Path2D.Double()
            for (x in 0 until width) {
                for (y in 0 until height) {
                    val color = Color(rgb[y * width + x], true)
                    if (color.alpha > 0) {
                        rect.x = x
                        rect.y = y
                        mask.append(rect, false)
                    }
                }
            }

            Area(mask)
        }

        mask.takeUnless { it.isEmpty }?.let {
            shape = it.createTransformedArea(
                AffineTransform.getTranslateInstance(
                    offset.x.toDouble(),
                    offset.y.toDouble()
                )
            )
        }
    }
}
