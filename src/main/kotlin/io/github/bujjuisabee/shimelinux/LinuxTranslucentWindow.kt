/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
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
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import javax.swing.JPanel
import javax.swing.JWindow

class LinuxTranslucentWindow : TranslucentWindow, JWindow() {
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
                    g.drawImage(image!!, offset.x, offset.y, null)
                }
            }
        }
    }

    override fun asComponent() = this

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        val screenBounds = NativeFactory.instance.getEnvironment().screen.toRectangle()
        val windowBounds = Rectangle(x, y, width, height)
        val newBounds = screenBounds.intersection(windowBounds)

        // Allow mascots to go offscreen by resizing the window and offsetting the image
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
        if (imageChanged) {
            repaint()
            imageChanged = false
        }
    }

    override fun setVisible(b: Boolean) {
        super.setVisible(b)
    }

    private fun setWindowMask() {
        if (image == null) return
        val image = image!!

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

        val mask = maskCache[image]!!
        shape = mask.createTransformedArea(AffineTransform.getTranslateInstance(offset.x.toDouble(), offset.y.toDouble()))
    }

    override fun getGraphicsConfiguration() = gc
}
