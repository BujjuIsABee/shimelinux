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

import com.formdev.flatlaf.FlatClientProperties
import com.formdev.flatlaf.ui.FlatLineBorder
import com.group_finity.mascot.image.TranslucentWindow
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Frame
import java.awt.GridBagLayout
import java.awt.Image
import java.awt.Insets
import java.util.Hashtable
import java.util.Properties
import javax.imageio.ImageIO
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ButtonGroup
import javax.swing.DefaultListModel
import javax.swing.ImageIcon
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JColorChooser
import javax.swing.JComboBox
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.JSlider
import javax.swing.JSpinner
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.text.replace

class SettingsWindow(parent: Frame?, modal: Boolean) : JDialog(parent, modal) {
    private val mainTabs: JTabbedPane
    private val generalTab: JPanel
    private val alwaysShowShimejiChooserCheckBox: JCheckBox
    private val alwaysShowInformationScreenCheckBox: JCheckBox
    private val scalingSlider: JSlider
    private val opacitySlider: JSlider
    private val nearestNeighborRadioButton: JRadioButton
    private val bicubicRadioButton: JRadioButton
    private val hqxRadioButton: JRadioButton
    private val filterButtonGroup: ButtonGroup
    private val interactiveWindowsTab: JPanel
    private val interactiveWindowsTabs: JTabbedPane
    private val whitelistModel: DefaultListModel<String>
    private val whitelist: JList<String>
    private val blacklistModel: DefaultListModel<String>
    private val blacklist: JList<String>
    private val interactiveWindowsButtonsPanel: JPanel
    private val addInteractiveWindowButton: JButton
    private val removeInteractiveWindowButton: JButton
    private val menuTab: JPanel
    private val menuScalingPanel: JPanel
    private val menuScalingSlider: JSlider
    private val themeComboBox: JComboBox<String>
    private val flatThemePanel: JPanel
    private val flatThemeColorsPanel: JPanel
    private val backgroundColorPanel: JPanel
    private val backgroundColorRightPanel: JPanel
    private val backgroundColorButton: JButton
    private val textColorPanel: JPanel
    private val textColorRightPanel: JPanel
    private val textColorButton: JButton
    private val accentColorPanel: JPanel
    private val accentColorRightPanel: JPanel
    private val accentColorButton: JButton
    private val gtkThemePanel: JPanel
    private val themePanel: JPanel
    private val resetButtonPanel: JPanel
    private val resetButton: JButton
    private val windowModeTab: JPanel
    private val windowModeCheckBox: JCheckBox
    private val windowModeSettingsPanel: JPanel
    private val dimensionsAndBackgroundPanel: JPanel
    private val widthSpinner: JSpinner
    private val heightSpinner: JSpinner
    private val backgroundPanel: JPanel
    private val windowBackgroundCustomizerPanel: JPanel
    private val windowBackgroundColorChooserPanel: JPanel
    private val windowBackgroundColorChooserRightPanel: JPanel
    private val windowBackgroundColorChooserButton: JButton
    private val dimensionsPanel: JPanel
    private val windowDimensionsCustomizerPanel: JPanel
    private val windowDimensionsCustomizerRightPanel: JPanel
    private val windowDimensionsSpinnersPanel: JPanel
    private val aboutTab: JPanel
    private val infoPanel: JPanel
    private val aboutImageLabel: JLabel
    private val shimelinuxLabel: JLabel
    private val versionLabel: JLabel
    private val footerPanel: JPanel
    private val doneButton: JButton
    private val cancelButton: JButton

    private var alwaysShowShimejiChooser = getProperty("AlwaysShowShimejiChooser", false)
    private var alwaysShowInformationScreen = getProperty("AlwaysShowInformationScreen", false)
    private var scaling = getProperty("Scaling", 1.0)
    private var opacity = getProperty("Opacity", 1.0)
    private var filter = getProperty("Filter", "Nearest")
    private var menuScaling = getProperty("MenuScaling", System.getProperty("sun.java2d.uiScale")?.toIntOrNull() ?: 1)
    private var theme = getProperty("Theme", "FlatDark")
    private var environment = getProperty("Environment", "linux")
    private var windowSize = getProperty("WindowSize", "600x500")
    private var background = getProperty("Background", "#00FF00")
    private val initialTheme = theme
    private val darkTheme = Properties()
    private val lightTheme = Properties()
    private val initialDarkBackgroundColor: String
    private val initialDarkTextColor: String
    private val initialDarkAccentColor: String
    private val initialLightBackgroundColor: String
    private val initialLightTextColor: String
    private val initialLightAccentColor: String

