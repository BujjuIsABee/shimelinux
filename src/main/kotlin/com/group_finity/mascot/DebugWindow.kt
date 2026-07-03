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

package com.group_finity.mascot

import java.awt.Font
import java.awt.GridLayout
import javax.imageio.ImageIO
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel

class DebugWindow(imageSet: String) : JFrame() {
    private val panel: JPanel
    private val behaviorLabel: JLabel
    private val behaviorValue: JLabel
    private val shimejiXLabel: JLabel
    private val shimejiXValue: JLabel
    private val shimejiYLabel: JLabel
    private val shimejiYValue: JLabel
    private val activeIELabel: JLabel
    private val activeIEValue: JLabel
    private val windowXLabel: JLabel
    private val windowXValue: JLabel
    private val windowYLabel: JLabel
    private val windowYValue: JLabel
    private val windowWidthLabel: JLabel
    private val windowWidthValue: JLabel
    private val windowHeightLabel: JLabel
    private val windowHeightValue: JLabel
    private val environmentXLabel: JLabel
    private val environmentXValue: JLabel
    private val environmentYLabel: JLabel
    private val environmentYValue: JLabel
    private val environmentWidthLabel: JLabel
    private val environmentWidthValue: JLabel
    private val environmentHeightLabel: JLabel
    private val environmentHeightValue: JLabel

    init {
        val icon = loadResource("/img/icon.png").use { ImageIO.read(it) }
        iconImage = icon
        title = imageSet
        isResizable = false
        defaultCloseOperation = DISPOSE_ON_CLOSE

        behaviorLabel = JLabel("Behavior".localize())
        behaviorLabel.font = behaviorLabel.font.deriveFont(Font.BOLD)
        behaviorValue = JLabel("N/A")
        behaviorValue.alignmentX = LEFT_ALIGNMENT

        shimejiXLabel = JLabel("ShimejiX".localize())
        shimejiXLabel.font = shimejiXLabel.font.deriveFont(Font.BOLD)
        shimejiXValue = JLabel("N/A")
        shimejiXValue.alignmentX = LEFT_ALIGNMENT

        shimejiYLabel = JLabel("ShimejiY".localize())
        shimejiYLabel.font = shimejiYLabel.font.deriveFont(Font.BOLD)
        shimejiYValue = JLabel("N/A")
        shimejiYValue.alignmentX = LEFT_ALIGNMENT

        activeIELabel = JLabel("WindowTitle".localize())
        activeIELabel.font = activeIELabel.font.deriveFont(Font.BOLD)
        activeIEValue = JLabel("N/A")
        activeIEValue.alignmentX = LEFT_ALIGNMENT

        windowXLabel = JLabel("WindowX".localize())
        windowXLabel.font = windowXLabel.font.deriveFont(Font.BOLD)
        windowXValue = JLabel("N/A")
        windowXValue.alignmentX = LEFT_ALIGNMENT

        windowYLabel = JLabel("WindowY".localize())
        windowYLabel.font = windowYLabel.font.deriveFont(Font.BOLD)
        windowYValue = JLabel("N/A")
        windowYValue.alignmentX = LEFT_ALIGNMENT

        windowWidthLabel = JLabel("WindowWidth".localize())
        windowWidthLabel.font = windowWidthLabel.font.deriveFont(Font.BOLD)
        windowWidthValue = JLabel("N/A")
        windowWidthValue.alignmentX = LEFT_ALIGNMENT

        windowHeightLabel = JLabel("WindowHeight".localize())
        windowHeightLabel.font = windowHeightLabel.font.deriveFont(Font.BOLD)
        windowHeightValue = JLabel("N/A")
        windowHeightValue.alignmentX = LEFT_ALIGNMENT

        environmentXLabel = JLabel("EnvironmentX".localize())
        environmentXLabel.font = environmentXLabel.font.deriveFont(Font.BOLD)
        environmentXValue = JLabel("N/A")
        environmentXValue.alignmentX = LEFT_ALIGNMENT

        environmentYLabel = JLabel("EnvironmentY".localize())
        environmentYLabel.font = environmentYLabel.font.deriveFont(Font.BOLD)
        environmentYValue = JLabel("N/A")
        environmentYValue.alignmentX = LEFT_ALIGNMENT

        environmentWidthLabel = JLabel("EnvironmentWidth".localize())
        environmentWidthLabel.font = environmentWidthLabel.font.deriveFont(Font.BOLD)
        environmentWidthValue = JLabel("N/A")
        environmentWidthValue.alignmentX = LEFT_ALIGNMENT

        environmentHeightLabel = JLabel("EnvironmentHeight".localize())
        environmentHeightLabel.font = environmentHeightLabel.font.deriveFont(Font.BOLD)
        environmentHeightValue = JLabel("N/A")
        environmentHeightValue.alignmentX = LEFT_ALIGNMENT

        panel = JPanel(GridLayout(12, 2, 42, 0))
        panel.border = BorderFactory.createEmptyBorder(0, 0, 0, 42)
        panel.add(behaviorLabel)
        panel.add(behaviorValue)
        panel.add(shimejiXLabel)
        panel.add(shimejiXValue)
        panel.add(shimejiYLabel)
        panel.add(shimejiYValue)
        panel.add(activeIELabel)
        panel.add(activeIEValue)
        panel.add(windowXLabel)
        panel.add(windowXValue)
        panel.add(windowYLabel)
        panel.add(windowYValue)
        panel.add(windowWidthLabel)
        panel.add(windowWidthValue)
        panel.add(windowHeightLabel)
        panel.add(windowHeightValue)
        panel.add(environmentXLabel)
        panel.add(environmentXValue)
        panel.add(environmentYLabel)
        panel.add(environmentYValue)
        panel.add(environmentWidthLabel)
        panel.add(environmentWidthValue)
        panel.add(environmentHeightLabel)
        panel.add(environmentHeightValue)

        add(panel)
        pack()
        setLocationRelativeTo(null)
    }

    fun setShimejiX(x: Int) {
        shimejiXValue.text = x.toString()
    }

    fun setShimejiY(y: Int) {
        shimejiYValue.text = y.toString()
    }

    fun setWindowX(x: Int) {
        windowXValue.text = x.toString()
    }

    fun setWindowY(y: Int) {
        windowYValue.text = y.toString()
    }

    fun setWindowWidth(width: Int) {
        windowWidthValue.text = width.toString()
    }

    fun setWindowHeight(height: Int) {
        windowHeightValue.text = height.toString()
    }

    fun setBehavior(behavior: String) {
        behaviorValue.text = behavior
    }

    fun setEnvironmentX(x: Int) {
        environmentXValue.text = x.toString()
    }

    fun setEnvironmentY(y: Int) {
        environmentYValue.text = y.toString()
    }

    fun setEnvironmentWidth(width: Int) {
        environmentWidthValue.text = width.toString()
    }

    fun setEnvironmentHeight(height: Int) {
        environmentHeightValue.text = height.toString()
    }

    fun setWindowTitle(title: String) {
        activeIEValue.text = title
    }
}
