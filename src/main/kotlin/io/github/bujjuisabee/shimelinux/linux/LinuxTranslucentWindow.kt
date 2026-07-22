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

import com.group_finity.mascot.Main
import com.group_finity.mascot.NativeFactory
import com.group_finity.mascot.image.NativeImage
import com.group_finity.mascot.image.TranslucentWindow
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.Rectangle
import java.awt.geom.AffineTransform
import java.awt.geom.Area
import java.awt.geom.Path2D
import javax.swing.JWindow
import javax.swing.UIManager

class LinuxTranslucentWindow : TranslucentWindow {
    private val window: JWindow
    private var image: LinuxNativeImage? = null
    private var imageChanged = false
    private var offset = Point(0, 0)

    init {
        System.setProperty("sun.awt.noerasebackground", "true") // Reduces flickering

        // Initialize window with a LaF that supports transparency
        UIManager.getLookAndFeel().let { previous ->
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())
            window = object : JWindow(Main.frame) {
                init {
                    background = Color(0, 0, 0, 0)
                    name = ""
                }

                override fun paint(g: Graphics) {
                    val g2d = g as Graphics2D
                    g2d.composite = AlphaComposite.Src
                    g2d.drawImage(image?.managedImage, offset.x, offset.y, null)
                    g2d.dispose()
                }

                override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
                    val screenBounds = NativeFactory.instance.environment.screen.toRectangle()
                    val windowBounds = Rectangle(x, y, width, height)
                    val newBounds = screenBounds.intersection(windowBounds)

                    // Allow mascots to go partially offscreen by offsetting the image and resizing the window
                    offset = Point(windowBounds.x - newBounds.x, windowBounds.y - newBounds.y)
                    super.setBounds(
                        newBounds.x,
                        newBounds.y,
                        newBounds.width,
                        newBounds.height
                    )
                }

                override fun getGraphicsConfiguration() = gc ?: super.graphicsConfiguration
            }
            UIManager.setLookAndFeel(previous)
        }
    }

    override fun asComponent() = window

    override fun setImage(image: NativeImage) {
        if (image is LinuxNativeImage && this.image != image) {
            imageChanged = true
            this.image = image
        }
    }

    override fun updateImage() {
        setWindowMask()

        if (imageChanged) {
            imageChanged = false
            window.repaint()
        }
    }

    override fun setAlwaysOnTop(onTop: Boolean) {
        window.isAlwaysOnTop = onTop
    }

    override fun dispose() {
        window.dispose()
    }

    private fun setWindowMask() {
        val image = image ?: return

        val mask = maskCache.getOrPut(image) {
            val path = Path2D.Double()
            val rect = Rectangle(0, 0, 1, 1)
            for (i in 0 until image.width * image.height) {
                val alpha = (image.rgb[i] shr 24) and 0xFF
                if (alpha > 0) {
                    rect.x = i % image.width
                    rect.y = i / image.width
                    path.append(rect, false)
                }
            }
            return@getOrPut Area(path)
        }

        if (!mask.isEmpty) {
            window.shape = mask.createTransformedArea(
                AffineTransform.getTranslateInstance(
                    offset.x.toDouble(),
                    offset.y.toDouble()
                )
            )
        }
    }

    companion object {
        private val maskCache = mutableMapOf<LinuxNativeImage, Area>()
        private val gc = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .defaultScreenDevice
            .configurations
            .firstOrNull { it.isTranslucencyCapable }
    }
}
