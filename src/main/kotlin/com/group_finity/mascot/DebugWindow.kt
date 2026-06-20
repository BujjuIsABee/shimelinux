/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot

import java.awt.Font
import javax.imageio.ImageIO
import javax.swing.GroupLayout
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.LayoutStyle

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

        val layout = GroupLayout(contentPane)
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(shimejiXLabel)
                                .addComponent(shimejiYLabel)
                                .addComponent(behaviorLabel)
                                .addComponent(windowXLabel)
                                .addComponent(windowYLabel)
                                .addComponent(windowWidthLabel)
                                .addComponent(windowHeightLabel)
                                .addComponent(environmentXLabel)
                                .addComponent(environmentYLabel)
                                .addComponent(environmentWidthLabel)
                                .addComponent(environmentHeightLabel)
                                .addComponent(activeIELabel)
                        )
                        .addGap(42, 42, 42)
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(behaviorValue, GroupLayout.DEFAULT_SIZE, 165, Int.MAX_VALUE)
                                .addGroup(
                                    layout.createSequentialGroup()
                                        .addGroup(
                                            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(shimejiYValue)
                                                .addComponent(shimejiXValue)
                                                .addComponent(windowXValue)
                                                .addComponent(windowYValue)
                                                .addComponent(windowHeightValue)
                                                .addComponent(environmentHeightValue)
                                                .addComponent(environmentWidthValue)
                                                .addComponent(windowWidthValue)
                                                .addComponent(environmentXValue)
                                                .addComponent(environmentYValue)
                                                .addComponent(activeIEValue)
                                        )
                                        .addGap(0, 0, Int.MAX_VALUE)
                                )
                        )
                        .addContainerGap()
                )
        )
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(
                    layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(behaviorLabel)
                                .addComponent(behaviorValue)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(shimejiXLabel)
                                .addComponent(shimejiXValue)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(shimejiYValue)
                                .addComponent(shimejiYLabel)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(activeIELabel)
                                .addComponent(activeIEValue)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(windowXLabel)
                                .addComponent(windowXValue)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(windowYLabel)
                                .addComponent(windowYValue)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(windowWidthLabel)
                                .addComponent(windowWidthValue)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(windowHeightLabel)
                                .addComponent(windowHeightValue, GroupLayout.Alignment.TRAILING)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(environmentXValue)
                                .addComponent(environmentXLabel)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(environmentYLabel)
                                .addComponent(environmentYValue)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(environmentWidthLabel)
                                .addComponent(environmentWidthValue)
                        )
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(
                            layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(environmentHeightLabel)
                                .addComponent(environmentHeightValue)
                        )
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Int.MAX_VALUE)
                )
        )

        val icon = this::class.java.getResourceAsStream("/img/icon.png").use { ImageIO.read(it) }
        iconImage = icon
        title = imageSet
        defaultCloseOperation = DISPOSE_ON_CLOSE
        contentPane.layout = layout
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
