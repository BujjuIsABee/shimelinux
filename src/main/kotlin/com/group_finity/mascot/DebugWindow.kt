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

import com.group_finity.mascot.script.Variable
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.GridLayout
import java.awt.image.BaseMultiResolutionImage
import javax.imageio.ImageIO
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTabbedPane

/**
 * A menu that shows statistics about a mascot.
 *
 * @param imageSet The name of the mascot, which is used as the title of the window.
 *
 * @author Kilkakon
 * @author Bujju
 */
class DebugWindow(val imageSet: String) : JFrame() {
    private val statisticsPanel: JPanel
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
    private val tabbedPane: JTabbedPane
    private val behaviorsPanel: JPanel
    private val behaviorsList: JList<String>
    private val variablesPanel: JPanel
    private val variablesList: JList<String>

    init {
        iconImage = BaseMultiResolutionImage(
            loadResource("img/icon64.png").use { ImageIO.read(it) },
            loadResource("img/icon128.png").use { ImageIO.read(it) },
            loadResource("img/icon256.png").use { ImageIO.read(it) },
        )
        title = imageSet
        isResizable = false
        layout = BorderLayout()

        shimejiXLabel = JLabel(localize("ShimejiX"))
        shimejiXLabel.font = shimejiXLabel.font.deriveFont(Font.BOLD)
        shimejiXValue = JLabel("N/A")
        shimejiXValue.alignmentX = LEFT_ALIGNMENT

        shimejiYLabel = JLabel(localize("ShimejiY"))
        shimejiYLabel.font = shimejiYLabel.font.deriveFont(Font.BOLD)
        shimejiYValue = JLabel("N/A")
        shimejiYValue.alignmentX = LEFT_ALIGNMENT

        activeIELabel = JLabel(localize("WindowTitle"))
        activeIELabel.font = activeIELabel.font.deriveFont(Font.BOLD)
        activeIEValue = JLabel("N/A")
        activeIEValue.alignmentX = LEFT_ALIGNMENT

        windowXLabel = JLabel(localize("WindowX"))
        windowXLabel.font = windowXLabel.font.deriveFont(Font.BOLD)
        windowXValue = JLabel("N/A")
        windowXValue.alignmentX = LEFT_ALIGNMENT

        windowYLabel = JLabel(localize("WindowY"))
        windowYLabel.font = windowYLabel.font.deriveFont(Font.BOLD)
        windowYValue = JLabel("N/A")
        windowYValue.alignmentX = LEFT_ALIGNMENT

        windowWidthLabel = JLabel(localize("WindowWidth"))
        windowWidthLabel.font = windowWidthLabel.font.deriveFont(Font.BOLD)
        windowWidthValue = JLabel("N/A")
        windowWidthValue.alignmentX = LEFT_ALIGNMENT

        windowHeightLabel = JLabel(localize("WindowHeight"))
        windowHeightLabel.font = windowHeightLabel.font.deriveFont(Font.BOLD)
        windowHeightValue = JLabel("N/A")
        windowHeightValue.alignmentX = LEFT_ALIGNMENT

        environmentXLabel = JLabel(localize("EnvironmentX"))
        environmentXLabel.font = environmentXLabel.font.deriveFont(Font.BOLD)
        environmentXValue = JLabel("N/A")
        environmentXValue.alignmentX = LEFT_ALIGNMENT

        environmentYLabel = JLabel(localize("EnvironmentY"))
        environmentYLabel.font = environmentYLabel.font.deriveFont(Font.BOLD)
        environmentYValue = JLabel("N/A")
        environmentYValue.alignmentX = LEFT_ALIGNMENT

        environmentWidthLabel = JLabel(localize("EnvironmentWidth"))
        environmentWidthLabel.font = environmentWidthLabel.font.deriveFont(Font.BOLD)
        environmentWidthValue = JLabel("N/A")
        environmentWidthValue.alignmentX = LEFT_ALIGNMENT

        environmentHeightLabel = JLabel(localize("EnvironmentHeight"))
        environmentHeightLabel.font = environmentHeightLabel.font.deriveFont(Font.BOLD)
        environmentHeightValue = JLabel("N/A")
        environmentHeightValue.alignmentX = LEFT_ALIGNMENT

        statisticsPanel = JPanel(GridLayout(11, 2, 10, 0))
        statisticsPanel.border = BorderFactory.createEmptyBorder(10, 10, 10, 0)
        statisticsPanel.add(shimejiXLabel)
        statisticsPanel.add(shimejiXValue)
        statisticsPanel.add(shimejiYLabel)
        statisticsPanel.add(shimejiYValue)
        statisticsPanel.add(activeIELabel)
        statisticsPanel.add(activeIEValue)
        statisticsPanel.add(windowXLabel)
        statisticsPanel.add(windowXValue)
        statisticsPanel.add(windowYLabel)
        statisticsPanel.add(windowYValue)
        statisticsPanel.add(windowWidthLabel)
        statisticsPanel.add(windowWidthValue)
        statisticsPanel.add(windowHeightLabel)
        statisticsPanel.add(windowHeightValue)
        statisticsPanel.add(environmentXLabel)
        statisticsPanel.add(environmentXValue)
        statisticsPanel.add(environmentYLabel)
        statisticsPanel.add(environmentYValue)
        statisticsPanel.add(environmentWidthLabel)
        statisticsPanel.add(environmentWidthValue)
        statisticsPanel.add(environmentHeightLabel)
        statisticsPanel.add(environmentHeightValue)

        behaviorsList = JList()

        behaviorsPanel = JPanel()
        behaviorsPanel.add(JScrollPane(behaviorsList).apply { preferredSize = Dimension(245, 144) })

        variablesList = JList()

        variablesPanel = JPanel()
        variablesPanel.add(JScrollPane(variablesList).apply { preferredSize = Dimension(245, 144) })

        tabbedPane = JTabbedPane()
        tabbedPane.addTab(localize("Behaviors"), behaviorsPanel)
        tabbedPane.addTab(localize("Variables"), variablesPanel)

        add(statisticsPanel, BorderLayout.WEST)
        add(tabbedPane, BorderLayout.EAST)
        pack()
        setLocationRelativeTo(null)
    }

    /**
     * Updates the statistics.
     */
    fun set(
        shimejiX: Int,
        shimejiY: Int,
        activeIE: String,
        windowX: Int,
        windowY: Int,
        windowWidth: Int,
        windowHeight: Int,
        environmentX: Int,
        environmentY: Int,
        environmentWidth: Int,
        environmentHeight: Int,
        behavior: String,
        variables: Map<String, Variable>
    ) {
        shimejiXValue.text = shimejiX.toString()
        shimejiYValue.text = shimejiY.toString()
        activeIEValue.text = activeIE
        windowXValue.text = windowX.toString()
        windowYValue.text = windowY.toString()
        windowWidthValue.text = windowWidth.toString()
        windowHeightValue.text = windowHeight.toString()
        environmentXValue.text = environmentX.toString()
        environmentYValue.text = environmentY.toString()
        environmentWidthValue.text = environmentWidth.toString()
        environmentHeightValue.text = environmentHeight.toString()

        behaviorsList.setListData(arrayOf(behavior))
        variablesList.setListData(variables.map { "${it.key}: ${it.value}" }.toTypedArray())
    }
}
