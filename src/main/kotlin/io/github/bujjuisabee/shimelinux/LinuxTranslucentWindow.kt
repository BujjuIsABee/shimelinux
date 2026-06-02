/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package io.github.bujjuisabee.shimelinux

import com.group_finity.mascot.image.NativeImage
import com.group_finity.mascot.image.TranslucentWindow
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.GraphicsConfiguration
import javax.swing.JWindow

class LinuxTranslucentWindow : TranslucentWindow, JWindow() {
    private var image: LinuxNativeImage? = null
    private var gc: GraphicsConfiguration? = null

    init {
        background = Color(0, 0, 0, 0)
    }

    override fun asComponent(): Component {
        return this
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

    override fun paint(g: Graphics?) {
        super.paint(g)
        if (image != null) {
            g?.drawImage(image!!.managedImage, 0, 0, null)
        }
    }
}
