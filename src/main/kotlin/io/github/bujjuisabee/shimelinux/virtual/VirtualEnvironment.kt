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

package io.github.bujjuisabee.shimelinux.virtual

import com.group_finity.mascot.Main
import com.group_finity.mascot.NativeFactory
import com.group_finity.mascot.environment.Area
import com.group_finity.mascot.environment.Environment
import com.group_finity.mascot.getProperty
import com.group_finity.mascot.loadResource
import java.awt.Color
import java.awt.Dimension
import java.awt.MouseInfo
import java.awt.Point
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.SwingUtilities

class VirtualEnvironment : Environment() {
    override val workArea: Area
        get() = screen

    override val activeIE = Area()
    override val activeIETitle = ""

    private val display = JFrame()

    init {
        val icon = loadResource("/img/icon.png").use { ImageIO.read(it) }
        display.iconImage = icon
        display.title = "ShimeLinux"
        display.isResizable = !NativeFactory.waylandLayersSupported
        display.isAutoRequestFocus = false
        display.addWindowListener(object : WindowListener {
            override fun windowOpened(e: WindowEvent) {}

            override fun windowClosing(e: WindowEvent) {
                Main.instance.exit()
            }

            override fun windowClosed(e: WindowEvent) {}

            override fun windowIconified(e: WindowEvent) {}

            override fun windowDeiconified(e: WindowEvent) {}

            override fun windowActivated(e: WindowEvent) {}

            override fun windowDeactivated(e: WindowEvent) {}
        })

        val windowArray = getProperty("WindowSize", "600x500").split("x")
        display.contentPane = VirtualContentPanel(
            Dimension(windowArray[0].toInt(), windowArray[1].toInt()),
            Color.decode(getProperty("Background", "#00FF00")),
        )

        display.pack()
        display.isVisible = true
        display.toFront()
        tick()
    }

    override fun tick() {
        if (display.isVisible) {
            screenRect.bounds = display.contentPane.bounds
            screen.set(screenRect)
        }

        val info = MouseInfo.getPointerInfo()
        var point = Point(0, 0)
        if (info != null && display.isVisible) {
            point = info.location
            SwingUtilities.convertPointFromScreen(point, display.contentPane)
        }
        cursor.set(point)
    }

    override fun moveActiveIE(point: Point) {}

    override fun restoreIE() {}

    override fun refreshCache() {}

    override fun dispose() {
        display.dispose()
    }

    fun addShimeji(shimeji: JPanel) {
        SwingUtilities.invokeLater {
            if (display.contentPane.size.width > 0 && display.contentPane.size.height > 0) {
                display.preferredSize = display.size
                display.rootPane.preferredSize = display.rootPane.size
                display.contentPane.preferredSize = display.contentPane.size
            }
            shimeji.isOpaque = false
            display.contentPane.add(shimeji)
        }
    }
}
