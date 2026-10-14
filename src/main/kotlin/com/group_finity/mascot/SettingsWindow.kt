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

import com.formdev.flatlaf.FlatLaf
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
import java.awt.event.HierarchyEvent
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
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

private const val VERSION = "v1.3.4"
private const val DEFAULT_DARK_BACKGROUND_COLOR = "#202020"
private const val DEFAULT_DARK_TEXT_COLOR = "#ffffff"
private const val DEFAULT_LIGHT_BACKGROUND_COLOR = "#ffffff"
private const val DEFAULT_LIGHT_TEXT_COLOR = "#000000"
private const val DEFAULT_ACCENT_COLOR = "#3c83c5"

/**
 * The settings menu.
 *
 * @author Kilkakon
 * @author Bujju
 */
class SettingsWindow(parent: Frame?, modal: Boolean) : JDialog(parent, modal) {
    private val themeComboBox: JComboBox<String>
    private val flatThemeBackgroundColorTextField: JTextField
    private val flatThemeTextColorTextField: JTextField
    private val flatThemeAccentColorTextField: JTextField
    private val windowBackgroundImagePreview: JLabel

    private val newProperties = Properties().apply { putAll(Main.properties) }
    private val oldDarkTheme = Properties().apply {
        runCatching {
            getPath("conf", "theme", "FlatDarkLaf.properties").inputStream().use { load(it) }
        }
    }
    private val newDarkTheme = Properties().apply { putAll(oldDarkTheme) }
    private val oldLightTheme = Properties().apply {
        runCatching {
            getPath("conf", "theme", "FlatLightLaf.properties").inputStream().use { load(it) }
        }
    }
    private val newLightTheme = Properties().apply { putAll(oldLightTheme) }

    var isEnvironmentReloadRequired = false
    var isImageReloadRequired = false
    var isInteractiveWindowReloadRequired = false