    var isEnvironmentReloadRequired = false
    var isRestartRequired = false
    var isImageReloadRequired = false
    var isInteractiveWindowReloadRequired = false

    init {
        val icon = loadResource("/img/icon.png").use { ImageIO.read(it) }
        setIconImage(icon)
        title = "Settings".localize()
        layout = BorderLayout()

        if (NativeFactory.usingWaylandLayers) {
            isResizable = false
        }

        try {
            getPath("conf", "theme", "FlatDarkLaf.properties").inputStream().use { darkTheme.load(it) }
            getPath("conf", "theme", "FlatLightLaf.properties").inputStream().use { lightTheme.load(it) }
        } catch (_: Exception) {
        }

        // Store initial theme colors
        initialDarkBackgroundColor = darkTheme.getProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
        initialDarkTextColor = darkTheme.getProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
        initialDarkAccentColor = darkTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
        initialLightBackgroundColor = lightTheme.getProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
        initialLightTextColor = lightTheme.getProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
        initialLightAccentColor = lightTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)

        alwaysShowShimejiChooserCheckBox = JCheckBox("AlwaysShowShimejiChooser".localize())
        alwaysShowShimejiChooserCheckBox.isSelected = alwaysShowShimejiChooser
        alwaysShowShimejiChooserCheckBox.addChangeListener {
            alwaysShowShimejiChooser = alwaysShowShimejiChooserCheckBox.isSelected
        }

        alwaysShowInformationScreenCheckBox = JCheckBox("AlwaysShowInformationScreen".localize())
        alwaysShowInformationScreenCheckBox.isSelected = alwaysShowInformationScreen
        alwaysShowInformationScreenCheckBox.addChangeListener {
            alwaysShowInformationScreen = alwaysShowInformationScreenCheckBox.isSelected
        }

        scalingSlider = object : JSlider() {
            override fun getPreferredSize() = Dimension(450, super.preferredSize.height)
        }
        scalingSlider.alignmentX = LEFT_ALIGNMENT
        scalingSlider.maximum = 80
        scalingSlider.majorTickSpacing = 10
        scalingSlider.minorTickSpacing = 5
        scalingSlider.paintLabels = true
        scalingSlider.paintTicks = true
        scalingSlider.snapToTicks = true
        scalingSlider.value = (scaling * 10.0).toInt()
        scalingSlider.addChangeListener {
            if (scalingSlider.value == 0) {
                scalingSlider.value = 5
            }

            if (scalingSlider.value / 10.0 != scaling) {
                scaling = scalingSlider.value / 10.0
                isImageReloadRequired = true
            }
        }

        opacitySlider = object : JSlider() {
            override fun getPreferredSize() = Dimension(450, super.preferredSize.height)
        }
        opacitySlider.alignmentX = LEFT_ALIGNMENT
        opacitySlider.majorTickSpacing = 10
        opacitySlider.minorTickSpacing = 5
        opacitySlider.paintLabels = true
        opacitySlider.paintTicks = true
        opacitySlider.snapToTicks = true
        opacitySlider.value = (opacity * 100.0).toInt()
        opacitySlider.addChangeListener {
            if (opacitySlider.value / 100.0 != opacity) {
                opacity = opacitySlider.value / 100.0
                isImageReloadRequired = true
            }
        }

        nearestNeighborRadioButton = JRadioButton("NearestNeighbor".localize())
        nearestNeighborRadioButton.isSelected = filter == "Nearest"
        nearestNeighborRadioButton.addChangeListener {
            if (nearestNeighborRadioButton.isSelected && filter != "Nearest") {
                filter = "Nearest"
                isImageReloadRequired = true
            }
        }

        bicubicRadioButton = JRadioButton("BicubicFilter".localize())
        bicubicRadioButton.isSelected = filter == "Bicubic"
        bicubicRadioButton.addChangeListener {
            if (bicubicRadioButton.isSelected && filter != "Bicubic") {
                filter = "Bicubic"
                isImageReloadRequired = true
            }
        }

