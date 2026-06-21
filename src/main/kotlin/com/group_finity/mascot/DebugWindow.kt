/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
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
    private val behaviorValue: JLabel
    private val shimejiXValue: JLabel
    private val shimejiYValue: JLabel
    private val activeIEValue: JLabel
    private val windowXValue: JLabel
    private val windowYValue: JLabel
    private val windowWidthValue: JLabel
    private val windowHeightValue: JLabel
    private val environmentXValue: JLabel
    private val environmentYValue: JLabel
    private val environmentWidthValue: JLabel
    private val environmentHeightValue: JLabel

    init {
        val behaviorLabel = JLabel(Main.instance.languageBundle.getString("Behaviour"))
        behaviorLabel.font = behaviorLabel.font.deriveFont(Font.BOLD)
        behaviorValue = JLabel("N/A")
        behaviorValue.alignmentX = LEFT_ALIGNMENT

        val shimejiXLabel = JLabel(Main.instance.languageBundle.getString("ShimejiX"))
        shimejiXLabel.font = shimejiXLabel.font.deriveFont(Font.BOLD)
        shimejiXValue = JLabel("N/A")
        shimejiXValue.alignmentX = LEFT_ALIGNMENT

        val shimejiYLabel = JLabel(Main.instance.languageBundle.getString("ShimejiY"))
        shimejiYLabel.font = shimejiYLabel.font.deriveFont(Font.BOLD)
        shimejiYValue = JLabel("N/A")
        shimejiYValue.alignmentX = LEFT_ALIGNMENT

        val activeIELabel = JLabel(Main.instance.languageBundle.getString("ActiveIE"))
        activeIELabel.font = activeIELabel.font.deriveFont(Font.BOLD)
        activeIEValue = JLabel("N/A")
        activeIEValue.alignmentX = LEFT_ALIGNMENT

        val windowXLabel = JLabel(Main.instance.languageBundle.getString("WindowX"))
        windowXLabel.font = windowXLabel.font.deriveFont(Font.BOLD)
        windowXValue = JLabel("N/A")
        windowXValue.alignmentX = LEFT_ALIGNMENT

        val windowYLabel = JLabel(Main.instance.languageBundle.getString("WindowY"))
        windowYLabel.font = windowYLabel.font.deriveFont(Font.BOLD)
        windowYValue = JLabel("N/A")
        windowYValue.alignmentX = LEFT_ALIGNMENT

        val windowWidthLabel = JLabel(Main.instance.languageBundle.getString("WindowWidth"))
        windowWidthLabel.font = windowWidthLabel.font.deriveFont(Font.BOLD)
        windowWidthValue = JLabel("N/A")
        windowWidthValue.alignmentX = LEFT_ALIGNMENT

        val windowHeightLabel = JLabel(Main.instance.languageBundle.getString("WindowHeight"))
        windowHeightLabel.font = windowHeightLabel.font.deriveFont(Font.BOLD)
        windowHeightValue = JLabel("N/A")
        windowHeightValue.alignmentX = LEFT_ALIGNMENT

        val environmentXLabel = JLabel(Main.instance.languageBundle.getString("EnvironmentX"))
        environmentXLabel.font = environmentXLabel.font.deriveFont(Font.BOLD)
        environmentXValue = JLabel("N/A")
        environmentXValue.alignmentX = LEFT_ALIGNMENT

        val environmentYLabel = JLabel(Main.instance.languageBundle.getString("EnvironmentY"))
        environmentYLabel.font = environmentYLabel.font.deriveFont(Font.BOLD)
        environmentYValue = JLabel("N/A")
        environmentYValue.alignmentX = LEFT_ALIGNMENT

        val environmentWidthLabel = JLabel(Main.instance.languageBundle.getString("EnvironmentWidth"))
        environmentWidthLabel.font = environmentWidthLabel.font.deriveFont(Font.BOLD)
        environmentWidthValue = JLabel("N/A")
        environmentWidthValue.alignmentX = LEFT_ALIGNMENT

        val environmentHeightLabel = JLabel(Main.instance.languageBundle.getString("EnvironmentHeight"))
        environmentHeightLabel.font = environmentHeightLabel.font.deriveFont(Font.BOLD)
        environmentHeightValue = JLabel("N/A")
        environmentHeightValue.alignmentX = LEFT_ALIGNMENT

        val panel = JPanel(GridLayout(12, 2, 42, 0))
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

        val icon = this::class.java.getResourceAsStream("/img/icon.png").use { ImageIO.read(it) }
        iconImage = icon
        title = imageSet
        defaultCloseOperation = DISPOSE_ON_CLOSE
        add(panel)
        pack()
        setLocationRelativeTo(null)
    }

    //region Setters
    fun setShimejiX(x: Int) { shimejiXValue.text = x.toString() }
    fun setShimejiY(y: Int) { shimejiYValue.text = y.toString() }
    fun setWindowX(x: Int) { windowXValue.text = x.toString() }
    fun setWindowY(y: Int) { windowYValue.text = y.toString() }
    fun setWindowWidth(width: Int) { windowWidthValue.text = width.toString() }
    fun setWindowHeight(height: Int) { windowHeightValue.text = height.toString() }
    fun setBehavior(behavior: String) { behaviorValue.text = behavior }
    fun setEnvironmentX(x: Int) { environmentXValue.text = x.toString() }
    fun setEnvironmentY(y: Int) { environmentYValue.text = y.toString() }
    fun setEnvironmentWidth(width: Int) { environmentWidthValue.text = width.toString() }
    fun setEnvironmentHeight(height: Int) { environmentHeightValue.text = height.toString() }
    fun setWindowTitle(title: String) { activeIEValue.text = title }
    //endregion
}
