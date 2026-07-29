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

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.FlatLightLaf
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
import javax.swing.UnsupportedLookAndFeelException
import javax.swing.border.BevelBorder
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class SettingsWindow(parent: Frame?, modal: Boolean) : JDialog(parent, modal) {
    private val tabbedPane: JTabbedPane
    private val generalTab: JPanel
    private val alwaysShowShimejiChooserCheckBox: JCheckBox
    private val alwaysShowInformationScreenCheckBox: JCheckBox
    private val scalingSlider: JSlider
    private val opacitySlider: JSlider
    private val filterButtonGroup: ButtonGroup
    private val nearestNeighborRadioButton: JRadioButton
    private val bicubicRadioButton: JRadioButton
    private val hqxRadioButton: JRadioButton
    private val interactiveWindowsTab: JPanel
    private val interactiveWindowsTabbedPane: JTabbedPane
    private val interactiveWindowsWhitelist: JList<String>
    private val interactiveWindowsBlacklist: JList<String>
    private val interactiveWindowsFooterPanel: JPanel
    private val addInteractiveWindowButton: JButton
    private val removeInteractiveWindowButton: JButton
    private val menuTab: JPanel
    private val menuScalingPanel: JPanel
    private val menuScalingSlider: JSlider
    private val themePanel: JPanel
    private val themeComboBox: JComboBox<String>
    private val themeCardsPanel: JPanel
    private val flatThemeCard: JPanel
    private val flatThemeColorsPanel: JPanel
    private val flatThemeBackgroundColorPanel: JPanel
    private val flatThemeBackgroundColorRightPanel: JPanel
    private val flatThemeBackgroundColorTextField: JTextField
    private val flatThemeBackgroundColorPreview: JPanel
    private val flatThemeBackgroundColorButton: JButton
    private val flatThemeTextColorPanel: JPanel
    private val flatThemeTextColorRightPanel: JPanel
    private val flatThemeTextColorTextField: JTextField
    private val flatThemeTextColorPreview: JPanel
    private val flatThemeTextColorButton: JButton
    private val flatThemeAccentColorPanel: JPanel
    private val flatThemeAccentColorRightPanel: JPanel
    private val flatThemeAccentColorTextField: JTextField
    private val flatThemeAccentColorPreview: JPanel
    private val flatThemeAccentColorButton: JButton
    private val flatThemeFooterPanel: JPanel
    private val resetFlatThemeButton: JButton
    private val gtkThemeCard: JPanel
    private val windowModeTab: JPanel
    private val windowModePanel: JPanel
    private val windowModeEnabledCheckBox: JCheckBox
    private val windowModeSettingsPanel: JPanel
    private val windowDimensionsPanel: JPanel
    private val windowDimensionsRightPanel: JPanel
    private val widthSpinner: JSpinner
    private val heightSpinner: JSpinner
    private val windowBackgroundPanel: JPanel
    private val windowBackgroundColorPanel: JPanel
    private val windowBackgroundColorRightPanel: JPanel
    private val windowBackgroundColorTextField: JTextField
    private val windowBackgroundColorPreview: JPanel
    private val windowBackgroundColorButton: JButton
    private val windowBackgroundImagePanel: JPanel
    private val windowBackgroundImageLabel: JLabel
    private val windowBackgroundImageRightPanel: JPanel
    private val windowBackgroundImagePreviewPanel: JPanel
    private val windowBackgroundImagePreview: JLabel
    private val changeWindowBackgroundImageButton: JButton
    private val windowBackgroundModeComboBox: JComboBox<String>
    private val removeWindowBackgroundImageButton: JButton
    private val aboutTab: JPanel
    private val aboutIcon: JLabel
    private val titleLabel: JLabel
    private val versionLabel: JLabel
    private val footerPanel: JPanel
    private val doneButton: JButton
    private val cancelButton: JButton

    private var alwaysShowShimejiChooser = getProperty("AlwaysShowShimejiChooser", false)
    private var alwaysShowInformationScreen = getProperty("AlwaysShowInformationScreen", false)
    private var scaling = getProperty("Scaling", 1.0)
    private var opacity = getProperty("Opacity", 1.0)
    private var filter = getProperty("Filter", "Nearest")
    private var menuScaling = getProperty("MenuScaling", 1)
    private var theme = getProperty("Theme", "FlatDark")
    private var environment = getProperty("Environment", "linux")
    private var windowSize = getProperty("WindowSize", "600x500")
    private var windowBackgroundColor = getProperty("Background", "#00FF00")
    private var windowBackgroundImage = getProperty("BackgroundImage", "")
    private var windowBackgroundMode = getProperty("BackgroundMode", "Center")
    private val initialTheme = theme
    private val initialDarkTheme = Properties()
    private val initialLightTheme = Properties()
    private val darkTheme = Properties().apply {
        try {
            getPath("conf", "theme", "FlatDarkLaf.properties").inputStream().use { load(it) }
            initialDarkTheme.putAll(this)
        } catch (_: Exception) {
        }
    }
    private val lightTheme = Properties().apply {
        try {
            getPath("conf", "theme", "FlatLightLaf.properties").inputStream().use { load(it) }
            initialLightTheme.putAll(this)
        } catch (_: Exception) {
        }
    }
    private val interactiveWindowsWhitelistModel = DefaultListModel<String>().apply {
        addAll(
            getProperty("InteractiveWindows", "").split("/").filter { it.isNotBlank() }
        )
    }
    private val interactiveWindowsBlacklistModel = DefaultListModel<String>().apply {
        addAll(
            getProperty("InteractiveWindowsBlacklist", "").split("/").filter { it.isNotBlank() }
        )
    }

    var isRestartRequired = false
    var isEnvironmentReloadRequired = false
    var isImageReloadRequired = false
    var isInteractiveWindowReloadRequired = false

    init {
        val icon = loadResource("/img/icon.png").use { ImageIO.read(it) }
        setIconImage(icon)
        title = localize("Settings")
        layout = BorderLayout()

        tabbedPane = object : JTabbedPane() {
            override fun getPreferredSize() = super.preferredSize.apply { width = 450 }
        }

        generalTab = JPanel()
        generalTab.layout = BoxLayout(generalTab, BoxLayout.Y_AXIS)
        generalTab.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        alwaysShowShimejiChooserCheckBox = JCheckBox(localize("AlwaysShowShimejiChooser"))
        alwaysShowShimejiChooserCheckBox.isSelected = alwaysShowShimejiChooser
        alwaysShowShimejiChooserCheckBox.addChangeListener {
            alwaysShowShimejiChooser = alwaysShowShimejiChooserCheckBox.isSelected
        }

        alwaysShowInformationScreenCheckBox = JCheckBox(localize("AlwaysShowInformationScreen"))
        alwaysShowInformationScreenCheckBox.isSelected = alwaysShowInformationScreen
        alwaysShowInformationScreenCheckBox.addChangeListener {
            alwaysShowInformationScreen = alwaysShowInformationScreenCheckBox.isSelected
        }

        scalingSlider = JSlider()
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

        opacitySlider = JSlider()
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

        filterButtonGroup = ButtonGroup()

        nearestNeighborRadioButton = JRadioButton(localize("NearestNeighbor"))
        nearestNeighborRadioButton.isSelected = filter == "Nearest"
        nearestNeighborRadioButton.addChangeListener {
            if (nearestNeighborRadioButton.isSelected) {
                filter = "Nearest"
            }
        }

        bicubicRadioButton = JRadioButton(localize("BicubicFilter"))
        bicubicRadioButton.isSelected = filter == "Bicubic"
        bicubicRadioButton.addChangeListener {
            if (bicubicRadioButton.isSelected) {
                filter = "Bicubic"
            }
        }

        hqxRadioButton = JRadioButton(localize("HqxFilter"))
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
        generalTab.add(JLabel(localize("Scaling")))
        generalTab.add(scalingSlider)
        generalTab.add(Box.createVerticalStrut(5))
        generalTab.add(JLabel(localize("Opacity")))
        generalTab.add(opacitySlider)
        generalTab.add(Box.createVerticalStrut(5))
        generalTab.add(JLabel(localize("Filter")))
        generalTab.add(nearestNeighborRadioButton)
        generalTab.add(bicubicRadioButton)
        generalTab.add(hqxRadioButton)

        interactiveWindowsTab = JPanel(BorderLayout())

        interactiveWindowsTabbedPane = JTabbedPane()

        interactiveWindowsWhitelist = JList(interactiveWindowsWhitelistModel)

        interactiveWindowsBlacklist = JList(interactiveWindowsBlacklistModel)

        interactiveWindowsTabbedPane.addTab(localize("Whitelist"), JScrollPane(interactiveWindowsWhitelist))
        interactiveWindowsTabbedPane.addTab(localize("Blacklist"), JScrollPane(interactiveWindowsBlacklist))

        interactiveWindowsFooterPanel = JPanel(FlowLayout())

        addInteractiveWindowButton = JButton(localize("Add"))
        addInteractiveWindowButton.addActionListener {
            val input = JOptionPane.showInputDialog(
                this,
                localize("InteractiveWindowHintMessage"),
                if (interactiveWindowsTabbedPane.selectedIndex == 0) {
                    localize("AddInteractiveWindow")
                } else {
                    localize("BlacklistInteractiveWindow")
                },
                JOptionPane.QUESTION_MESSAGE
            )

            if (!input.isNullOrEmpty() && !input.contains("/")) {
                if (interactiveWindowsTabbedPane.selectedIndex == 0) {
                    interactiveWindowsWhitelistModel.addElement(input.trim())
                } else {
                    interactiveWindowsBlacklistModel.addElement(input.trim())
                }
            }
        }

        removeInteractiveWindowButton = JButton(localize("Remove"))
        removeInteractiveWindowButton.addActionListener {
            if (interactiveWindowsTabbedPane.selectedIndex == 0) {
                if (interactiveWindowsWhitelist.selectedIndex != -1) {
                    interactiveWindowsWhitelistModel.remove(interactiveWindowsWhitelist.selectedIndex)
                }
            } else {
                if (interactiveWindowsBlacklist.selectedIndex != -1) {
                    interactiveWindowsBlacklistModel.remove(interactiveWindowsBlacklist.selectedIndex)
                }
            }
        }

        interactiveWindowsFooterPanel.add(addInteractiveWindowButton)
        interactiveWindowsFooterPanel.add(removeInteractiveWindowButton)

        interactiveWindowsTab.add(interactiveWindowsTabbedPane, BorderLayout.CENTER)
        interactiveWindowsTab.add(interactiveWindowsFooterPanel, BorderLayout.SOUTH)

        menuTab = JPanel(BorderLayout())
        menuTab.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        menuScalingPanel = JPanel()
        menuScalingPanel.layout = BoxLayout(menuScalingPanel, BoxLayout.Y_AXIS)
        menuScalingPanel.border = BorderFactory.createTitledBorder(localize("MenuScaling"))

        menuScalingSlider = JSlider()
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

        themePanel = JPanel()
        themePanel.layout = BoxLayout(themePanel, BoxLayout.Y_AXIS)
        themePanel.border = BorderFactory.createTitledBorder(localize("Theme"))

        val themeCardLayout = CardLayout()

        themeCardsPanel = JPanel(themeCardLayout)

        themeComboBox = JComboBox()
        themeComboBox.addItem(localize("FlatDark"))
        themeComboBox.addItem(localize("FlatLight"))
        themeComboBox.addItem(localize("Gtk"))
        themeComboBox.addActionListener {
            theme = getThemeFromIndex(themeComboBox.selectedIndex)
            refreshTheme()

            when (themeComboBox.selectedIndex) {
                0, 1 -> {
                    themeCardLayout.show(themeCardsPanel, "Flat")
                }

                2 -> {
                    themeCardLayout.show(themeCardsPanel, "Gtk")
                }
            }
        }

        flatThemeCard = JPanel(BorderLayout())

        flatThemeColorsPanel = JPanel()
        flatThemeColorsPanel.layout = BoxLayout(flatThemeColorsPanel, BoxLayout.Y_AXIS)

        flatThemeBackgroundColorPanel = JPanel(BorderLayout())

        flatThemeBackgroundColorRightPanel = JPanel()
        flatThemeBackgroundColorRightPanel.layout = BoxLayout(flatThemeBackgroundColorRightPanel, BoxLayout.X_AXIS)

        flatThemeBackgroundColorTextField = object : JTextField() {
            override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
        }
        flatThemeBackgroundColorTextField.addActionListener {
            val color = runCatching { Color.decode(flatThemeBackgroundColorTextField.text) }.getOrNull()

            if (color != null) {
                val hex = String.format("#%06X", color.rgb and 0xFFFFFF)
                if (themeComboBox.selectedIndex == 0) {
                    darkTheme.setProperty("@background", hex)
                } else {
                    lightTheme.setProperty("@background", hex)
                }
                refreshTheme()
            }
        }

        flatThemeBackgroundColorPreview = object : JPanel() {
            init {
                border = BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED)
            }

            override fun getPreferredSize() = Dimension(
                flatThemeBackgroundColorButton.preferredSize.height,
                flatThemeBackgroundColorButton.preferredSize.height
            )

            override fun getBackground() = Color.decode(
                if (themeComboBox.selectedIndex == 0) {
                    darkTheme.getProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
                } else {
                    lightTheme.getProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
                }
            )
        }

        flatThemeBackgroundColorButton = JButton(localize("Change"))
        flatThemeBackgroundColorButton.addActionListener {
            val color = JColorChooser.showDialog(
                this,
                localize("ChooseBackgroundColor"),
                Color.decode(
                    if (themeComboBox.selectedIndex == 0) {
                        darkTheme.getProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
                    } else {
                        lightTheme.getProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
                    }
                ),
                false
            )

            if (color != null) {
                val hex = String.format("#%06X", color.rgb and 0xFFFFFF)
                if (themeComboBox.selectedIndex == 0) {
                    darkTheme.setProperty("@background", hex)
                } else {
                    lightTheme.setProperty("@background", hex)
                }
                refreshTheme()
            }
        }

        flatThemeBackgroundColorRightPanel.add(flatThemeBackgroundColorTextField)
        flatThemeBackgroundColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeBackgroundColorRightPanel.add(flatThemeBackgroundColorPreview)
        flatThemeBackgroundColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeBackgroundColorRightPanel.add(flatThemeBackgroundColorButton)

        flatThemeBackgroundColorPanel.add(JLabel(localize("BackgroundColor")), BorderLayout.WEST)
        flatThemeBackgroundColorPanel.add(flatThemeBackgroundColorRightPanel, BorderLayout.EAST)

        flatThemeTextColorPanel = JPanel(BorderLayout())

        flatThemeTextColorRightPanel = JPanel()
        flatThemeTextColorRightPanel.layout = BoxLayout(flatThemeTextColorRightPanel, BoxLayout.X_AXIS)

        flatThemeTextColorTextField = object : JTextField() {
            override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
        }
        flatThemeTextColorTextField.addActionListener {
            val color = runCatching { Color.decode(flatThemeTextColorTextField.text) }.getOrNull()

            if (color != null) {
                val hex = String.format("#%06X", color.rgb and 0xFFFFFF)
                if (themeComboBox.selectedIndex == 0) {
                    darkTheme.setProperty("@foreground", hex)
                } else {
                    lightTheme.setProperty("@foreground", hex)
                }
                refreshTheme()
            }
        }

        flatThemeTextColorPreview = object : JPanel() {
            init {
                border = BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED)
            }

            override fun getPreferredSize() = Dimension(
                flatThemeTextColorButton.preferredSize.height,
                flatThemeTextColorButton.preferredSize.height
            )

            override fun getBackground() = Color.decode(
                if (themeComboBox.selectedIndex == 0) {
                    darkTheme.getProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
                } else {
                    lightTheme.getProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
                }
            )
        }

        flatThemeTextColorButton = JButton(localize("Change"))
        flatThemeTextColorButton.addActionListener {
            val color = JColorChooser.showDialog(
                this,
                localize("ChooseTextColor"),
                Color.decode(
                    if (themeComboBox.selectedIndex == 0) {
                        darkTheme.getProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
                    } else {
                        lightTheme.getProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
                    }
                ),
                false
            )

            if (color != null) {
                val hex = String.format("#%06X", color.rgb and 0xFFFFFF)
                if (themeComboBox.selectedIndex == 0) {
                    darkTheme.setProperty("@foreground", hex)
                } else {
                    lightTheme.setProperty("@foreground", hex)
                }
                refreshTheme()
            }
        }

        flatThemeTextColorRightPanel.add(flatThemeTextColorTextField)
        flatThemeTextColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeTextColorRightPanel.add(flatThemeTextColorPreview)
        flatThemeTextColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeTextColorRightPanel.add(flatThemeTextColorButton)

        flatThemeTextColorPanel.add(JLabel(localize("TextColor")), BorderLayout.WEST)
        flatThemeTextColorPanel.add(flatThemeTextColorRightPanel, BorderLayout.EAST)

        flatThemeAccentColorPanel = JPanel(BorderLayout())

        flatThemeAccentColorRightPanel = JPanel()
        flatThemeAccentColorRightPanel.layout = BoxLayout(flatThemeAccentColorRightPanel, BoxLayout.X_AXIS)

        flatThemeAccentColorTextField = object : JTextField() {
            override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
        }
        flatThemeAccentColorTextField.addActionListener {
            val color = runCatching { Color.decode(flatThemeAccentColorTextField.text) }.getOrNull()

            if (color != null) {
                val hex = String.format("#%06X", color.rgb and 0xFFFFFF)
                if (themeComboBox.selectedIndex == 0) {
                    darkTheme.setProperty("@accentColor", hex)
                } else {
                    lightTheme.setProperty("@accentColor", hex)
                }
                refreshTheme()
            }
        }

        flatThemeAccentColorPreview = object : JPanel() {
            init {
                border = BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED)
            }

            override fun getPreferredSize() = Dimension(
                flatThemeAccentColorButton.preferredSize.height,
                flatThemeAccentColorButton.preferredSize.height
            )

            override fun getBackground() = Color.decode(
                if (themeComboBox.selectedIndex == 0) {
                    darkTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                } else {
                    lightTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                }
            )
        }

        flatThemeAccentColorButton = JButton(localize("Change"))
        flatThemeAccentColorButton.addActionListener {
            val color = JColorChooser.showDialog(
                this,
                localize("ChooseAccentColor"),
                Color.decode(
                    if (themeComboBox.selectedIndex == 0) {
                        darkTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                    } else {
                        lightTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                    }
                ),
                false
            )

            if (color != null) {
                val hex = String.format("#%06X", color.rgb and 0xFFFFFF)
                if (themeComboBox.selectedIndex == 0) {
                    darkTheme.setProperty("@accentColor", hex)
                } else {
                    lightTheme.setProperty("@accentColor", hex)
                }
                refreshTheme()
            }
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

        flatThemeFooterPanel = JPanel(FlowLayout())

        resetFlatThemeButton = JButton(localize("Reset"))
        resetFlatThemeButton.addActionListener {
            if (themeComboBox.selectedIndex == 0) {
                darkTheme.setProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
                darkTheme.setProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
                darkTheme.setProperty("@accentColor", DEFAULT_ACCENT_COLOR)
            } else {
                lightTheme.setProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
                lightTheme.setProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
                lightTheme.setProperty("@accentColor", DEFAULT_ACCENT_COLOR)
            }
            refreshTheme()
        }

        flatThemeFooterPanel.add(resetFlatThemeButton)

        flatThemeCard.add(flatThemeColorsPanel, BorderLayout.NORTH)
        flatThemeCard.add(flatThemeFooterPanel, BorderLayout.SOUTH)

        gtkThemeCard = JPanel()
        gtkThemeCard.add(JLabel(localize("GtkThemeMessage")))

        themeCardsPanel.add(flatThemeCard, "Flat")
        themeCardsPanel.add(gtkThemeCard, "Gtk")

        themeComboBox.selectedIndex = getIndexFromTheme(theme)

        themePanel.add(themeComboBox)
        themePanel.add(themeCardsPanel)

        menuTab.add(menuScalingPanel, BorderLayout.NORTH)
        menuTab.add(themePanel, BorderLayout.CENTER)

        windowModeTab = JPanel(BorderLayout())
        windowModeTab.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        windowModePanel = JPanel()
        windowModePanel.layout = BoxLayout(windowModePanel, BoxLayout.Y_AXIS)

        windowModeEnabledCheckBox = JCheckBox(localize("WindowedModeEnabled"))
        windowModeEnabledCheckBox.isSelected = environment == "virtual"
        windowModeEnabledCheckBox.addActionListener {
            environment = if (windowModeEnabledCheckBox.isSelected) "virtual" else "linux"
            windowModeSettingsPanel.isVisible = windowModeEnabledCheckBox.isSelected
        }

        windowModeSettingsPanel = JPanel()
        windowModeSettingsPanel.layout = BoxLayout(windowModeSettingsPanel, BoxLayout.Y_AXIS)
        windowModeSettingsPanel.isVisible = environment == "virtual"

        windowDimensionsPanel = JPanel(BorderLayout())
        windowDimensionsPanel.alignmentX = LEFT_ALIGNMENT

        windowDimensionsRightPanel = JPanel()
        windowDimensionsRightPanel.layout = BoxLayout(windowDimensionsRightPanel, BoxLayout.X_AXIS)

        val (windowWidth, windowHeight) = windowSize.split("x").map { it.toIntOrNull() ?: 0 }

        widthSpinner = JSpinner()
        widthSpinner.value = windowWidth
        widthSpinner.addChangeListener {
            val (_, windowHeight) = windowSize.split("x")
            windowSize = "${widthSpinner.value}x${windowHeight}"
        }

        heightSpinner = JSpinner()
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

        windowDimensionsPanel.add(JLabel(localize("Dimensions")), BorderLayout.WEST)
        windowDimensionsPanel.add(windowDimensionsRightPanel, BorderLayout.EAST)

        windowBackgroundPanel = JPanel()
        windowBackgroundPanel.layout = BoxLayout(windowBackgroundPanel, BoxLayout.Y_AXIS)
        windowBackgroundPanel.border = BorderFactory.createEmptyBorder(6, 12, 0, 0)
        windowBackgroundPanel.alignmentX = LEFT_ALIGNMENT

        windowBackgroundColorPanel = JPanel(BorderLayout())

        windowBackgroundColorRightPanel = JPanel()
        windowBackgroundColorRightPanel.layout = BoxLayout(windowBackgroundColorRightPanel, BoxLayout.X_AXIS)

        windowBackgroundColorTextField = object : JTextField(windowBackgroundColor) {
            override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
        }
        windowBackgroundColorTextField.addActionListener {
            val color = runCatching { Color.decode(windowBackgroundColorTextField.text) }.getOrNull()

            if (color != null) {
                windowBackgroundColor = String.format("#%06X", color.rgb and 0xFFFFFF)
                repaint()
            }
        }

        windowBackgroundColorPreview = object : JPanel() {
            init {
                border = BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED)
            }

            override fun getPreferredSize() = Dimension(
                windowBackgroundColorButton.preferredSize.height,
                windowBackgroundColorButton.preferredSize.height
            )

            override fun getBackground() = Color.decode(windowBackgroundColor)
        }

        windowBackgroundColorButton = JButton(localize("Change"))
        windowBackgroundColorButton.preferredSize = removeInteractiveWindowButton.preferredSize
        windowBackgroundColorButton.addActionListener {
            val color = JColorChooser.showDialog(
                this,
                localize("ChooseBackgroundColor"),
                Color.decode(windowBackgroundColor),
                false
            )

            if (color != null) {
                val hex = String.format("#%06X", color.rgb and 0xFFFFFF)
                windowBackgroundColor = hex
                windowBackgroundColorTextField.text = hex
                repaint()
            }
        }

        windowBackgroundColorRightPanel.add(windowBackgroundColorTextField)
        windowBackgroundColorRightPanel.add(Box.createHorizontalStrut(3))
        windowBackgroundColorRightPanel.add(windowBackgroundColorPreview)
        windowBackgroundColorRightPanel.add(Box.createHorizontalStrut(3))
        windowBackgroundColorRightPanel.add(windowBackgroundColorButton)

        windowBackgroundColorPanel.add(JLabel(localize("Color")), BorderLayout.WEST)
        windowBackgroundColorPanel.add(windowBackgroundColorRightPanel, BorderLayout.EAST)

        windowBackgroundImagePanel = JPanel(BorderLayout())

        windowBackgroundImageLabel = JLabel(localize("Image"))
        windowBackgroundImageLabel.verticalAlignment = SwingConstants.TOP

        windowBackgroundImageRightPanel = JPanel(GridBagLayout())
        windowBackgroundColorRightPanel.alignmentX = RIGHT_ALIGNMENT

        windowBackgroundImagePreviewPanel = JPanel(BorderLayout())
        windowBackgroundImagePreviewPanel.preferredSize = Dimension(96, 96)

        windowBackgroundImagePreview = JLabel()
        windowBackgroundImagePreview.border = BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED)

        refreshBackgroundImagePreview()

        windowBackgroundImagePreviewPanel.add(windowBackgroundImagePreview)

        changeWindowBackgroundImageButton = JButton(localize("Change"))
        changeWindowBackgroundImageButton.addActionListener {
            val dialog = JFileChooser()
            dialog.dialogTitle = localize("ChooseBackgroundImage")

            if (dialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                windowBackgroundImage = dialog.selectedFile.canonicalPath
                refreshBackgroundImagePreview()
            }
        }

        windowBackgroundModeComboBox = JComboBox()
        windowBackgroundModeComboBox.addItem(localize("BackgroundModeCenter"))
        windowBackgroundModeComboBox.addItem(localize("BackgroundModeFit"))
        windowBackgroundModeComboBox.addItem(localize("BackgroundModeStretch"))
        windowBackgroundModeComboBox.addItem(localize("BackgroundModeFill"))
        windowBackgroundModeComboBox.selectedIndex = getIndexFromBackgroundMode(windowBackgroundMode)
        windowBackgroundModeComboBox.addItemListener {
            windowBackgroundMode = getBackgroundModeFromIndex(windowBackgroundModeComboBox.selectedIndex)
            refreshBackgroundImagePreview()
        }

        removeWindowBackgroundImageButton = JButton(localize("Remove"))
        removeWindowBackgroundImageButton.addActionListener {
            windowBackgroundImage = ""
            refreshBackgroundImagePreview()
        }

        val constraints = GridBagConstraints()

        constraints.gridheight = 3
        constraints.fill = GridBagConstraints.NONE
        windowBackgroundImageRightPanel.add(windowBackgroundImagePreviewPanel, constraints)

        constraints.gridx = 1
        constraints.gridheight = 1
        constraints.weighty = 1.0
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.insets = Insets(6, 3, 0, 0)
        windowBackgroundImageRightPanel.add(changeWindowBackgroundImageButton, constraints)

        constraints.gridy = 1
        windowBackgroundImageRightPanel.add(windowBackgroundModeComboBox, constraints)

        constraints.gridy = 2
        windowBackgroundImageRightPanel.add(removeWindowBackgroundImageButton, constraints)

        windowBackgroundImagePanel.add(windowBackgroundImageLabel, BorderLayout.WEST)
        windowBackgroundImagePanel.add(windowBackgroundImageRightPanel, BorderLayout.EAST)

        windowBackgroundPanel.add(windowBackgroundColorPanel)
        windowBackgroundPanel.add(Box.createVerticalStrut(3))
        windowBackgroundPanel.add(windowBackgroundImagePanel)

        windowModeSettingsPanel.add(windowDimensionsPanel)
        windowModeSettingsPanel.add(JLabel(localize("Background")))
        windowModeSettingsPanel.add(windowBackgroundPanel)

        windowModePanel.add(windowModeEnabledCheckBox)
        windowModePanel.add(windowModeSettingsPanel)

        windowModeTab.add(windowModePanel, BorderLayout.NORTH)

        aboutTab = JPanel()
        aboutTab.layout = BoxLayout(aboutTab, BoxLayout.Y_AXIS)

        aboutIcon = JLabel()
        aboutIcon.icon = ImageIcon(icon.getScaledInstance(96, 96, Image.SCALE_DEFAULT))
        aboutIcon.alignmentX = CENTER_ALIGNMENT

        titleLabel = JLabel("ShimeLinux")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, titleLabel.font.size + 10.0f)
        titleLabel.alignmentX = CENTER_ALIGNMENT

        versionLabel = JLabel(VERSION)
        versionLabel.alignmentX = CENTER_ALIGNMENT

        aboutTab.add(Box.createVerticalGlue())
        aboutTab.add(aboutIcon)
        aboutTab.add(titleLabel)
        aboutTab.add(versionLabel)
        aboutTab.add(Box.createVerticalGlue())

        tabbedPane.addTab(localize("General"), generalTab)
        if (System.getenv("XDG_CURRENT_DESKTOP") == "KDE") {
            tabbedPane.addTab(localize("InteractiveWindows"), interactiveWindowsTab)
        }
        tabbedPane.addTab(localize("Menu"), menuTab)
        tabbedPane.addTab(localize("WindowMode"), windowModeTab)
        tabbedPane.addTab(localize("About"), aboutTab)

        footerPanel = JPanel(FlowLayout())

        doneButton = JButton(localize("Done"))
        doneButton.addActionListener {
            applyChanges()
            dispose()
        }

        cancelButton = JButton(localize("Cancel"))
        cancelButton.addActionListener {
            cancelChanges()
            dispose()
        }

        footerPanel.add(doneButton)
        footerPanel.add(cancelButton)

        add(tabbedPane, BorderLayout.CENTER)
        add(footerPanel, BorderLayout.SOUTH)
        pack()
        setLocationRelativeTo(null)
    }

    private fun applyChanges() {
        val whitelist = interactiveWindowsWhitelistModel.elements().toList().toString()
            .replace("[", "")
            .replace("]", "")
            .replace(", ", "/")

        val blacklist = interactiveWindowsBlacklistModel.elements().toList().toString()
            .replace("[", "")
            .replace("]", "")
            .replace(", ", "/")

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
        try {
            if (theme != "Gtk") {
                FlatLaf.setGlobalExtraDefaults(mapOf())

                if (themeComboBox.selectedIndex == 0) {
                    flatThemeBackgroundColorTextField.text = darkTheme.getProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
                    flatThemeTextColorTextField.text = darkTheme.getProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
                    flatThemeAccentColorTextField.text = darkTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                } else if (themeComboBox.selectedIndex == 1) {
                    flatThemeBackgroundColorTextField.text = lightTheme.getProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
                    flatThemeTextColorTextField.text = lightTheme.getProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
                    flatThemeAccentColorTextField.text = lightTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                }

                getPath("conf", "theme", "FlatDarkLaf.properties").outputStream().use {
                    darkTheme.store(it, "Flat Dark Theme")
                }
                getPath("conf", "theme", "FlatLightLaf.properties").outputStream().use {
                    lightTheme.store(it, "Flat Light Theme")
                }
            }

            var isDark = theme == "FlatDark"

            if (theme == "Gtk") {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")

                val backgroundColor = UIManager.getColor("Panel.background")
                val textColor = UIManager.getColor("Label.foreground")
                val accentColor = UIManager.getColor("textHighlight")

                val hsb = Color.RGBtoHSB(backgroundColor.red, backgroundColor.green, backgroundColor.blue, null)
                isDark = hsb[2] < 0.5

                FlatLaf.setGlobalExtraDefaults(
                    mapOf(
                        "@background" to String.format("#%06X", backgroundColor.rgb and 0xFFFFFF),
                        "@foreground" to String.format("#%06X", textColor.rgb and 0xFFFFFF),
                        "@accentColor" to String.format("#%06X", accentColor.rgb and 0xFFFFFF)
                    )
                )
            }

            FlatLaf.setup(
                if (isDark) {
                    FlatDarkLaf()
                } else {
                    FlatLightLaf()
                }
            )

            for (window in getWindows().filter { it !is TranslucentWindow }) {
                SwingUtilities.updateComponentTreeUI(window)
            }

            pack()
        } catch (_: UnsupportedLookAndFeelException) {
        }
    }

    private fun refreshBackgroundImagePreview() {
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

        windowBackgroundImagePreview.icon = ImageIcon(image)
        windowBackgroundImagePreview.preferredSize = Dimension(image.getWidth(null), image.getHeight(null))
    }

    companion object {
        private const val VERSION = "1.1.3"

        private const val DEFAULT_DARK_BACKGROUND_COLOR = "#202020"
        private const val DEFAULT_DARK_TEXT_COLOR = "#ffffff"
        private const val DEFAULT_LIGHT_BACKGROUND_COLOR = "#ffffff"
        private const val DEFAULT_LIGHT_TEXT_COLOR = "#000000"
        private const val DEFAULT_ACCENT_COLOR = "#3c83c5"

        private fun getIndexFromTheme(theme: String) = when (theme) {
            "FlatDark" -> 0
            "FlatLight" -> 1
            "Gtk" -> 2
            else -> 0
        }

        private fun getThemeFromIndex(index: Int) = when (index) {
            0 -> "FlatDark"
            1 -> "FlatLight"
            2 -> "Gtk"
            else -> "FlatDark"
        }

        private fun getIndexFromBackgroundMode(mode: String) = when (mode) {
            "Center" -> 0
            "Fit" -> 1
            "Stretch" -> 2
            "Fill" -> 3
            else -> 0
        }

        private fun getBackgroundModeFromIndex(index: Int) = when (index) {
            0 -> "Center"
            1 -> "Fit"
            2 -> "Stretch"
            3 -> "Fill"
            else -> "Center"
        }
    }
}