        hqxRadioButton = JRadioButton("HqxFilter".localize())
        hqxRadioButton.isSelected = filter == "Hqx"
        hqxRadioButton.addChangeListener {
            if (hqxRadioButton.isSelected && filter != "Hqx") {
                filter = "Hqx"
                isImageReloadRequired = true
            }
        }

        filterButtonGroup = ButtonGroup()
        filterButtonGroup.add(nearestNeighborRadioButton)
        filterButtonGroup.add(bicubicRadioButton)
        filterButtonGroup.add(hqxRadioButton)

        generalTab = JPanel()
        generalTab.layout = BoxLayout(generalTab, BoxLayout.Y_AXIS)
        generalTab.add(alwaysShowShimejiChooserCheckBox)
        generalTab.add(alwaysShowInformationScreenCheckBox)
        generalTab.add(Box.createVerticalStrut(10))
        generalTab.add(JLabel("Scaling".localize()))
        generalTab.add(scalingSlider)
        generalTab.add(Box.createVerticalStrut(10))
        generalTab.add(JLabel("Opacity".localize()))
        generalTab.add(opacitySlider)
        generalTab.add(Box.createVerticalStrut(10))
        generalTab.add(JLabel("Filter".localize()))
        generalTab.add(nearestNeighborRadioButton)
        generalTab.add(bicubicRadioButton)
        generalTab.add(hqxRadioButton)

        whitelistModel = DefaultListModel<String>()
        for (title in getProperty("InteractiveWindows", "").split('/')) {
            if (title.isNotBlank()) {
                whitelistModel.add(whitelistModel.size, title)
            }
        }

        blacklistModel = DefaultListModel<String>()
        for (title in getProperty("InteractiveWindowsBlacklist", "").split('/')) {
            if (title.isNotBlank()) {
                blacklistModel.add(blacklistModel.size, title)
            }
        }

        whitelist = JList(whitelistModel)
        blacklist = JList(blacklistModel)

        interactiveWindowsTabs = JTabbedPane()
        interactiveWindowsTabs.addTab("Whitelist".localize(), JScrollPane(whitelist))
        interactiveWindowsTabs.addTab("Blacklist".localize(), JScrollPane(blacklist))

        addInteractiveWindowButton = JButton("Add".localize())
        addInteractiveWindowButton.preferredSize = Dimension(130, 26)
        addInteractiveWindowButton.addActionListener { handleAddInteractiveWindowButtonAction() }

        removeInteractiveWindowButton = JButton("Remove".localize())
        removeInteractiveWindowButton.preferredSize = Dimension(130, 26)
        removeInteractiveWindowButton.addActionListener { handleRemoveInteractiveWindowButtonAction() }

        interactiveWindowsButtonsPanel = JPanel(FlowLayout())
        interactiveWindowsButtonsPanel.add(addInteractiveWindowButton)
        interactiveWindowsButtonsPanel.add(removeInteractiveWindowButton)

        interactiveWindowsTab = JPanel(BorderLayout())
        interactiveWindowsTab.add(interactiveWindowsTabs, BorderLayout.CENTER)
        interactiveWindowsTab.add(interactiveWindowsButtonsPanel, BorderLayout.SOUTH)

        menuScalingSlider = JSlider()
        menuScalingSlider.minimum = 1
        menuScalingSlider.maximum = 3
        menuScalingSlider.majorTickSpacing = 1
        menuScalingSlider.paintTicks = true
        menuScalingSlider.snapToTicks = true
        menuScalingSlider.paintLabels = true
        menuScalingSlider.labelTable = Hashtable(
            mapOf(
                1 to JLabel("1x"),
                2 to JLabel("2x"),
                3 to JLabel("3x")
            )
        )
        menuScalingSlider.value = menuScaling
        menuScalingSlider.addChangeListener {
            if (menuScalingSlider.value != menuScaling) {
                menuScaling = menuScalingSlider.value
                isRestartRequired = true
            }
        }

        menuScalingPanel = JPanel()
        menuScalingPanel.layout = BoxLayout(menuScalingPanel, BoxLayout.Y_AXIS)
        menuScalingPanel.border = BorderFactory.createTitledBorder("MenuScaling".localize())
        menuScalingPanel.add(menuScalingSlider)

        themeComboBox = JComboBox<String>()
        themeComboBox.addItem("FlatDark".localize())
        themeComboBox.addItem("FlatLight".localize())
        themeComboBox.addItem("Gtk".localize())