    init {
        val icon = loadResource("img/icon.png").use { ImageIO.read(it) }
        setIconImage(icon)
        title = localize("Settings")
        isResizable = !Platform.isTilingWindowManager
        layout = BorderLayout()

        val tabbedPane = object : JTabbedPane() {
            override fun getPreferredSize() = super.preferredSize.apply { width = 450 }
        }

        val generalTab = JPanel()
        generalTab.layout = BoxLayout(generalTab, BoxLayout.Y_AXIS)
        generalTab.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        val alwaysShowShimejiChooserCheckBox = JCheckBox(localize("AlwaysShowShimejiChooser"))
        alwaysShowShimejiChooserCheckBox.isSelected = getProperty("AlwaysShowShimejiChooser", false)
        alwaysShowShimejiChooserCheckBox.addChangeListener {
            newProperties.setProperty(
                "AlwaysShowShimejiChooser",
                alwaysShowShimejiChooserCheckBox.isSelected.toString()
            )
        }

        val alwaysShowInformationScreenCheckBox = JCheckBox(localize("AlwaysShowInformationScreen"))
        alwaysShowInformationScreenCheckBox.isSelected = getProperty("AlwaysShowInformationScreen", false)
        alwaysShowInformationScreenCheckBox.addChangeListener {
            newProperties.setProperty(
                "AlwaysShowInformationScreen",
                alwaysShowInformationScreenCheckBox.isSelected.toString()
            )
        }

        val scalingSlider = JSlider()
        scalingSlider.alignmentX = LEFT_ALIGNMENT
        scalingSlider.maximum = 80
        scalingSlider.majorTickSpacing = 10
        scalingSlider.minorTickSpacing = 5
        scalingSlider.paintLabels = true
        scalingSlider.paintTicks = true
        scalingSlider.snapToTicks = true
        scalingSlider.value = (getProperty("Scaling", 1.0) * 10.0).toInt()
        scalingSlider.addChangeListener {
            if (scalingSlider.value == 0) {
                scalingSlider.value = 5
            }

            newProperties.setProperty("Scaling", (scalingSlider.value / 10.0).toString())
        }

        val opacitySlider = JSlider()
        opacitySlider.alignmentX = LEFT_ALIGNMENT
        opacitySlider.majorTickSpacing = 10
        opacitySlider.minorTickSpacing = 5
        opacitySlider.paintLabels = true
        opacitySlider.paintTicks = true
        opacitySlider.snapToTicks = true
        opacitySlider.value = (getProperty("Opacity", 1.0) * 100.0).toInt()
        opacitySlider.addChangeListener {
            newProperties.setProperty("Opacity", (opacitySlider.value / 100.0).toString())
        }

        val filterButtonGroup = ButtonGroup()

        val filter = getProperty("Filter", "Nearest")

        val nearestNeighborRadioButton = JRadioButton(localize("NearestNeighbor"))
        nearestNeighborRadioButton.isSelected = filter == "Nearest"
        nearestNeighborRadioButton.addChangeListener {
            if (nearestNeighborRadioButton.isSelected) {
                newProperties.setProperty("Filter", "Nearest")
            }
        }

        val bicubicRadioButton = JRadioButton(localize("BicubicFilter"))
        bicubicRadioButton.isSelected = filter == "Bicubic"
        bicubicRadioButton.addChangeListener {
            if (bicubicRadioButton.isSelected) {
                newProperties.setProperty("Filter", "Bicubic")
            }
        }

        val hqxRadioButton = JRadioButton(localize("HqxFilter"))
        hqxRadioButton.isSelected = filter == "Hqx"
        hqxRadioButton.addChangeListener {
            if (hqxRadioButton.isSelected) {
                newProperties.setProperty("Filter", "Hqx")
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

        val interactiveWindowsTab = JPanel(BorderLayout())

        val interactiveWindowsTabbedPane = JTabbedPane()

        val interactiveWindowsWhitelistModel = DefaultListModel<String>()
        interactiveWindowsWhitelistModel.addAll(
            getProperty("InteractiveWindows", "").split('/').filter { it.isNotBlank() }
        )
        val interactiveWindowsWhitelist = JList(interactiveWindowsWhitelistModel)

        val interactiveWindowsBlacklistModel = DefaultListModel<String>()
        interactiveWindowsBlacklistModel.addAll(
            getProperty("InteractiveWindowsBlacklist", "").split('/').filter { it.isNotBlank() }
        )
        val interactiveWindowsBlacklist = JList(interactiveWindowsBlacklistModel)

        interactiveWindowsTabbedPane.addTab(localize("Whitelist"), JScrollPane(interactiveWindowsWhitelist))
        interactiveWindowsTabbedPane.addTab(localize("Blacklist"), JScrollPane(interactiveWindowsBlacklist))

        val interactiveWindowsFooterPanel = JPanel(FlowLayout())

        val addInteractiveWindowButton = JButton(localize("Add"))
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

            if (!input.isNullOrEmpty() && !input.contains('/')) {
                if (interactiveWindowsTabbedPane.selectedIndex == 0) {
                    interactiveWindowsWhitelistModel.addElement(input.trim())
                    newProperties.setProperty(
                        "InteractiveWindows",
                        interactiveWindowsWhitelistModel.elements().toList().joinToString("/")
                    )
                } else {
                    interactiveWindowsBlacklistModel.addElement(input.trim())
                    newProperties.setProperty(
                        "InteractiveWindowsBlacklist",
                        interactiveWindowsBlacklistModel.elements().toList().joinToString("/")
                    )
                }
            }
        }

        val removeInteractiveWindowButton = JButton(localize("Remove"))
        removeInteractiveWindowButton.addActionListener {
            if (interactiveWindowsTabbedPane.selectedIndex == 0) {
                if (interactiveWindowsWhitelist.selectedIndex != -1) {
                    interactiveWindowsWhitelistModel.remove(interactiveWindowsWhitelist.selectedIndex)
                    newProperties.setProperty(
                        "InteractiveWindows",
                        interactiveWindowsWhitelistModel.elements().toList().joinToString("/")
                    )
                }
            } else {
                if (interactiveWindowsBlacklist.selectedIndex != -1) {
                    interactiveWindowsBlacklistModel.remove(interactiveWindowsBlacklist.selectedIndex)
                    newProperties.setProperty(
                        "InteractiveWindowsBlacklist",
                        interactiveWindowsBlacklistModel.elements().toList().joinToString("/")
                    )
                }
            }
        }

        interactiveWindowsFooterPanel.add(addInteractiveWindowButton)
        interactiveWindowsFooterPanel.add(removeInteractiveWindowButton)

        interactiveWindowsTab.add(interactiveWindowsTabbedPane, BorderLayout.CENTER)
        interactiveWindowsTab.add(interactiveWindowsFooterPanel, BorderLayout.SOUTH)

        val themeTab = JPanel()
        themeTab.layout = BoxLayout(themeTab, BoxLayout.Y_AXIS)
        themeTab.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        val themeCardLayout = CardLayout()

        val themeCardsPanel = JPanel(themeCardLayout)

        themeComboBox = JComboBox()
        themeComboBox.addItem(localize("FlatDark"))
        themeComboBox.addItem(localize("FlatLight"))
        themeComboBox.addItem(localize("Gtk"))
        themeComboBox.addActionListener {
            newProperties.setProperty(
                "Theme", when (themeComboBox.selectedIndex) {
                    0 -> "FlatDark"
                    1 -> "FlatLight"
                    2 -> "Gtk"
                    else -> "FlatDark"
                }
            )
            refreshTheme()

            if (themeComboBox.selectedIndex == 2) {
                themeCardLayout.show(themeCardsPanel, "Gtk")
            } else {
                themeCardLayout.show(themeCardsPanel, "Flat")
            }
        }

        val flatThemeCard = JPanel(BorderLayout())

        val flatThemeColorsPanel = JPanel()
        flatThemeColorsPanel.layout = BoxLayout(flatThemeColorsPanel, BoxLayout.Y_AXIS)

        val flatThemeBackgroundColorPanel = JPanel(BorderLayout())

        val flatThemeBackgroundColorRightPanel = JPanel()
        flatThemeBackgroundColorRightPanel.layout = BoxLayout(flatThemeBackgroundColorRightPanel, BoxLayout.X_AXIS)

        flatThemeBackgroundColorTextField = object : JTextField() {
            override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
        }
        flatThemeBackgroundColorTextField.addActionListener {
            val color = runCatching { Color.decode(flatThemeBackgroundColorTextField.text) }.getOrNull()

            if (color != null) {
                val hex = String.format("#%06X", color.rgb and 0xFFFFFF)
                if (themeComboBox.selectedIndex == 0) {
                    newDarkTheme.setProperty("@background", hex)
                } else {
                    newLightTheme.setProperty("@background", hex)
                }
                refreshTheme()
            }
        }

        val flatThemeBackgroundColorButton = JButton(localize("Change"))
        flatThemeBackgroundColorButton.addActionListener {
            val color = JColorChooser.showDialog(
                this,
                localize("ChooseBackgroundColor"),
                Color.decode(
                    if (themeComboBox.selectedIndex == 0) {
                        newDarkTheme.getProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
                    } else {
                        newLightTheme.getProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
                    }
                ),
                false
            )

            if (color != null) {
                val hex = String.format("#%06X", color.rgb and 0xFFFFFF)
                if (themeComboBox.selectedIndex == 0) {
                    newDarkTheme.setProperty("@background", hex)
                } else {
                    newLightTheme.setProperty("@background", hex)
                }
                refreshTheme()
            }
        }

        val flatThemeBackgroundColorPreview = object : JPanel() {
            init {
                border = BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED)
            }

            override fun getPreferredSize() = Dimension(
                flatThemeBackgroundColorButton.preferredSize.height,
                flatThemeBackgroundColorButton.preferredSize.height
            )

            override fun getBackground() = Color.decode(
                if (themeComboBox.selectedIndex == 0) {
                    newDarkTheme.getProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
                } else {
                    newLightTheme.getProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
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

        flatThemeTextColorTextField = object : JTextField() {
            override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
        }
        flatThemeTextColorTextField.addActionListener {
            val color = runCatching { Color.decode(flatThemeTextColorTextField.text) }.getOrNull()

            if (color != null) {
                val hex = String.format("#%06X", color.rgb and 0xFFFFFF)
                if (themeComboBox.selectedIndex == 0) {
                    newDarkTheme.setProperty("@foreground", hex)
                } else {
                    newLightTheme.setProperty("@foreground", hex)
                }
                refreshTheme()
            }
        }

        val flatThemeTextColorButton = JButton(localize("Change"))
        flatThemeTextColorButton.addActionListener {
            val color = JColorChooser.showDialog(
                this,
                localize("ChooseTextColor"),
                Color.decode(
                    if (themeComboBox.selectedIndex == 0) {
                        newDarkTheme.getProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
                    } else {
                        newLightTheme.getProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
                    }
                ),
                false
            )

            if (color != null) {
                val hex = String.format("#%06X", color.rgb and 0xFFFFFF)
                if (themeComboBox.selectedIndex == 0) {
                    newDarkTheme.setProperty("@foreground", hex)
                } else {
                    newLightTheme.setProperty("@foreground", hex)
                }
                refreshTheme()
            }
        }

        val flatThemeTextColorPreview = object : JPanel() {
            init {
                border = BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED)
            }

            override fun getPreferredSize() = Dimension(
                flatThemeTextColorButton.preferredSize.height,
                flatThemeTextColorButton.preferredSize.height
            )

            override fun getBackground() = Color.decode(
                if (themeComboBox.selectedIndex == 0) {
                    newDarkTheme.getProperty("@foreground", "#ffffff")
                } else {
                    newLightTheme.getProperty("@foreground", "#000000")
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

        flatThemeAccentColorTextField = object : JTextField() {
            override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
        }
        flatThemeAccentColorTextField.addActionListener {
            val color = runCatching { Color.decode(flatThemeAccentColorTextField.text) }.getOrNull()

            if (color != null) {
                val hex = String.format("#%06X", color.rgb and 0xFFFFFF)
                if (themeComboBox.selectedIndex == 0) {
                    newDarkTheme.setProperty("@accentColor", hex)
                } else {
                    newLightTheme.setProperty("@accentColor", hex)
                }
                refreshTheme()
            }
        }

        val flatThemeAccentColorButton = JButton(localize("Change"))
        flatThemeAccentColorButton.addActionListener {
            val color = JColorChooser.showDialog(
                this,
                localize("ChooseAccentColor"),
                Color.decode(
                    if (themeComboBox.selectedIndex == 0) {
                        newDarkTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                    } else {
                        newLightTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                    }
                ),
                false
            )

            if (color != null) {
                val hex = String.format("#%06X", color.rgb and 0xFFFFFF)
                if (themeComboBox.selectedIndex == 0) {
                    newDarkTheme.setProperty("@accentColor", hex)
                } else {
                    newLightTheme.setProperty("@accentColor", hex)
                }
                refreshTheme()
            }
        }

        val flatThemeAccentColorPreview = object : JPanel() {
            init {
                border = BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED)
            }

            override fun getPreferredSize() = Dimension(
                flatThemeAccentColorButton.preferredSize.height,
                flatThemeAccentColorButton.preferredSize.height
            )

            override fun getBackground() = Color.decode(
                if (themeComboBox.selectedIndex == 0) {
                    newDarkTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                } else {
                    newLightTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
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

        val flatThemeFooterPanel = JPanel(FlowLayout())

        val matchGtkThemeButton = JButton(localize("MatchGtkTheme"))
        matchGtkThemeButton.addActionListener {
            val backgroundColor: Color
            val textColor: Color
            val accentColor: Color
            UIManager.getLookAndFeel().let { previous ->
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
                backgroundColor = UIManager.getColor("Panel.background")
                textColor = UIManager.getColor("Panel.foreground")
                accentColor = UIManager.getColor("textHighlight")
                UIManager.setLookAndFeel(previous)
            }

            if (themeComboBox.selectedIndex == 0) {
                newDarkTheme.setProperty("@background", String.format("#%06X", backgroundColor.rgb and 0xFFFFFF))
                newDarkTheme.setProperty("@foreground", String.format("#%06X", textColor.rgb and 0xFFFFFF))
                newDarkTheme.setProperty("@accentColor", String.format("#%06X", accentColor.rgb and 0xFFFFFF))
            } else {
                newLightTheme.setProperty("@background", String.format("#%06X", backgroundColor.rgb and 0xFFFFFF))
                newLightTheme.setProperty("@foreground", String.format("#%06X", textColor.rgb and 0xFFFFFF))
                newLightTheme.setProperty("@accentColor", String.format("#%06X", accentColor.rgb and 0xFFFFFF))
            }
            refreshTheme()
        }

        val resetFlatThemeButton = JButton(localize("Reset"))
        resetFlatThemeButton.addActionListener {
            if (themeComboBox.selectedIndex == 0) {
                newDarkTheme.setProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
                newDarkTheme.setProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
                newDarkTheme.setProperty("@accentColor", DEFAULT_ACCENT_COLOR)
            } else {
                newLightTheme.setProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
                newLightTheme.setProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
                newLightTheme.setProperty("@accentColor", DEFAULT_ACCENT_COLOR)
            }
            refreshTheme()
        }

        flatThemeFooterPanel.add(resetFlatThemeButton)
        flatThemeFooterPanel.add(matchGtkThemeButton)

        flatThemeCard.add(flatThemeColorsPanel, BorderLayout.NORTH)
        flatThemeCard.add(flatThemeFooterPanel, BorderLayout.SOUTH)

        val gtkThemeCard = JPanel()
        gtkThemeCard.add(JLabel(localize("GtkThemeMessage")))

        themeCardsPanel.add(flatThemeCard, "Flat")
        themeCardsPanel.add(gtkThemeCard, "Gtk")

        themeComboBox.selectedIndex = when (getProperty("Theme", "FlatDark")) {
            "FlatDark" -> 0
            "FlatLight" -> 1
            "Gtk" -> 2
            else -> 0
        }

        themeTab.add(themeComboBox)
        themeTab.add(themeCardsPanel)

        val environmentTab = JPanel()
        environmentTab.layout = BoxLayout(environmentTab, BoxLayout.Y_AXIS)
        environmentTab.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)

        val environmentCardLayout = CardLayout()

        val environmentCardsPanel = JPanel(environmentCardLayout)

        val kdeEnvironmentName = localize("KdeEnvironment")
        val waylandEnvironmentName = localize("WaylandEnvironment")
        val genericEnvironmentName = localize("GenericEnvironment")
        val virtualEnvironmentName = localize("VirtualEnvironment")

        val defaultEnvironmentName = when (NativeFactory.default) {
            "kde" -> kdeEnvironmentName
            "wayland" -> waylandEnvironmentName
            else -> genericEnvironmentName
        }

        val automaticEnvironmentName = localize("AutomaticEnvironment") + " ($defaultEnvironmentName)"

        val environmentComboBox = JComboBox<String>()
        environmentComboBox.addItem(automaticEnvironmentName)
        if (Platform.currentDesktop.contains("kde")) {
            environmentComboBox.addItem(kdeEnvironmentName)
        }
        if (Platform.supportsWaylandLayerSurfaces) {
            environmentComboBox.addItem(waylandEnvironmentName)
        }
        environmentComboBox.addItem(genericEnvironmentName)
        environmentComboBox.addItem(virtualEnvironmentName)
        environmentComboBox.selectedItem = when (getProperty("Environment", "")) {
            "kde" if (Platform.currentDesktop.contains("kde")) -> kdeEnvironmentName
            "wayland" if (Platform.supportsWaylandLayerSurfaces) -> waylandEnvironmentName
            "generic" -> genericEnvironmentName
            "virtual" -> virtualEnvironmentName
            else -> automaticEnvironmentName
        }
        environmentComboBox.addActionListener {
            newProperties.setProperty(
                "Environment", when (environmentComboBox.selectedItem as String) {
                    kdeEnvironmentName -> "kde"
                    waylandEnvironmentName -> "wayland"
                    genericEnvironmentName -> "generic"
                    virtualEnvironmentName -> "virtual"
                    else -> ""
                }
            )

            val environment = when (newProperties.getProperty("Environment", "")) {
                "kde" -> "kde"
                "wayland" -> "wayland"
                "generic" -> "generic"
                "virtual" -> "virtual"
                else -> NativeFactory.default
            }

            environmentCardLayout.show(environmentCardsPanel, environment)

            // Toggle the interactive windows tab
            val index = tabbedPane.indexOfComponent(interactiveWindowsTab)
            if (environment == "kde" && index == -1) {
                tabbedPane.add(interactiveWindowsTab, localize("InteractiveWindows"), 1)
            } else if (environment != "kde" && index != -1) {
                tabbedPane.removeTabAt(index)
            }
        }

        val kdeEnvironmentCard = JPanel(BorderLayout())

        val kdeEnvironmentSettingsPanel = JPanel()
        kdeEnvironmentSettingsPanel.layout = BoxLayout(kdeEnvironmentSettingsPanel, BoxLayout.Y_AXIS)

        val useWaylandLayersCheckBox = JCheckBox("Use Wayland Layer Surfaces")
        useWaylandLayersCheckBox.isSelected = getProperty("UseWaylandLayersKde", true)
        useWaylandLayersCheckBox.horizontalAlignment = SwingConstants.LEFT
        useWaylandLayersCheckBox.addActionListener {
            newProperties.setProperty("UseWaylandLayersKde", useWaylandLayersCheckBox.isSelected.toString())
        }

        val kdeScreenDimensionsPanel = ScreenDimensionsPanel()
        kdeScreenDimensionsPanel.alignmentX = LEFT_ALIGNMENT

        kdeEnvironmentSettingsPanel.add(useWaylandLayersCheckBox)
        kdeEnvironmentSettingsPanel.add(kdeScreenDimensionsPanel)

        kdeEnvironmentCard.add(kdeEnvironmentSettingsPanel, BorderLayout.NORTH)

        val waylandEnvironmentCard = JPanel(BorderLayout())

        val waylandEnvironmentSettingsPanel = JPanel()
        waylandEnvironmentSettingsPanel.layout = BoxLayout(waylandEnvironmentSettingsPanel, BoxLayout.Y_AXIS)

        val layerLabel = JLabel("Layer")
        layerLabel.horizontalAlignment = SwingConstants.LEFT

        val layerComboBox = JComboBox<String>()
        layerComboBox.alignmentX = LEFT_ALIGNMENT
        layerComboBox.addItem("Background")
        layerComboBox.addItem("Bottom")
        layerComboBox.addItem("Top")
        layerComboBox.addItem("Overlay")
        layerComboBox.selectedIndex = getProperty("MascotLayerWayland", 3)
        layerComboBox.addActionListener {
            newProperties.setProperty("MascotLayerWayland", layerComboBox.selectedIndex.toString())
        }

        val waylandScreenDimensionsPanel = ScreenDimensionsPanel()
        waylandScreenDimensionsPanel.alignmentX = LEFT_ALIGNMENT

        waylandEnvironmentSettingsPanel.add(JLabel("Layer"))
        waylandEnvironmentSettingsPanel.add(layerComboBox)
        waylandEnvironmentSettingsPanel.add(waylandScreenDimensionsPanel)

        waylandEnvironmentCard.add(waylandEnvironmentSettingsPanel, BorderLayout.NORTH)

        val genericEnvironmentCard = JPanel(BorderLayout())

        val genericEnvironmentSettingsPanel = JPanel()
        genericEnvironmentSettingsPanel.layout = BoxLayout(genericEnvironmentSettingsPanel, BoxLayout.Y_AXIS)

        genericEnvironmentSettingsPanel.add(ScreenDimensionsPanel())

        genericEnvironmentCard.add(genericEnvironmentSettingsPanel, BorderLayout.NORTH)

        val virtualEnvironmentCard = JPanel(BorderLayout())

        val virtualEnvironmentSettingsPanel = JPanel()
        virtualEnvironmentSettingsPanel.layout = BoxLayout(virtualEnvironmentSettingsPanel, BoxLayout.Y_AXIS)

        val windowDimensionsPanel = JPanel(BorderLayout())
        windowDimensionsPanel.alignmentX = LEFT_ALIGNMENT

        val windowDimensionsRightPanel = JPanel()
        windowDimensionsRightPanel.layout = BoxLayout(windowDimensionsRightPanel, BoxLayout.X_AXIS)

        val (windowWidth, windowHeight) = getProperty("WindowSize", "600x500").split('x').map { it.toIntOrNull() ?: 0 }

        val widthSpinner = JSpinner()
        widthSpinner.value = windowWidth
        widthSpinner.addChangeListener {
            val (_, windowHeight) = newProperties.getProperty("WindowSize", "600x500").split('x')
            newProperties.setProperty("WindowSize", "${widthSpinner.value}x${windowHeight}")
        }

        val heightSpinner = JSpinner()
        heightSpinner.value = windowHeight
        heightSpinner.addChangeListener {
            val (windowWidth, _) = newProperties.getProperty("WindowSize", "600x500").split('x')
            newProperties.setProperty("WindowSize", "${windowWidth}x${heightSpinner.value}")
        }

        windowDimensionsRightPanel.add(widthSpinner)
        windowDimensionsRightPanel.add(Box.createHorizontalStrut(3))
        windowDimensionsRightPanel.add(JLabel("x"))
        windowDimensionsRightPanel.add(Box.createHorizontalStrut(3))
        windowDimensionsRightPanel.add(heightSpinner)

        windowDimensionsPanel.add(JLabel(localize("Dimensions")), BorderLayout.WEST)
        windowDimensionsPanel.add(windowDimensionsRightPanel, BorderLayout.EAST)

        val windowBackgroundPanel = JPanel()
        windowBackgroundPanel.layout = BoxLayout(windowBackgroundPanel, BoxLayout.Y_AXIS)
        windowBackgroundPanel.border = BorderFactory.createEmptyBorder(6, 12, 0, 0)
        windowBackgroundPanel.alignmentX = LEFT_ALIGNMENT

        val windowBackgroundColorPanel = JPanel(BorderLayout())

        val windowBackgroundColorRightPanel = JPanel()
        windowBackgroundColorRightPanel.layout = BoxLayout(windowBackgroundColorRightPanel, BoxLayout.X_AXIS)

        val windowBackgroundColorTextField = object : JTextField(getProperty("Background", "#00FF00")) {
            override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
        }
        windowBackgroundColorTextField.addActionListener {
            val color = runCatching { Color.decode(windowBackgroundColorTextField.text) }.getOrNull()

            if (color != null) {
                newProperties.setProperty("Background", String.format("#%06X", color.rgb and 0xFFFFFF))
                repaint()
            }
        }

        val windowBackgroundColorButton = JButton(localize("Change"))
        windowBackgroundColorButton.preferredSize = removeInteractiveWindowButton.preferredSize
        windowBackgroundColorButton.addActionListener {
            val color = JColorChooser.showDialog(
                this,
                localize("ChooseBackgroundColor"),
                Color.decode(newProperties.getProperty("Background", "#00FF00")),
                false
            )

            if (color != null) {
                val hex = String.format("#%06X", color.rgb and 0xFFFFFF)
                newProperties.setProperty("Background", hex)
                windowBackgroundColorTextField.text = hex
                repaint()
            }
        }

        val windowBackgroundColorPreview = object : JPanel() {
            init {
                border = BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED)
            }

            override fun getPreferredSize() = Dimension(
                windowBackgroundColorButton.preferredSize.height,
                windowBackgroundColorButton.preferredSize.height
            )

            override fun getBackground() = Color.decode(newProperties.getProperty("Background", "#00FF00"))
        }

        windowBackgroundColorRightPanel.add(windowBackgroundColorTextField)
        windowBackgroundColorRightPanel.add(Box.createHorizontalStrut(3))
        windowBackgroundColorRightPanel.add(windowBackgroundColorPreview)
        windowBackgroundColorRightPanel.add(Box.createHorizontalStrut(3))
        windowBackgroundColorRightPanel.add(windowBackgroundColorButton)

        windowBackgroundColorPanel.add(JLabel(localize("Color")), BorderLayout.WEST)
        windowBackgroundColorPanel.add(windowBackgroundColorRightPanel, BorderLayout.EAST)

        val windowBackgroundImagePanel = JPanel(BorderLayout())

        val windowBackgroundImageLabel = JLabel(localize("Image"))
        windowBackgroundImageLabel.verticalAlignment = SwingConstants.TOP

        val windowBackgroundImageRightPanel = JPanel(GridBagLayout())
        windowBackgroundColorRightPanel.alignmentX = RIGHT_ALIGNMENT

        val windowBackgroundImagePreviewPanel = JPanel(BorderLayout())
        windowBackgroundImagePreviewPanel.preferredSize = Dimension(96, 96)

        windowBackgroundImagePreview = JLabel()
        windowBackgroundImagePreview.border = BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED)

        refreshBackgroundImagePreview()

        windowBackgroundImagePreviewPanel.add(windowBackgroundImagePreview)

        val windowBackgroundModeComboBox = JComboBox<String>()
        windowBackgroundModeComboBox.isEnabled = getProperty("BackgroundImage", "") != ""
        windowBackgroundModeComboBox.addItem(localize("BackgroundModeCenter"))
        windowBackgroundModeComboBox.addItem(localize("BackgroundModeFit"))
        windowBackgroundModeComboBox.addItem(localize("BackgroundModeStretch"))
        windowBackgroundModeComboBox.addItem(localize("BackgroundModeFill"))
        windowBackgroundModeComboBox.selectedIndex = when (getProperty("BackgroundMode", "Center")) {
            "Center" -> 0
            "Fit" -> 1
            "Stretch" -> 2
            "Fill" -> 3
            else -> 0
        }
        windowBackgroundModeComboBox.addItemListener {
            newProperties.setProperty(
                "BackgroundMode", when (windowBackgroundModeComboBox.selectedIndex) {
                    0 -> "Center"
                    1 -> "Fit"
                    2 -> "Stretch"
                    3 -> "Fill"
                    else -> "Center"
                }
            )
            refreshBackgroundImagePreview()
        }

        val removeWindowBackgroundImageButton = JButton(localize("Remove"))
        removeWindowBackgroundImageButton.isEnabled = windowBackgroundModeComboBox.isEnabled
        removeWindowBackgroundImageButton.addActionListener {
            newProperties.setProperty("BackgroundImage", "")
            refreshBackgroundImagePreview()
            windowBackgroundModeComboBox.isEnabled = false
            removeWindowBackgroundImageButton.isEnabled = false
        }

        val changeWindowBackgroundImageButton = JButton(localize("Change"))
        changeWindowBackgroundImageButton.addActionListener {
            val dialog = JFileChooser()
            dialog.dialogTitle = localize("ChooseBackgroundImage")

            if (dialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                newProperties.setProperty("BackgroundImage", dialog.selectedFile.canonicalPath)
                refreshBackgroundImagePreview()
                windowBackgroundModeComboBox.isEnabled = true
                removeWindowBackgroundImageButton.isEnabled = true
            }
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

        virtualEnvironmentSettingsPanel.add(windowDimensionsPanel)
        virtualEnvironmentSettingsPanel.add(JLabel(localize("Background")))
        virtualEnvironmentSettingsPanel.add(windowBackgroundPanel)

        virtualEnvironmentCard.add(virtualEnvironmentSettingsPanel, BorderLayout.NORTH)

        environmentCardsPanel.add(kdeEnvironmentCard, "kde")
        environmentCardsPanel.add(waylandEnvironmentCard, "wayland")
        environmentCardsPanel.add(genericEnvironmentCard, "generic")
        environmentCardsPanel.add(virtualEnvironmentCard, "virtual")

        environmentCardLayout.show(environmentCardsPanel, NativeFactory.current)

        environmentTab.add(environmentComboBox)
        environmentTab.add(Box.createVerticalStrut(3))
        environmentTab.add(environmentCardsPanel)

        val aboutTab = JPanel()
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

        tabbedPane.addTab(localize("General"), generalTab)
        if (NativeFactory.current == "kde") {
            tabbedPane.addTab(localize("InteractiveWindows"), interactiveWindowsTab)
        }
        tabbedPane.addTab(localize("Theme"), themeTab)
        tabbedPane.addTab(localize("Environment"), environmentTab)
        tabbedPane.addTab(localize("About"), aboutTab)

        val footerPanel = JPanel(FlowLayout())

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

        footerPanel.add(doneButton)
        footerPanel.add(cancelButton)

        add(tabbedPane, BorderLayout.CENTER)
        add(footerPanel, BorderLayout.SOUTH)
        pack()
        setLocationRelativeTo(null)
    }

    private fun applyChanges() {
        if (getProperty("Scaling", "1.0") != newProperties.getProperty("Scaling", "1.0") ||
            getProperty("Opacity", "1.0") != newProperties.getProperty("Opacity", "1.0") ||
            getProperty("Filter", "Nearest") != newProperties.getProperty("Filter", "Nearest")
        ) {
            isImageReloadRequired = true
        }

        if (getProperty("InteractiveWindows", "") != newProperties.getProperty("InteractiveWindows", "") ||
            getProperty("InteractiveWindowsBlacklist", "") != newProperties.getProperty("InteractiveWindowsBlacklist", "")
        ) {
            isInteractiveWindowReloadRequired = true
        }

        if (getProperty("Environment", "") != newProperties.getProperty("Environment", "") ||
            getProperty("UseWaylandLayersKde", "true") != newProperties.getProperty("UseWaylandLayersKde", "true") ||
            getProperty("MascotLayerWayland", "3") != newProperties.getProperty("MascotLayerWayland", "3") ||
            getProperty("WindowSize", "600x500") != newProperties.getProperty("WindowSize", "600x500") ||
            getProperty("Background", "#00FF00") != newProperties.getProperty("Background", "#00FF00") ||
            getProperty("BackgroundImage", "") != newProperties.getProperty("BackgroundImage", "") ||
            getProperty("BackgroundMode", "Center") != newProperties.getProperty("BackgroundMode", "Center")
        ) {
            isEnvironmentReloadRequired = true
        }

        Main.properties.putAll(newProperties)
        Main.updateConfigFile()
    }

    private fun cancelChanges() {
        newProperties.setProperty("Theme", getProperty("Theme", "FlatDark"))
        newDarkTheme.putAll(oldDarkTheme)
        newLightTheme.putAll(oldLightTheme)
        refreshTheme()
    }

    private fun refreshTheme() {
        try {
            val theme = newProperties.getProperty("Theme", "FlatDark")

            if (theme != "Gtk") {
                FlatLaf.setGlobalExtraDefaults(mapOf())

                if (themeComboBox.selectedIndex == 0) {
                    flatThemeBackgroundColorTextField.text = newDarkTheme.getProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
                    flatThemeTextColorTextField.text = newDarkTheme.getProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
                    flatThemeAccentColorTextField.text = newDarkTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                } else if (themeComboBox.selectedIndex == 1) {
                    flatThemeBackgroundColorTextField.text = newLightTheme.getProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
                    flatThemeTextColorTextField.text = newLightTheme.getProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
                    flatThemeAccentColorTextField.text = newLightTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                }

                getPath("conf", "theme", "FlatDarkLaf.properties").outputStream().use {
                    newDarkTheme.store(it, "Flat Dark Theme")
                }
                getPath("conf", "theme", "FlatLightLaf.properties").outputStream().use {
                    newLightTheme.store(it, "Flat Light Theme")
                }
            }

            UIManager.setLookAndFeel(
                when (theme) {
                    "FlatDark" -> "com.formdev.flatlaf.FlatDarkLaf"
                    "FlatLight" -> "com.formdev.flatlaf.FlatLightLaf"
                    "Gtk" -> "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
                    else -> "com.formdev.flatlaf.FlatDarkLaf"
                }
            )

            for (window in getWindows()) {
                SwingUtilities.updateComponentTreeUI(window)
            }

            pack()
        } catch (_: Exception) {
        }
    }

    private fun refreshBackgroundImagePreview() {
        var image = ImageIcon(newProperties.getProperty("BackgroundImage", "")).image
        val size = Dimension(96, 96)
        val mode = newProperties.getProperty("BackgroundMode", "Center")

        if (mode == "Stretch") {
            image = image.getScaledInstance(size.width, size.height, Image.SCALE_SMOOTH)
        } else if (mode != "Center") {
            val widthRatio = (size.width / image.getWidth(null)).toDouble()
            val heightRatio = (size.height / image.getHeight(null)).toDouble()
            val factor = if (mode == "Fit") {
                widthRatio.coerceAtMost(heightRatio)
            } else {
                widthRatio.coerceAtLeast(heightRatio)
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

    private inner class ScreenDimensionsPanel : JPanel() {
        private val overrideScreenDimensionsCheckBox: JCheckBox
        private val screenPositionPanel: JPanel
        private val screenXSpinner: JSpinner
        private val screenYSpinner: JSpinner
        private val screenSizePanel: JPanel
        private val screenWidthSpinner: JSpinner
        private val screenHeightSpinner: JSpinner

        init {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = LEFT_ALIGNMENT

            overrideScreenDimensionsCheckBox = JCheckBox(localize("OverrideScreenDimensions"))
            overrideScreenDimensionsCheckBox.horizontalAlignment = SwingConstants.LEFT
            overrideScreenDimensionsCheckBox.addActionListener {
                newProperties.setProperty("OverrideScreenDimensions", overrideScreenDimensionsCheckBox.isSelected.toString())
                screenPositionPanel.isVisible = overrideScreenDimensionsCheckBox.isSelected
                screenSizePanel.isVisible = overrideScreenDimensionsCheckBox.isSelected
            }

            screenPositionPanel = JPanel(BorderLayout())
            screenPositionPanel.alignmentX = LEFT_ALIGNMENT

            val screenPositionRightPanel = JPanel()
            screenPositionRightPanel.layout = BoxLayout(screenPositionRightPanel, BoxLayout.X_AXIS)

            screenXSpinner = object : JSpinner() {
                override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
            }
            screenXSpinner.addChangeListener {
                newProperties.setProperty("ScreenX", (screenXSpinner.value as Int).toString())
            }

            screenYSpinner = object : JSpinner() {
                override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
            }
            screenYSpinner.addChangeListener {
                newProperties.setProperty("ScreenY", (screenYSpinner.value as Int).toString())
            }

            screenPositionRightPanel.add(screenXSpinner)
            screenPositionRightPanel.add(Box.createHorizontalStrut(3))
            screenPositionRightPanel.add(JLabel("x"))
            screenPositionRightPanel.add(Box.createHorizontalStrut(3))
            screenPositionRightPanel.add(screenYSpinner)

            screenPositionPanel.add(JLabel(localize("Position")), BorderLayout.WEST)
            screenPositionPanel.add(screenPositionRightPanel, BorderLayout.EAST)

            screenSizePanel = JPanel(BorderLayout())
            screenSizePanel.alignmentX = LEFT_ALIGNMENT

            val screenSizeRightPanel = JPanel()
            screenSizeRightPanel.layout = BoxLayout(screenSizeRightPanel, BoxLayout.X_AXIS)

            screenWidthSpinner = object : JSpinner() {
                override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
            }
            screenWidthSpinner.addChangeListener {
                newProperties.setProperty("ScreenWidth", (screenWidthSpinner.value as Int).toString())
            }

            screenHeightSpinner = object : JSpinner() {
                override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
            }
            screenHeightSpinner.addChangeListener {
                newProperties.setProperty("ScreenHeight", (screenHeightSpinner.value as Int).toString())
            }

            screenSizeRightPanel.add(screenWidthSpinner)
            screenSizeRightPanel.add(Box.createHorizontalStrut(3))
            screenSizeRightPanel.add(JLabel("x"))
            screenSizeRightPanel.add(Box.createHorizontalStrut(3))
            screenSizeRightPanel.add(screenHeightSpinner)

            screenSizePanel.add(JLabel(localize("Size")), BorderLayout.WEST)
            screenSizePanel.add(screenSizeRightPanel, BorderLayout.EAST)

            add(overrideScreenDimensionsCheckBox)
            add(screenPositionPanel)
            add(Box.createVerticalStrut(3))
            add(screenSizePanel)

            addHierarchyListener { e ->
                if ((e.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong()) != 0L) {
                    overrideScreenDimensionsCheckBox.isSelected = newProperties.getProperty("OverrideScreenDimensions", "false").toBoolean()
                    screenPositionPanel.isVisible = overrideScreenDimensionsCheckBox.isSelected
                    screenXSpinner.value = newProperties.getProperty("ScreenX", NativeFactory.instance.environment.screen.left.toString()).toInt()
                    screenYSpinner.value = newProperties.getProperty("ScreenY", NativeFactory.instance.environment.screen.top.toString()).toInt()
                    screenSizePanel.isVisible = overrideScreenDimensionsCheckBox.isSelected
                    screenWidthSpinner.value = newProperties.getProperty("ScreenWidth", NativeFactory.instance.environment.screen.width.toString()).toInt()
                    screenHeightSpinner.value = newProperties.getProperty("ScreenHeight", NativeFactory.instance.environment.screen.height.toString()).toInt()
                }
            }
        }
    }
}
