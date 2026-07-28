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

import com.group_finity.mascot.image.TranslucentWindow
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Frame
import java.awt.GridBagConstraints
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
import javax.swing.JFileChooser
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JRadioButton
import javax.swing.JScrollPane
import javax.swing.JSlider
import javax.swing.JSpinner
import javax.swing.JTabbedPane
import javax.swing.JTextField
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.border.BevelBorder
import javax.swing.border.SoftBevelBorder
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class SettingsWindow(parent: Frame?, modal: Boolean) : JDialog(parent, modal) {
    private var alwaysShowShimejiChooser = getProperty("AlwaysShowShimejiChooser", false)
    private var alwaysShowInformationScreen = getProperty("AlwaysShowInformationScreen", false)
    private var scaling = getProperty("Scaling", 1.0)
    private var opacity = getProperty("Opacity", 1.0)
    private var filter = getProperty("Filter", "Nearest")
    private val whitelistModel = DefaultListModel<String>()
    private val blacklistModel = DefaultListModel<String>()
    private var menuScaling = getProperty("MenuScaling", 1)
    private var theme = getProperty("Theme", "FlatDark")
    private var environment = getProperty("Environment", "linux")
    private var windowSize = getProperty("WindowSize", "600x500")
    private var windowBackgroundColor = getProperty("Background", "#00FF00")
    private var windowBackgroundImage = getProperty("BackgroundImage", "")
    private var windowBackgroundMode = getProperty("BackgroundMode", "Center")

    private val darkTheme = Properties()
    private val lightTheme = Properties()
    private val initialDarkTheme = Properties()
    private val initialLightTheme = Properties()
    private val initialTheme = theme

    var isRestartRequired = false
    var isEnvironmentReloadRequired = false
    var isImageReloadRequired = false
    var isInteractiveWindowReloadRequired = false

    init {
        try {
            getPath("conf", "theme", "FlatDarkLaf.properties").inputStream().use {
                darkTheme.load(it)
                initialDarkTheme.putAll(darkTheme)
            }

            getPath("conf", "theme", "FlatLightLaf.properties").inputStream().use {
                lightTheme.load(it)
                initialLightTheme.putAll(lightTheme)
            }
        } catch (_: Exception) {
        }

        val icon = loadResource("/img/icon.png").use { ImageIO.read(it) }
        setIconImage(icon)
        title = localize("Settings")
        layout = BorderLayout()

        val tabs = object : JTabbedPane() {
            override fun getPreferredSize() = super.getPreferredSize().apply {
                width = 450
            }
        }

        val generalTab = JPanel()
        generalTab.layout = BoxLayout(generalTab, BoxLayout.Y_AXIS)

        val alwaysShowShimejiChooserCheckBox = JCheckBox(localize("AlwaysShowShimejiChooser"))
        alwaysShowShimejiChooserCheckBox.isSelected = alwaysShowShimejiChooser
        alwaysShowShimejiChooserCheckBox.addChangeListener {
            alwaysShowShimejiChooser = alwaysShowShimejiChooserCheckBox.isSelected
        }

        val alwaysShowInformationScreenCheckBox = JCheckBox(localize("AlwaysShowInformationScreen"))
        alwaysShowInformationScreenCheckBox.isSelected = alwaysShowInformationScreen
        alwaysShowInformationScreenCheckBox.addChangeListener {
            alwaysShowInformationScreen = alwaysShowInformationScreenCheckBox.isSelected
        }

        val scalingSlider = JSlider()
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

            scaling = scalingSlider.value / 10.0
        }

        val opacitySlider = JSlider()
        opacitySlider.alignmentX = LEFT_ALIGNMENT
        opacitySlider.majorTickSpacing = 10
        opacitySlider.minorTickSpacing = 5
        opacitySlider.paintLabels = true
        opacitySlider.paintTicks = true
        opacitySlider.snapToTicks = true
        opacitySlider.value = (opacity * 100.0).toInt()
        opacitySlider.addChangeListener {
            opacity = opacitySlider.value / 100.0
        }

        val filterButtonGroup = ButtonGroup()

        val nearestNeighborRadioButton = JRadioButton(localize("NearestNeighbor"))
        nearestNeighborRadioButton.isSelected = filter == "Nearest"
        nearestNeighborRadioButton.addChangeListener {
            if (nearestNeighborRadioButton.isSelected) {
                filter = "Nearest"
            }
        }

        val bicubicRadioButton = JRadioButton(localize("BicubicFilter"))
        bicubicRadioButton.isSelected = filter == "Bicubic"
        bicubicRadioButton.addChangeListener {
            if (bicubicRadioButton.isSelected) {
                filter = "Bicubic"
            }
        }

        val hqxRadioButton = JRadioButton(localize("HqxFilter"))
        hqxRadioButton.isSelected = filter == "Hqx"
        hqxRadioButton.addChangeListener {
            if (hqxRadioButton.isSelected) {
                filter = "Hqx"
            }
        }

        filterButtonGroup.add(nearestNeighborRadioButton)
        filterButtonGroup.add(bicubicRadioButton)
        filterButtonGroup.add(hqxRadioButton)

        generalTab.add(alwaysShowShimejiChooserCheckBox)
        generalTab.add(alwaysShowInformationScreenCheckBox)
        generalTab.add(Box.createVerticalStrut(10))
        generalTab.add(JLabel(localize("Scaling")))
        generalTab.add(scalingSlider)
        generalTab.add(Box.createVerticalStrut(10))
        generalTab.add(JLabel(localize("Opacity")))
        generalTab.add(opacitySlider)
        generalTab.add(Box.createVerticalStrut(10))
        generalTab.add(JLabel(localize("Filter")))
        generalTab.add(nearestNeighborRadioButton)
        generalTab.add(bicubicRadioButton)
        generalTab.add(hqxRadioButton)

        val interactiveWindowsTab = JPanel(BorderLayout())

        val interactiveWindowsTabs = JTabbedPane()

        val whitelist = JList(whitelistModel)
        for (caption in getProperty("InteractiveWindows", "").split("/").filter { it.isNotBlank() }) {
            whitelistModel.add(whitelistModel.size, caption)
        }

        val blacklist = JList(blacklistModel)
        for (caption in getProperty("InteractiveWindowsBlacklist", "").split("/").filter { it.isNotBlank() }) {
            blacklistModel.add(blacklistModel.size, caption)
        }

        interactiveWindowsTabs.addTab(localize("Whitelist"), JScrollPane(whitelist))
        interactiveWindowsTabs.addTab(localize("Blacklist"), JScrollPane(blacklist))

        val interactiveWindowsButtonsPanel = JPanel(FlowLayout())

        val addInteractiveWindowButton = JButton(localize("Add"))
        addInteractiveWindowButton.preferredSize = Dimension(130, 26)
        addInteractiveWindowButton.addActionListener {
            val input = JOptionPane.showInputDialog(
                rootPane,
                localize("InteractiveWindowHintMessage"),
                if (interactiveWindowsTabs.selectedIndex == 0) {
                    localize("AddInteractiveWindow")
                } else {
                    localize("BlacklistInteractiveWindow")
                },
                JOptionPane.QUESTION_MESSAGE
            )

            if (!input.isNullOrBlank() && !input.contains("/")) {
                if (interactiveWindowsTabs.selectedIndex == 0) {
                    whitelistModel.add(whitelistModel.size, input.trim())
                } else {
                    blacklistModel.add(blacklistModel.size, input.trim())
                }
            }
        }

        val removeInteractiveWindowButton = JButton(localize("Remove"))
        removeInteractiveWindowButton.preferredSize = Dimension(130, 26)
        removeInteractiveWindowButton.addActionListener {
            if (interactiveWindowsTabs.selectedIndex == 0) {
                if (whitelist.selectedIndex != -1) {
                    whitelistModel.remove(whitelist.selectedIndex)
                }
            } else {
                if (blacklist.selectedIndex != -1) {
                    blacklistModel.remove(blacklist.selectedIndex)
                }
            }
        }

        interactiveWindowsButtonsPanel.add(addInteractiveWindowButton)
        interactiveWindowsButtonsPanel.add(removeInteractiveWindowButton)

        interactiveWindowsTab.add(interactiveWindowsTabs, BorderLayout.CENTER)
        interactiveWindowsTab.add(interactiveWindowsButtonsPanel, BorderLayout.SOUTH)

        val menuTab = JPanel(BorderLayout())

        val menuScalingPanel = JPanel()
        menuScalingPanel.layout = BoxLayout(menuScalingPanel, BoxLayout.Y_AXIS)
        menuScalingPanel.border = BorderFactory.createTitledBorder(localize("MenuScaling"))

        val menuScalingSlider = JSlider()
        menuScalingSlider.minimum = 1
        menuScalingSlider.maximum = 3
        menuScalingSlider.majorTickSpacing = 1
        menuScalingSlider.paintLabels = true
        menuScalingSlider.paintTicks = true
        menuScalingSlider.snapToTicks = true
        menuScalingSlider.value = menuScaling
        menuScalingSlider.labelTable = Hashtable(
            mapOf(
                1 to JLabel("1x"),
                2 to JLabel("2x"),
                3 to JLabel("3x")
            )
        )
        menuScalingSlider.addChangeListener {
            menuScaling = menuScalingSlider.value
        }

        menuScalingPanel.add(menuScalingSlider)

        val themePanel = JPanel()
        themePanel.layout = BoxLayout(themePanel, BoxLayout.Y_AXIS)
        themePanel.border = BorderFactory.createTitledBorder(localize("Theme"))

        val themeCards = CardLayout()
        val themeCardsPanel = JPanel(themeCards)

        val themes = listOf(
            Pair(0, "FlatDark"),
            Pair(1, "FlatLight"),
            Pair(2, "GTK")
        )

        val themeComboBox = JComboBox<String>()
        themeComboBox.addItem(localize("FlatDark"))
        themeComboBox.addItem(localize("FlatLight"))
        themeComboBox.addItem(localize("Gtk"))
        themeComboBox.selectedIndex = themes.find { it.second == theme }?.first ?: 0
        themeComboBox.addActionListener {
            theme = themes.find { it.first == themeComboBox.selectedIndex }?.second ?: "FlatDark"
            refreshTheme()

            if (theme.startsWith("Flat")) {
                themeCards.show(themeCardsPanel, "Flat")
            } else {
                themeCards.show(themeCardsPanel, "Gtk")
            }
        }

        val flatThemeCard = JPanel(BorderLayout())

        val flatThemeColorsPanel = JPanel()
        flatThemeColorsPanel.layout = BoxLayout(flatThemeColorsPanel, BoxLayout.Y_AXIS)

        val flatThemeBackgroundColorPanel = JPanel(BorderLayout())

        val flatThemeBackgroundColorRightPanel = JPanel()
        flatThemeBackgroundColorRightPanel.layout = BoxLayout(flatThemeBackgroundColorRightPanel, BoxLayout.X_AXIS)

        val flatThemeBackgroundColorTextField = JTextField(if (themeComboBox.selectedIndex == 0) {
            darkTheme.getProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
        } else {
            lightTheme.getProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
        })
        flatThemeBackgroundColorTextField.addActionListener {
            val color = runCatching {
                Color.decode(flatThemeBackgroundColorTextField.text)
            }.getOrNull()

            if (color != null) {
                if (themeComboBox.selectedIndex == 0) {
                    darkTheme.setProperty("@background", getHex(color))
                } else {
                    lightTheme.setProperty("@background", getHex(color))
                }
                refreshTheme()
            }
        }

        val flatThemeBackgroundColorButton = JButton(localize("Change"))
        flatThemeBackgroundColorButton.addActionListener {
            val selectedTheme = if (themeComboBox.selectedIndex == 0) darkTheme else lightTheme
            val defaultColor = if (themeComboBox.selectedIndex == 0) DEFAULT_DARK_BACKGROUND_COLOR else DEFAULT_LIGHT_BACKGROUND_COLOR

            val color = JColorChooser.showDialog(
                this,
                localize("ChooseBackgroundColor"),
                Color.decode(selectedTheme.getProperty("@background", defaultColor)),
                false
            )

            if (color != null) {
                val hex = getHex(color)
                selectedTheme.setProperty("@background", hex)
                flatThemeBackgroundColorTextField.text = hex
                refreshTheme()
            }
        }

        val flatThemeBackgroundColorPreview = object : JPanel() {
            init {
                border = SoftBevelBorder(BevelBorder.LOWERED)
            }

            override fun getPreferredSize() = Dimension(
                flatThemeBackgroundColorButton.preferredSize.height,
                flatThemeBackgroundColorButton.preferredSize.height,
            )

            override fun getBackground() = Color.decode(
                if (themeComboBox.selectedIndex == 0) {
                    darkTheme.getProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
                } else {
                    lightTheme.getProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
                }
            )
        }

        flatThemeBackgroundColorRightPanel.add(flatThemeBackgroundColorTextField)
        flatThemeBackgroundColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeBackgroundColorRightPanel.add(flatThemeBackgroundColorPreview)
        flatThemeBackgroundColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeBackgroundColorRightPanel.add(flatThemeBackgroundColorButton)

        flatThemeBackgroundColorPanel.add(JLabel(localize("BackgroundColor")), BorderLayout.WEST)
        flatThemeBackgroundColorPanel.add(flatThemeBackgroundColorRightPanel, BorderLayout.EAST)

        val flatThemeTextColorPanel = JPanel(BorderLayout())

        val flatThemeTextColorRightPanel = JPanel()
        flatThemeTextColorRightPanel.layout = BoxLayout(flatThemeTextColorRightPanel, BoxLayout.X_AXIS)

        val flatThemeTextColorTextField = JTextField(if (themeComboBox.selectedIndex == 0) {
            darkTheme.getProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
        } else {
            lightTheme.getProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
        })
        flatThemeTextColorTextField.addActionListener {
            val color = runCatching {
                Color.decode(flatThemeTextColorTextField.text)
            }.getOrNull()

            if (color != null) {
                if (themeComboBox.selectedIndex == 0) {
                    darkTheme.setProperty("@foreground", getHex(color))
                } else {
                    lightTheme.setProperty("@foreground", getHex(color))
                }
                refreshTheme()
            }
        }

        val flatThemeTextColorButton = JButton(localize("Change"))
        flatThemeTextColorButton.addActionListener {
            val selectedTheme = if (themeComboBox.selectedIndex == 0) darkTheme else lightTheme
            val defaultColor = if (themeComboBox.selectedIndex == 0) DEFAULT_DARK_TEXT_COLOR else DEFAULT_LIGHT_TEXT_COLOR

            val color = JColorChooser.showDialog(
                this,
                localize("ChooseTextColor"),
                Color.decode(selectedTheme.getProperty("@foreground", defaultColor)),
                false
            )

            if (color != null) {
                val hex = getHex(color)
                selectedTheme.setProperty("@foreground", hex)
                flatThemeTextColorTextField.text = hex
                refreshTheme()
            }
        }

        val flatThemeTextColorPreview = object : JPanel() {
            init {
                border = SoftBevelBorder(BevelBorder.LOWERED)
            }

            override fun getPreferredSize() = Dimension(
                flatThemeTextColorButton.preferredSize.height,
                flatThemeTextColorButton.preferredSize.height,
            )

            override fun getBackground() = Color.decode(
                if (themeComboBox.selectedIndex == 0) {
                    darkTheme.getProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
                } else {
                    lightTheme.getProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
                }
            )
        }

        flatThemeTextColorRightPanel.add(flatThemeTextColorTextField)
        flatThemeTextColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeTextColorRightPanel.add(flatThemeTextColorPreview)
        flatThemeTextColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeTextColorRightPanel.add(flatThemeTextColorButton)

        flatThemeTextColorPanel.add(JLabel(localize("TextColor")), BorderLayout.WEST)
        flatThemeTextColorPanel.add(flatThemeTextColorRightPanel, BorderLayout.EAST)

        val flatThemeAccentColorPanel = JPanel(BorderLayout())

        val flatThemeAccentColorRightPanel = JPanel()
        flatThemeAccentColorRightPanel.layout = BoxLayout(flatThemeAccentColorRightPanel, BoxLayout.X_AXIS)

        val flatThemeAccentColorTextField = JTextField(if (themeComboBox.selectedIndex == 0) {
            darkTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
        } else {
            lightTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
        })
        flatThemeAccentColorTextField.addActionListener {
            val color = runCatching {
                Color.decode(flatThemeAccentColorTextField.text)
            }.getOrNull()

            if (color != null) {
                if (themeComboBox.selectedIndex == 0) {
                    darkTheme.setProperty("@accentColor", getHex(color))
                } else {
                    lightTheme.setProperty("@accentColor", getHex(color))
                }
                refreshTheme()
            }
        }

        val flatThemeAccentColorButton = JButton(localize("Change"))
        flatThemeAccentColorButton.addActionListener {
            val selectedTheme = if (themeComboBox.selectedIndex == 0) darkTheme else lightTheme

            val color = JColorChooser.showDialog(
                this,
                localize("ChooseAccentColor"),
                Color.decode(selectedTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)),
                false
            )

            if (color != null) {
                val hex = getHex(color)
                selectedTheme.setProperty("@accentColor", hex)
                flatThemeAccentColorTextField.text = hex
                refreshTheme()
            }
        }

        val flatThemeAccentColorPreview = object : JPanel() {
            init {
                border = SoftBevelBorder(BevelBorder.LOWERED)
            }

            override fun getPreferredSize() = Dimension(
                flatThemeTextColorButton.preferredSize.height,
                flatThemeTextColorButton.preferredSize.height,
            )

            override fun getBackground() = Color.decode(
                if (themeComboBox.selectedIndex == 0) {
                    darkTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                } else {
                    lightTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                }
            )
        }

        flatThemeAccentColorRightPanel.add(flatThemeAccentColorTextField)
        flatThemeAccentColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeAccentColorRightPanel.add(flatThemeAccentColorPreview)
        flatThemeAccentColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeAccentColorRightPanel.add(flatThemeAccentColorButton)

        flatThemeAccentColorPanel.add(JLabel(localize("AccentColor")), BorderLayout.WEST)
        flatThemeAccentColorPanel.add(flatThemeAccentColorRightPanel, BorderLayout.EAST)

        flatThemeColorsPanel.add(Box.createVerticalStrut(3))
        flatThemeColorsPanel.add(flatThemeBackgroundColorPanel)
        flatThemeColorsPanel.add(Box.createVerticalStrut(3))
        flatThemeColorsPanel.add(flatThemeTextColorPanel)
        flatThemeColorsPanel.add(Box.createVerticalStrut(3))
        flatThemeColorsPanel.add(flatThemeAccentColorPanel)

        val flatThemeButtonsPanel = JPanel(FlowLayout())

        val resetFlatThemeButton = JButton(localize("Reset"))
        resetFlatThemeButton.addActionListener {
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

        flatThemeButtonsPanel.add(resetFlatThemeButton)

        flatThemeCard.add(flatThemeColorsPanel, BorderLayout.NORTH)
        flatThemeCard.add(flatThemeButtonsPanel, BorderLayout.SOUTH)

        val gtkThemeCard = JPanel(GridBagLayout())
        gtkThemeCard.add(JLabel(localize("GtkThemeMessage")))

        themeCardsPanel.add(flatThemeCard, "Flat")
        themeCardsPanel.add(gtkThemeCard, "Gtk")

        if (theme.startsWith("Flat")) {
            themeCards.show(themeCardsPanel, "Flat")
        } else {
            themeCards.show(themeCardsPanel, "Gtk")
        }

        themePanel.add(themeComboBox)
        themePanel.add(themeCardsPanel)

        menuTab.add(menuScalingPanel, BorderLayout.NORTH)
        menuTab.add(themePanel, BorderLayout.CENTER)

        val windowModeTab = JPanel(BorderLayout())

        val windowModePanel = JPanel()
        windowModePanel.layout = BoxLayout(windowModePanel, BoxLayout.Y_AXIS)

        val windowModeSettingsPanel = JPanel()
        windowModeSettingsPanel.layout = BoxLayout(windowModeSettingsPanel, BoxLayout.Y_AXIS)
        windowModeSettingsPanel.isVisible = environment == "virtual"

        val windowModeEnabledCheckBox = JCheckBox(localize("WindowedModeEnabled"))
        windowModeEnabledCheckBox.isSelected = environment == "virtual"
        windowModeEnabledCheckBox.addActionListener {
            environment = if (windowModeEnabledCheckBox.isSelected) "virtual" else "linux"
            windowModeSettingsPanel.isVisible = windowModeEnabledCheckBox.isSelected
        }

        val windowDimensionsPanel = JPanel(BorderLayout())
        windowDimensionsPanel.alignmentX = LEFT_ALIGNMENT

        val windowDimensionsRightPanel = JPanel()
        windowDimensionsRightPanel.layout = BoxLayout(windowDimensionsRightPanel, BoxLayout.X_AXIS)

        val (windowWidth, windowHeight) = windowSize.split("x").map { it.toIntOrNull() ?: 0 }

        val widthSpinner = JSpinner()
        widthSpinner.value = windowWidth
        widthSpinner.addChangeListener {
            val (_, windowHeight) = windowSize.split("x")
            windowSize = "${widthSpinner.value}x${windowHeight}"
        }

        val heightSpinner = JSpinner()
        heightSpinner.value = windowHeight
        heightSpinner.addChangeListener {
            val (windowWidth, _) = windowSize.split("x")
            windowSize = "${windowWidth}x${heightSpinner.value}"
        }

        windowDimensionsRightPanel.add(widthSpinner)
        windowDimensionsRightPanel.add(Box.createHorizontalStrut(3))
        windowDimensionsRightPanel.add(JLabel("x"))
        windowDimensionsRightPanel.add(Box.createHorizontalStrut(3))
        windowDimensionsRightPanel.add(heightSpinner)
        windowDimensionsRightPanel.add(Box.createHorizontalStrut(6))

        windowDimensionsPanel.add(JLabel(localize("Dimensions")), BorderLayout.WEST)
        windowDimensionsPanel.add(windowDimensionsRightPanel, BorderLayout.EAST)

        val windowBackgroundPanel = JPanel()
        windowBackgroundPanel.layout = BoxLayout(windowBackgroundPanel, BoxLayout.Y_AXIS)
        windowBackgroundPanel.alignmentX = LEFT_ALIGNMENT

        val windowBackgroundColorPanel = JPanel(BorderLayout())
        windowBackgroundPanel.border = BorderFactory.createEmptyBorder(6, 12, 6, 6)

        val windowBackgroundColorRightPanel = JPanel()
        windowBackgroundColorRightPanel.layout = BoxLayout(windowBackgroundColorRightPanel, BoxLayout.X_AXIS)

        val windowBackgroundColorTextField = JTextField(windowBackgroundColor)
        windowBackgroundColorTextField.addActionListener {
            val color = runCatching {
                Color.decode(windowBackgroundColorTextField.text)
            }.getOrNull()

            if (color != null) {
                windowBackgroundColor = getHex(color)
                repaint()
            }
        }

        val windowBackgroundColorButton = JButton(localize("Change"))
        windowBackgroundColorButton.addActionListener {
            val color = JColorChooser.showDialog(
                this,
                localize("ChooseBackgroundColor"),
                Color.decode(windowBackgroundColor),
                false
            )

            if (color != null) {
                val hex = getHex(color)
                windowBackgroundColor = hex
                windowBackgroundColorTextField.text = hex
                repaint()
            }
        }

        val windowBackgroundColorPreview = object : JPanel() {
            init {
                border = SoftBevelBorder(BevelBorder.LOWERED)
            }

            override fun getPreferredSize() = Dimension(
                windowBackgroundColorButton.preferredSize.height,
                windowBackgroundColorButton.preferredSize.height
            )

            override fun getBackground() = Color.decode(windowBackgroundColor)
        }

        windowBackgroundColorRightPanel.add(windowBackgroundColorTextField)
        windowBackgroundColorRightPanel.add(Box.createHorizontalStrut(3))
        windowBackgroundColorRightPanel.add(windowBackgroundColorPreview)
        windowBackgroundColorRightPanel.add(Box.createHorizontalStrut(3))
        windowBackgroundColorRightPanel.add(windowBackgroundColorButton)

        windowBackgroundColorPanel.add(JLabel(localize("Color")), BorderLayout.WEST)
        windowBackgroundColorPanel.add(windowBackgroundColorRightPanel, BorderLayout.EAST)

        val windowBackgroundImagePanel = JPanel(BorderLayout())

        val windowBackgroundImageRightPanel = JPanel(GridBagLayout())
        windowBackgroundImageRightPanel.alignmentX = RIGHT_ALIGNMENT
        val constraints = GridBagConstraints()

        val windowBackgroundImagePreviewPanel = JPanel(BorderLayout())
        windowBackgroundImagePreviewPanel.preferredSize = Dimension(96, 96)

        val windowBackgroundImagePreview = JLabel()
        windowBackgroundImagePreview.border = SoftBevelBorder(BevelBorder.LOWERED)

        refreshBackgroundImagePreview(windowBackgroundImagePreview)

        windowBackgroundImagePreviewPanel.add(windowBackgroundImagePreview, BorderLayout.CENTER)

        val changeWindowBackgroundImageButton = JButton(localize("Change"))
        changeWindowBackgroundImageButton.addActionListener {
            val dialog = JFileChooser()
            dialog.dialogTitle = localize("ChooseBackgroundImage")

            if (dialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                windowBackgroundImage = dialog.selectedFile.canonicalPath
                refreshBackgroundImagePreview(windowBackgroundImagePreview)
            }
        }

        val windowBackgroundModes = listOf(
            Pair(0, "Center"),
            Pair(1, "Fit"),
            Pair(2, "Stretch"),
            Pair(3, "Fill")
        )

        val windowBackgroundModeComboBox = JComboBox<String>()
        windowBackgroundModeComboBox.addItem(localize("BackgroundModeCenter"))
        windowBackgroundModeComboBox.addItem(localize("BackgroundModeFit"))
        windowBackgroundModeComboBox.addItem(localize("BackgroundModeStretch"))
        windowBackgroundModeComboBox.addItem(localize("BackgroundModeFill"))
        windowBackgroundModeComboBox.selectedIndex = windowBackgroundModes.find { it.second == windowBackgroundMode }?.first ?: 0
        windowBackgroundModeComboBox.addItemListener {
            windowBackgroundMode = windowBackgroundModes.find { it.first == windowBackgroundModeComboBox.selectedIndex }?.second ?: "Center"
            refreshBackgroundImagePreview(windowBackgroundImagePreview)
        }

        val removeWindowBackgroundImageButton = JButton(localize("Remove"))
        removeWindowBackgroundImageButton.addActionListener {
            windowBackgroundImage = ""
            refreshBackgroundImagePreview(windowBackgroundImagePreview)
        }

        constraints.gridx = 0
        constraints.gridy = 0
        constraints.gridheight = 3
        constraints.fill = GridBagConstraints.NONE
        constraints.insets = Insets(6, 3, 0, 0)
        windowBackgroundImageRightPanel.add(windowBackgroundImagePreviewPanel, constraints)

        constraints.gridx = 1
        constraints.gridheight = 1
        constraints.weighty = 1.0
        constraints.fill = GridBagConstraints.HORIZONTAL
        windowBackgroundImageRightPanel.add(changeWindowBackgroundImageButton, constraints)

        constraints.gridy = 1
        windowBackgroundImageRightPanel.add(windowBackgroundModeComboBox, constraints)

        constraints.gridy = 2
        windowBackgroundImageRightPanel.add(removeWindowBackgroundImageButton, constraints)

        windowBackgroundColorButton.preferredSize = removeWindowBackgroundImageButton.preferredSize

        val imageLabel = JLabel(localize("Image"))
        imageLabel.verticalAlignment = SwingConstants.TOP

        windowBackgroundImagePanel.add(imageLabel, BorderLayout.WEST)
        windowBackgroundImagePanel.add(windowBackgroundImageRightPanel, BorderLayout.EAST)

        windowBackgroundPanel.add(windowBackgroundColorPanel)
        windowBackgroundPanel.add(windowBackgroundImagePanel)

        windowModeSettingsPanel.add(windowDimensionsPanel)
        windowModeSettingsPanel.add(JLabel(localize("Background")))
        windowModeSettingsPanel.add(windowBackgroundPanel)

        windowModePanel.add(windowModeEnabledCheckBox)
        windowModePanel.add(windowModeSettingsPanel)

        windowModeTab.add(windowModePanel, BorderLayout.NORTH)

        val aboutTab = JPanel(GridBagLayout())
        aboutTab.layout = BoxLayout(aboutTab, BoxLayout.Y_AXIS)

        val aboutIcon = JLabel()
        aboutIcon.icon = ImageIcon(icon.getScaledInstance(96, 96, Image.SCALE_DEFAULT))
        aboutIcon.alignmentX = CENTER_ALIGNMENT

        val titleLabel = JLabel("ShimeLinux")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, titleLabel.font.size + 10.0f)
        titleLabel.alignmentX = CENTER_ALIGNMENT

        val versionLabel = JLabel(VERSION)
        versionLabel.alignmentX = CENTER_ALIGNMENT

        aboutTab.add(Box.createVerticalGlue())
        aboutTab.add(aboutIcon)
        aboutTab.add(titleLabel)
        aboutTab.add(versionLabel)
        aboutTab.add(Box.createVerticalGlue())

        tabs.addTab(localize("General"), generalTab)
        if (System.getenv("XDG_CURRENT_DESKTOP") == "KDE") {
            tabs.addTab(localize("InteractiveWindows"), interactiveWindowsTab)
        }
        tabs.addTab(localize("Menu"), menuTab)
        tabs.addTab(localize("WindowMode"), windowModeTab)
        tabs.addTab(localize("About"), aboutTab)

        val buttonsPanel = JPanel(FlowLayout())

        val doneButton = JButton(localize("Done"))
        doneButton.addActionListener {
            applyChanges()
            dispose()
        }

        val cancelButton = JButton(localize("Cancel"))
        cancelButton.addActionListener {
            cancelChanges()
            dispose()
        }

        buttonsPanel.add(doneButton)
        buttonsPanel.add(cancelButton)

        add(tabs, BorderLayout.CENTER)
        add(buttonsPanel, BorderLayout.SOUTH)
        pack()
        setLocationRelativeTo(null)
    }

    private fun applyChanges() {
        if (getProperty("AlwaysShowShimejiChooser", false) != alwaysShowShimejiChooser) {
            Main.properties.setProperty("AlwaysShowShimejiChooser", alwaysShowShimejiChooser.toString())
        }

        if (getProperty("AlwaysShowInformationScreen", false) != alwaysShowInformationScreen) {
            Main.properties.setProperty("AlwaysShowInformationScreen", alwaysShowInformationScreen.toString())
        }

        if (getProperty("Scaling", 1.0) != scaling) {
            Main.properties.setProperty("Scaling", scaling.toString())
            isImageReloadRequired = true
        }

        if (getProperty("Opacity", 1.0) != opacity) {
            Main.properties.setProperty("Opacity", opacity.toString())
            isImageReloadRequired = true
        }

        if (getProperty("Filter", "Nearest") != filter) {
            Main.properties.setProperty("Filter", filter)
            isImageReloadRequired = true
        }

        val whitelist = whitelistModel.elements().toList().toString()
            .replace("[", "")
            .replace("]", "")
            .replace(", ", "/")

        val blacklist = blacklistModel.elements().toList().toString()
            .replace("[", "")
            .replace("]", "")
            .replace(", ", "/")

        if (getProperty("InteractiveWindows", "") != whitelist) {
            Main.properties.setProperty("InteractiveWindows", whitelist)
            isInteractiveWindowReloadRequired = true
        }

        if (getProperty("InteractiveWindowsBlacklist", "") != blacklist) {
            Main.properties.setProperty("InteractiveWindowsBlacklist", blacklist)
            isInteractiveWindowReloadRequired = true
        }

        if (getProperty("MenuScaling", 1) != menuScaling) {
            Main.properties.setProperty("MenuScaling", menuScaling.toString())
            isRestartRequired = true
        }

        if (getProperty("Theme", "FlatDark") != theme) {
            Main.properties.setProperty("Theme", theme)
        }

        if (getProperty("Environment", "linux") != environment) {
            Main.properties.setProperty("Environment", environment)
            isEnvironmentReloadRequired = true
        }

        if (getProperty("WindowSize", "600x500") != windowSize) {
            Main.properties.setProperty("WindowSize", windowSize)
            isEnvironmentReloadRequired = true
        }

        if (getProperty("Background", "#00FF00") != windowBackgroundColor) {
            Main.properties.setProperty("Background", windowBackgroundColor)
            isEnvironmentReloadRequired = true
        }

        if (getProperty("BackgroundImage", "") != windowBackgroundImage) {
            Main.properties.setProperty("BackgroundImage", windowBackgroundImage)
            isEnvironmentReloadRequired = true
        }

        if (getProperty("BackgroundMode", "Center") != windowBackgroundMode) {
            Main.properties.setProperty("BackgroundMode", windowBackgroundMode)
            isEnvironmentReloadRequired = true
        }

        getPath("conf", "settings.properties").outputStream().use {
            Main.properties.store(it, "Configuration Options")
        }
    }

    private fun cancelChanges() {
        theme = initialTheme
        darkTheme.putAll(initialDarkTheme)
        lightTheme.putAll(initialLightTheme)
        refreshTheme()
    }

    private fun refreshTheme() {
        getPath("conf", "theme", "FlatDarkLaf.properties").outputStream().use {
            darkTheme.store(it, "Flat Dark Theme")
        }
        getPath("conf", "theme", "FlatLightLaf.properties").outputStream().use {
            lightTheme.store(it, "Flat Light Theme")
        }

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

    private fun refreshBackgroundImagePreview(preview: JLabel) {
        var image = ImageIcon(windowBackgroundImage).image
        val size = Dimension(96, 96)

        if (windowBackgroundMode == "Stretch") {
            image = image.getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH)
        } else if (windowBackgroundMode != "Center") {
            val factor = when (windowBackgroundMode) {
                "Fit" -> (size.width / image.getWidth(null).toDouble()).coerceAtMost(size.height / image.getHeight(null).toDouble())
                else -> (size.width / image.getWidth(null).toDouble()).coerceAtLeast(size.height / image.getHeight(null).toDouble())
            }

            image = image.getScaledInstance(
                (factor * image.getWidth(null)).toInt(),
                (factor * image.getHeight(null)).toInt(),
                Image.SCALE_SMOOTH
            )
        }

        preview.icon = ImageIcon(image)
        preview.preferredSize = Dimension(image.getWidth(null), image.getHeight(null))
    }

    companion object {
        private const val VERSION = "1.1.3"

        private const val DEFAULT_DARK_BACKGROUND_COLOR = "#202020"
        private const val DEFAULT_DARK_TEXT_COLOR = "#ffffff"
        private const val DEFAULT_LIGHT_BACKGROUND_COLOR = "#ffffff"
        private const val DEFAULT_LIGHT_TEXT_COLOR = "#000000"
        private const val DEFAULT_ACCENT_COLOR = "#3c83c5"

        private fun getHex(color: Color) = String.format("#%06X", color.rgb and 0xFFFFFF)
    }
}