        backgroundColorButton = JButton("Change".localize())
        backgroundColorButton.addActionListener { handleChangeBackgroundColorButtonAction() }

        backgroundColorRightPanel = JPanel()
        backgroundColorRightPanel.layout = BoxLayout(backgroundColorRightPanel, BoxLayout.X_AXIS)
        backgroundColorRightPanel.add(
            getColorPreview(
                backgroundColorButton,
                "@background",
                DEFAULT_DARK_BACKGROUND_COLOR,
                DEFAULT_LIGHT_BACKGROUND_COLOR
            )
        )
        backgroundColorRightPanel.add(Box.createHorizontalStrut(3))
        backgroundColorRightPanel.add(backgroundColorButton)

        backgroundColorPanel = JPanel(BorderLayout())
        backgroundColorPanel.add(JLabel("BackgroundColor".localize()), BorderLayout.WEST)
        backgroundColorPanel.add(backgroundColorRightPanel, BorderLayout.EAST)

        textColorButton = JButton("Change".localize())
        textColorButton.addActionListener { handleChangeTextColorButtonAction() }

        textColorRightPanel = JPanel()
        textColorRightPanel.layout = BoxLayout(textColorRightPanel, BoxLayout.X_AXIS)
        textColorRightPanel.add(
            getColorPreview(
                textColorButton,
                "@foreground",
                DEFAULT_DARK_TEXT_COLOR,
                DEFAULT_LIGHT_TEXT_COLOR
            )
        )
        textColorRightPanel.add(Box.createHorizontalStrut(3))
        textColorRightPanel.add(textColorButton)

        textColorPanel = JPanel(BorderLayout())
        textColorPanel.add(JLabel("TextColor".localize()), BorderLayout.WEST)
        textColorPanel.add(textColorRightPanel, BorderLayout.EAST)

        accentColorButton = JButton("Change".localize())
        accentColorButton.addActionListener { handleChangeAccentColorButtonAction() }

        accentColorRightPanel = JPanel()
        accentColorRightPanel.layout = BoxLayout(accentColorRightPanel, BoxLayout.X_AXIS)
        accentColorRightPanel.add(
            getColorPreview(
                accentColorButton,
                "@accentColor",
                DEFAULT_ACCENT_COLOR,
                DEFAULT_ACCENT_COLOR
            )
        )
        accentColorRightPanel.add(Box.createHorizontalStrut(3))
        accentColorRightPanel.add(accentColorButton)

        accentColorPanel = JPanel(BorderLayout())
        accentColorPanel.add(JLabel("AccentColor".localize()), BorderLayout.WEST)
        accentColorPanel.add(accentColorRightPanel, BorderLayout.EAST)

        flatThemeColorsPanel = JPanel()
        flatThemeColorsPanel.layout = BoxLayout(flatThemeColorsPanel, BoxLayout.Y_AXIS)
        flatThemeColorsPanel.add(Box.createVerticalStrut(6))
        flatThemeColorsPanel.add(backgroundColorPanel)
        flatThemeColorsPanel.add(Box.createVerticalStrut(6))
        flatThemeColorsPanel.add(textColorPanel)
        flatThemeColorsPanel.add(Box.createVerticalStrut(6))
        flatThemeColorsPanel.add(accentColorPanel)

        resetButton = JButton("Reset".localize())
        resetButton.addActionListener { handleResetButtonAction() }

        resetButtonPanel = JPanel()
        resetButtonPanel.alignmentX = CENTER_ALIGNMENT
        resetButtonPanel.add(resetButton)

        flatThemePanel = JPanel(BorderLayout())
        flatThemePanel.add(flatThemeColorsPanel, BorderLayout.NORTH)
        flatThemePanel.add(resetButtonPanel, BorderLayout.SOUTH)

        gtkThemePanel = JPanel(GridBagLayout())
        gtkThemePanel.add(JLabel("GtkThemeMessage".localize()))

        val themeMap = mapOf(0 to "FlatDark", 1 to "FlatLight", 2 to "GTK")
        val indexMap = themeMap.entries.associate { it.value to it.key }

        themeComboBox.selectedIndex = indexMap[theme] ?: 0
        themeComboBox.addItemListener {
            theme = themeMap[themeComboBox.selectedIndex] ?: "FlatDark"
            refreshTheme()
            flatThemePanel.isVisible = themeComboBox.selectedIndex != 2
            gtkThemePanel.isVisible = themeComboBox.selectedIndex == 2
        }

