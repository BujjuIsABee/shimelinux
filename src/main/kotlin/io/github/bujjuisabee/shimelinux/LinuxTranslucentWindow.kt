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
import java.awt.Component
import java.awt.Graphics
import java.awt.GraphicsConfiguration
import java.awt.Point
import java.awt.Rectangle
import javax.swing.JPanel
import javax.swing.JWindow

class LinuxTranslucentWindow : TranslucentWindow, JWindow() {
    private var image: LinuxNativeImage? = null
    private var gc: GraphicsConfiguration? = null
    private var offset: Point = Point(0, 0)

    init {
        val panel = object : JPanel() {
            init {
                background = Color(0, 0, 0, 0)
            }

            override fun paintComponent(g: Graphics?) {
                if (image != null) {
                    g?.drawImage(image!!.managedImage, offset.x, offset.y, null)
                }
            }
        }

        contentPane = panel
        background = panel.background
    }

    override fun asComponent(): Component {
        return this
    }

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        val screenBounds = NativeFactory.instance.getEnvironment().screen.toRectangle()
        val windowBounds = Rectangle(x, y, width, height)
        val newBounds = screenBounds.intersection(windowBounds)

        // Allow mascots to go offscreen by resizing the window and offsetting the image
        super.setBounds(newBounds.x, newBounds.y, newBounds.width, newBounds.height)
        offset = Point(windowBounds.x - newBounds.x, windowBounds.y - newBounds.y)
    }

    override fun setImage(image: NativeImage) {
        this.image = image as LinuxNativeImage
    }

    override fun updateImage() {
        repaint()
    }

    override fun setVisible(b: Boolean) {
        super.setVisible(b)

        // Store the current graphics configuration
        if (b && gc == null) {
            gc = this.graphicsConfiguration
        }
    }

    override fun getGraphicsConfiguration(): GraphicsConfiguration? {
        // Use the stored graphics configuration
        // This fixes an issue with transparency
        return gc ?: super.getGraphicsConfiguration()
    }
}