        flatThemePanel.isVisible = themeComboBox.selectedIndex != 2
        gtkThemePanel.isVisible = themeComboBox.selectedIndex == 2

        themePanel = JPanel()
        themePanel.layout = BoxLayout(themePanel, BoxLayout.Y_AXIS)
        themePanel.border = BorderFactory.createTitledBorder("Theme".localize())
        themePanel.add(themeComboBox)
        themePanel.add(flatThemePanel)
        themePanel.add(gtkThemePanel)

        menuTab = JPanel(BorderLayout())
        menuTab.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        menuTab.add(menuScalingPanel, BorderLayout.NORTH)
        menuTab.add(themePanel, BorderLayout.CENTER)

        val windowArray = windowSize.split('x')

        widthSpinner = JSpinner()
        widthSpinner.value = windowArray[0].toInt()
        widthSpinner.addChangeListener {
            val windowArray = windowSize.split('x')
            val oldWidth = windowArray[0].toInt()

            if (widthSpinner.value != oldWidth) {
                windowSize = buildString {
                    append(widthSpinner.value)
                    append('x')
                    append(windowArray[1])
                }
                isEnvironmentReloadRequired = true
            }
        }

        heightSpinner = JSpinner()
        heightSpinner.value = windowArray[1].toInt()
        heightSpinner.addChangeListener {
            val windowArray = windowSize.split('x')
            val oldHeight = windowArray[1].toInt()

            if (heightSpinner.value != oldHeight) {
                windowSize = buildString {
                    append(windowArray[0])
                    append('x')
                    append(heightSpinner.value)
                }
                isEnvironmentReloadRequired = true
            }
        }

        windowDimensionsCustomizerRightPanel = JPanel(FlowLayout())
        windowDimensionsCustomizerRightPanel.alignmentX = LEFT_ALIGNMENT
        windowDimensionsCustomizerRightPanel.add(widthSpinner)
        windowDimensionsCustomizerRightPanel.add(JLabel("x"))
        windowDimensionsCustomizerRightPanel.add(heightSpinner)

        windowDimensionsSpinnersPanel = JPanel(BorderLayout())
        windowDimensionsSpinnersPanel.alignmentX = LEFT_ALIGNMENT
        windowDimensionsSpinnersPanel.add(JLabel("Dimensions".localize()), BorderLayout.WEST)
        windowDimensionsSpinnersPanel.add(windowDimensionsCustomizerRightPanel, BorderLayout.EAST)

        windowDimensionsCustomizerPanel = JPanel()
        windowDimensionsCustomizerPanel.alignmentX = LEFT_ALIGNMENT
        windowDimensionsCustomizerPanel.layout = BoxLayout(windowDimensionsCustomizerPanel, BoxLayout.Y_AXIS)
        windowDimensionsCustomizerPanel.add(windowDimensionsSpinnersPanel)

        dimensionsPanel = JPanel(BorderLayout())
        dimensionsPanel.alignmentX = LEFT_ALIGNMENT
        dimensionsPanel.add(windowDimensionsCustomizerPanel, BorderLayout.NORTH)

        windowBackgroundColorChooserButton = JButton("Change".localize())
        windowBackgroundColorChooserButton.addActionListener { handleChangeWindowBackgroundColorButton() }

        windowBackgroundColorChooserRightPanel = JPanel()
        windowBackgroundColorChooserRightPanel.layout = BoxLayout(windowBackgroundColorChooserRightPanel, BoxLayout.X_AXIS)
        windowBackgroundColorChooserRightPanel.add(getWindowBackgroundColorPreview(windowBackgroundColorChooserButton))
        windowBackgroundColorChooserRightPanel.add(Box.createHorizontalStrut(3))
        windowBackgroundColorChooserRightPanel.add(windowBackgroundColorChooserButton)

        windowBackgroundColorChooserPanel = JPanel(BorderLayout())
        windowBackgroundColorChooserPanel.alignmentX = LEFT_ALIGNMENT
        windowBackgroundColorChooserPanel.add(JLabel("Background".localize()), BorderLayout.WEST)
        windowBackgroundColorChooserPanel.add(windowBackgroundColorChooserRightPanel, BorderLayout.EAST)

        windowBackgroundCustomizerPanel = JPanel()
        windowBackgroundCustomizerPanel.alignmentX = LEFT_ALIGNMENT
        windowBackgroundCustomizerPanel.layout = BoxLayout(windowBackgroundCustomizerPanel, BoxLayout.Y_AXIS)
        windowBackgroundCustomizerPanel.add(windowBackgroundColorChooserPanel)

        backgroundPanel = JPanel(BorderLayout())
        backgroundPanel.alignmentX = LEFT_ALIGNMENT
        backgroundPanel.add(windowBackgroundCustomizerPanel, BorderLayout.NORTH)

        dimensionsAndBackgroundPanel = JPanel()
        dimensionsAndBackgroundPanel.layout = BoxLayout(dimensionsAndBackgroundPanel, BoxLayout.Y_AXIS)
        dimensionsAndBackgroundPanel.add(dimensionsPanel)
        dimensionsAndBackgroundPanel.add(Box.createVerticalStrut(3))
        dimensionsAndBackgroundPanel.add(backgroundPanel)

        windowModeSettingsPanel = JPanel(BorderLayout())
        windowModeSettingsPanel.alignmentX = LEFT_ALIGNMENT
        windowModeSettingsPanel.add(dimensionsAndBackgroundPanel, BorderLayout.NORTH)

        windowModeCheckBox = JCheckBox("WindowedModeEnabled".localize())
        windowModeCheckBox.isSelected = environment == "virtual"
        windowModeCheckBox.addActionListener {
            val newEnvironment = if (windowModeCheckBox.isSelected) "virtual" else "linux"
            if (environment != newEnvironment) {
                environment = newEnvironment
                isEnvironmentReloadRequired = true
            }

            windowModeSettingsPanel.isVisible = windowModeCheckBox.isSelected
        }

        windowModeSettingsPanel.isVisible = windowModeCheckBox.isSelected

        windowModeTab = JPanel()
        windowModeTab.layout = BoxLayout(windowModeTab, BoxLayout.Y_AXIS)
        windowModeTab.add(windowModeCheckBox)
        windowModeTab.add(windowModeSettingsPanel)

        aboutImageLabel = JLabel()
        aboutImageLabel.icon = ImageIcon(icon.getScaledInstance(96, 96, Image.SCALE_DEFAULT))
        aboutImageLabel.alignmentX = CENTER_ALIGNMENT

        shimelinuxLabel = JLabel("ShimeLinux")
        shimelinuxLabel.font = shimelinuxLabel.font.deriveFont(Font.BOLD, shimelinuxLabel.font.size + 10.0f)
        shimelinuxLabel.alignmentX = CENTER_ALIGNMENT

        versionLabel = JLabel(VERSION)
        versionLabel.alignmentX = CENTER_ALIGNMENT

        infoPanel = JPanel()
        infoPanel.layout = BoxLayout(infoPanel, BoxLayout.Y_AXIS)
        infoPanel.add(aboutImageLabel)
        infoPanel.add(shimelinuxLabel)
        infoPanel.add(versionLabel)

        aboutTab = JPanel(GridBagLayout())
        aboutTab.add(infoPanel)

        mainTabs = JTabbedPane()
        mainTabs.addTab("General".localize(), generalTab)
        mainTabs.addTab("InteractiveWindows".localize(), interactiveWindowsTab)
        mainTabs.addTab("Menu".localize(), menuTab)
        mainTabs.addTab("WindowMode".localize(), windowModeTab)
        mainTabs.addTab("About".localize(), aboutTab)

        // Don't show interactive windows tab unless the KDE environment is used
        if (System.getenv("XDG_CURRENT_DESKTOP") != "KDE") {
            interactiveWindowsTab.isVisible = false
        }

        doneButton = JButton("Done".localize())
        doneButton.addActionListener { handleDone() }

        cancelButton = JButton("Cancel".localize())
        cancelButton.addActionListener { handleCancel() }

        footerPanel = JPanel(FlowLayout())
        footerPanel.add(doneButton)
        footerPanel.add(cancelButton)

        add(mainTabs, BorderLayout.CENTER)
        add(footerPanel, BorderLayout.SOUTH)
        pack()
        setLocationRelativeTo(null)
    }

    private fun handleAddInteractiveWindowButtonAction() {
        val input = JOptionPane.showInputDialog(
            rootPane,
            "InteractiveWindowHintMessage".localize(),
            if (interactiveWindowsTabs.selectedIndex == 0) {
                "AddInteractiveWindow".localize()
            } else {
                "BlacklistInteractiveWindow".localize()
            },
            JOptionPane.QUESTION_MESSAGE
        )

        if (!input.isNullOrBlank() && !input.contains('/')) {
            if (interactiveWindowsTabs.selectedIndex == 0) {
                whitelistModel.add(whitelistModel.size, input.trim())
            } else {
                blacklistModel.add(blacklistModel.size, input.trim())
            }

            isInteractiveWindowReloadRequired = true
        }
    }

    private fun handleRemoveInteractiveWindowButtonAction() {
        if (interactiveWindowsTabs.selectedIndex == 0) {
            if (whitelist.selectedIndex != -1) {
                whitelistModel.remove(whitelist.selectedIndex)
                isInteractiveWindowReloadRequired = true
            }
        } else {
            if (blacklist.selectedIndex != -1) {
                blacklistModel.remove(blacklist.selectedIndex)
                isInteractiveWindowReloadRequired = true
            }
        }
    }

    private fun handleChangeBackgroundColorButtonAction() {
        val selectedTheme = if (themeComboBox.selectedIndex == 0) darkTheme else lightTheme
        val defaultColor = if (themeComboBox.selectedIndex == 0) DEFAULT_DARK_BACKGROUND_COLOR else DEFAULT_LIGHT_BACKGROUND_COLOR

        val color = JColorChooser.showDialog(
            this,
            "ChooseBackgroundColor".localize(),
            Color.decode(selectedTheme.getProperty("@background", defaultColor)),
            false
        )

        if (color != null) {
            selectedTheme.setProperty("@background", getHex(color))
            refreshTheme()
        }
    }

    private fun handleChangeTextColorButtonAction() {
        val selectedTheme = if (themeComboBox.selectedIndex == 0) darkTheme else lightTheme
        val defaultColor = if (themeComboBox.selectedIndex == 0) DEFAULT_DARK_TEXT_COLOR else DEFAULT_LIGHT_TEXT_COLOR

        val color = JColorChooser.showDialog(
            this,
            "ChooseTextColor".localize(),
            Color.decode(selectedTheme.getProperty("@foreground", defaultColor)),
            false
        )

        if (color != null) {
            val selectedTheme = if (themeComboBox.selectedIndex == 0) darkTheme else lightTheme
            selectedTheme.setProperty("@foreground", getHex(color))
            refreshTheme()
        }
    }

    private fun handleChangeAccentColorButtonAction() {
        val selectedTheme = if (themeComboBox.selectedIndex == 0) darkTheme else lightTheme
        val defaultColor = DEFAULT_ACCENT_COLOR

        val color = JColorChooser.showDialog(
            this,
            "ChooseAccentColor".localize(),
            Color.decode(selectedTheme.getProperty("@accentColor", defaultColor)),
            false
        )

        if (color != null) {
            selectedTheme.setProperty("@accentColor", getHex(color))
            refreshTheme()
        }
    }

    private fun handleResetButtonAction() {
        // Reset current theme colors
        if (themeComboBox.selectedIndex == 0) {
            darkTheme.setProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
            darkTheme.setProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
            darkTheme.setProperty("@accentColor", DEFAULT_ACCENT_COLOR)
        } else if (themeComboBox.selectedIndex == 1) {
            lightTheme.setProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
            lightTheme.setProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
            lightTheme.setProperty("@accentColor", DEFAULT_ACCENT_COLOR)
        }

        refreshTheme()
    }

    private fun handleChangeWindowBackgroundColorButton() {
        val color = JColorChooser.showDialog(
            this,
            "ChooseBackgroundColor".localize(),
            Color.decode(background),
            false
        )

        if (color != null) {
            background = getHex(color)
            isEnvironmentReloadRequired = true
        }
    }

    private fun handleDone() {
        if (isRestartRequired) {
            val response = JOptionPane.showConfirmDialog(
                this,
                "RestartRequiredMessage".localize(),
                "RestartRequired".localize(),
                JOptionPane.YES_NO_OPTION
            )

            isRestartRequired = response == JOptionPane.YES_OPTION
        }

        setProperty("AlwaysShowShimejiChooser", alwaysShowShimejiChooser.toString())
        setProperty("AlwaysShowInformationScreen", alwaysShowInformationScreen.toString())
        setProperty("Scaling", scaling.toString())
        setProperty("Opacity", opacity.toString())
        setProperty("Filter", filter)
        setProperty("MenuScaling", menuScaling.toString())
        setProperty("Theme", theme)
        setProperty("Environment", environment)
        setProperty("WindowSize", windowSize)
        setProperty("Background", background)

        val whitelist = whitelistModel.elements().toList().toString()
            .replace("[", "")
            .replace("]", "")
            .replace(", ", "/")

        val blacklist = blacklistModel.elements().toList().toString()
            .replace("[", "")
            .replace("]", "")
            .replace(", ", "/")

        setProperty("InteractiveWindows", whitelist)
        setProperty("InteractiveWindowsBlacklist", blacklist)

        getPath("conf", "settings.properties").outputStream().use {
            Main.instance.properties.store(it, "Configuration Options")
        }

        dispose()
    }

    private fun handleCancel() {
        // Reset theme
        darkTheme.setProperty("@background", initialDarkBackgroundColor)
        darkTheme.setProperty("@foreground", initialDarkTextColor)
        darkTheme.setProperty("@accentColor", initialDarkAccentColor)
        lightTheme.setProperty("@background", initialLightBackgroundColor)
        lightTheme.setProperty("@foreground", initialLightTextColor)
        lightTheme.setProperty("@accentColor", initialLightAccentColor)
        theme = initialTheme
        refreshTheme()

        isEnvironmentReloadRequired = false
        isRestartRequired = false
        isImageReloadRequired = false
        isInteractiveWindowReloadRequired = false

        dispose()
    }

    private fun getColorPreview(
        button: JButton,
        colorKey: String,
        colorDark: String,
        colorLight: String
    ) = object : JPanel() {
        init {
            isOpaque = false

            putClientProperty(FlatClientProperties.STYLE, "arc: 6")
        }

        override fun getPreferredSize() = Dimension(
            button.preferredSize.height,
            button.preferredSize.height
        )

        override fun getBackground() = Color.decode(
            if (themeComboBox.selectedIndex == 0) {
                darkTheme.getProperty(colorKey, colorDark)
            } else {
                lightTheme.getProperty(colorKey, colorLight)
            }
        )

        override fun getBorder() = FlatLineBorder(
            Insets(15, 15, 15, 15),
            UIManager.getColor("Component.borderColor"),
            1.0f,
            6
        )
    }

    private fun getWindowBackgroundColorPreview(button: JButton) = object : JPanel() {
        init {
            isOpaque = false

            putClientProperty(FlatClientProperties.STYLE, "arc: 6")
        }

        override fun getPreferredSize() = Dimension(
            button.preferredSize.height,
            button.preferredSize.height
        )

        override fun getBackground() = Color.decode(this@SettingsWindow.background)

        override fun getBorder() = FlatLineBorder(
            Insets(15, 15, 15, 15),
            UIManager.getColor("Component.borderColor"),
            1.0f,
            6
        )
    }

    private fun refreshTheme() {
        getPath("conf", "theme", "FlatDarkLaf.properties").outputStream().use { darkTheme.store(it, "Flat Dark Theme") }
        getPath("conf", "theme", "FlatLightLaf.properties").outputStream().use { lightTheme.store(it, "Flat Light Theme") }

        UIManager.setLookAndFeel(
            when (theme) {
                "FlatDark" -> "com.formdev.flatlaf.FlatDarkLaf"
                "FlatLight" -> "com.formdev.flatlaf.FlatLightLaf"
                "GTK" -> "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
                else -> "com.formdev.flatlaf.FlatDarkLaf"
            }
        )

        for (window in getWindows()) {
            // Do not update translucent windows
            if (window is TranslucentWindow) continue

            SwingUtilities.updateComponentTreeUI(window)
        }

        pack()
    }

    companion object {
        private const val VERSION = "v1.1.1"

        private const val DEFAULT_DARK_BACKGROUND_COLOR = "#202020"
        private const val DEFAULT_DARK_TEXT_COLOR = "#ffffff"
        private const val DEFAULT_LIGHT_BACKGROUND_COLOR = "#ffffff"
        private const val DEFAULT_LIGHT_TEXT_COLOR = "#000000"
        private const val DEFAULT_ACCENT_COLOR = "#3c83c5"

        private fun getHex(color: Color) = String.format("#%06X", color.rgb and 0xFFFFFF)
    }
}
