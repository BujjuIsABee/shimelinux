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
import io.github.bujjuisabee.shimelinux.BuildConfig
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
    private val interactiveWindowsButtonsPanel: JPanel
    private val addInteractiveWindowButton: JButton
    private val removeInteractiveWindowButton: JButton
    private val themeTab: JPanel
    private val themeCardLayout: CardLayout
    private val themeCardsPanel: JPanel
    private val themeComboBox: JComboBox<String>
    private val flatThemeCard: JPanel
    private val flatThemeColorsPanel: JPanel
    private val flatThemeBackgroundColorPanel: JPanel
    private val flatThemeBackgroundColorRightPanel: JPanel
    private val flatThemeBackgroundColorTextField: JTextField
    private val flatThemeBackgroundColorButton: JButton
    private val flatThemeBackgroundColorPreview: JPanel
    private val flatThemeTextColorPanel: JPanel
    private val flatThemeTextColorRightPanel: JPanel
    private val flatThemeTextColorTextField: JTextField
    private val flatThemeTextColorButton: JButton
    private val flatThemeTextColorPreview: JPanel
    private val flatThemeAccentColorPanel: JPanel
    private val flatThemeAccentColorRightPanel: JPanel
    private val flatThemeAccentColorTextField: JTextField
    private val flatThemeAccentColorButton: JButton
    private val flatThemeAccentColorPreview: JPanel
    private val flatThemeButtonsPanel: JPanel
    private val matchGtkThemeButton: JButton
    private val resetFlatThemeButton: JButton
    private val gtkThemeCard: JPanel
    private val environmentTab: JPanel
    private val environmentPanel: JPanel
    private val environmentComboBox: JComboBox<String>
    private val environmentHelpButton: JButton
    private val environmentCardLayout: CardLayout
    private val environmentCardsPanel: JPanel
    private val kdeEnvironmentCard: JPanel
    private val kdeEnvironmentSettingsPanel: JPanel
    private val useWaylandLayersCheckBoxKde: JCheckBox
    private val layerPanelKde: JPanel
    private val layerComboBoxKde: JComboBox<String>
    private val screenDimensionsPanelKde: ScreenDimensionsPanel
    private val waylandEnvironmentCard: JPanel
    private val waylandEnvironmentSettingsPanel: JPanel
    private val layerPanelWayland: JPanel
    private val layerComboBoxWayland: JComboBox<String>
    private val screenDimensionsPanelWayland: ScreenDimensionsPanel
    private val genericEnvironmentCard: JPanel
    private val genericEnvironmentSettingsPanel: JPanel
    private val virtualEnvironmentCard: JPanel
    private val virtualEnvironmentSettingsPanel: JPanel
    private val windowDimensionsPanel: JPanel
    private val windowDimensionsRightPanel: JPanel
    private val widthSpinner: JSpinner
    private val heightSpinner: JSpinner
    private val windowBackgroundPanel: JPanel
    private val windowBackgroundColorPanel: JPanel
    private val windowBackgroundColorRightPanel: JPanel
    private val windowBackgroundColorTextField: JTextField
    private val windowBackgroundColorButton: JButton
    private val windowBackgroundColorPreview: JPanel
    private val windowBackgroundImagePanel: JPanel
    private val windowBackgroundImageLabel: JLabel
    private val windowBackgroundImageRightPanel: JPanel
    private val windowBackgroundImagePreviewPanel: JPanel
    private val windowBackgroundImagePreview: JLabel
    private val windowBackgroundModeComboBox: JComboBox<String>
    private val removeWindowBackgroundImageButton: JButton
    private val changeWindowBackgroundImageButton: JButton
    private val aboutTab: JPanel
    private val aboutIcon: JLabel
    private val titleLabel: JLabel
    private val versionLabel: JLabel
    private val buttonsPanel: JPanel
    private val doneButton: JButton
    private val cancelButton: JButton

    private val oldDarkTheme = Properties()
    private val oldLightTheme = Properties()
    private val newDarkTheme = Properties()
    private val newLightTheme = Properties()
    private val newProperties = Properties()

    var isEnvironmentReloadRequired = false
    var isImageReloadRequired = false
    var isInteractiveWindowReloadRequired = false

    init {
        val icon = loadResource("img/icon.png").use { ImageIO.read(it) }
        setIconImage(icon)
        title = localize("Settings")
        isResizable = !Platform.isTilingWindowManager
        layout = BorderLayout()

        try {
            getPath("conf", "theme", "FlatDarkLaf.properties").inputStream().use { oldDarkTheme.load(it) }
            getPath("conf", "theme", "FlatLightLaf.properties").inputStream().use { oldLightTheme.load(it) }
        } catch (_: Exception) {
        }

        newDarkTheme.putAll(oldDarkTheme)
        newLightTheme.putAll(oldLightTheme)
        newProperties.putAll(Main.properties)

        alwaysShowShimejiChooserCheckBox = JCheckBox(localize("AlwaysShowShimejiChooser"))
        alwaysShowShimejiChooserCheckBox.isSelected = getProperty("AlwaysShowShimejiChooser", false)
        alwaysShowShimejiChooserCheckBox.addChangeListener {
            newProperties.setProperty(
                "AlwaysShowShimejiChooser",
                alwaysShowShimejiChooserCheckBox.isSelected.toString()
            )
        }

        alwaysShowInformationScreenCheckBox = JCheckBox(localize("AlwaysShowInformationScreen"))
        alwaysShowInformationScreenCheckBox.isSelected = getProperty("AlwaysShowInformationScreen", false)
        alwaysShowInformationScreenCheckBox.addChangeListener {
            newProperties.setProperty(
                "AlwaysShowInformationScreen",
                alwaysShowInformationScreenCheckBox.isSelected.toString()
            )
        }

        scalingSlider = JSlider()
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

        opacitySlider = JSlider()
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

        val filter = getProperty("Filter", "Nearest")

        nearestNeighborRadioButton = JRadioButton(localize("NearestNeighbor"))
        nearestNeighborRadioButton.isSelected = filter == "Nearest"
        nearestNeighborRadioButton.addChangeListener {
            if (nearestNeighborRadioButton.isSelected) {
                newProperties.setProperty("Filter", "Nearest")
            }
        }

        bicubicRadioButton = JRadioButton(localize("BicubicFilter"))
        bicubicRadioButton.isSelected = filter == "Bicubic"
        bicubicRadioButton.addChangeListener {
            if (bicubicRadioButton.isSelected) {
                newProperties.setProperty("Filter", "Bicubic")
            }
        }

        hqxRadioButton = JRadioButton(localize("HqxFilter"))
        hqxRadioButton.isSelected = filter == "Hqx"
        hqxRadioButton.addChangeListener {
            if (hqxRadioButton.isSelected) {
                newProperties.setProperty("Filter", "Hqx")
            }
        }

        filterButtonGroup = ButtonGroup()
        filterButtonGroup.add(nearestNeighborRadioButton)
        filterButtonGroup.add(bicubicRadioButton)
        filterButtonGroup.add(hqxRadioButton)

        generalTab = JPanel()
        generalTab.layout = BoxLayout(generalTab, BoxLayout.Y_AXIS)
        generalTab.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
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

        val interactiveWindowsWhitelistModel = DefaultListModel<String>().also { model ->
            model.addAll(getProperty("InteractiveWindows", "").split('/').filter { it.isNotBlank() })
        }
        interactiveWindowsWhitelist = JList(interactiveWindowsWhitelistModel)

        val interactiveWindowsBlacklistModel = DefaultListModel<String>().also { model ->
            model.addAll(getProperty("InteractiveWindowsBlacklist", "").split('/').filter { it.isNotBlank() })
        }
        interactiveWindowsBlacklist = JList(interactiveWindowsBlacklistModel)

        interactiveWindowsTabbedPane = JTabbedPane()
        interactiveWindowsTabbedPane.addTab(localize("Whitelist"), JScrollPane(interactiveWindowsWhitelist))
        interactiveWindowsTabbedPane.addTab(localize("Blacklist"), JScrollPane(interactiveWindowsBlacklist))

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

        removeInteractiveWindowButton = JButton(localize("Remove"))
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

        interactiveWindowsButtonsPanel = JPanel(FlowLayout())
        interactiveWindowsButtonsPanel.add(addInteractiveWindowButton)
        interactiveWindowsButtonsPanel.add(removeInteractiveWindowButton)

        interactiveWindowsTab = JPanel(BorderLayout())
        interactiveWindowsTab.add(interactiveWindowsTabbedPane, BorderLayout.CENTER)
        interactiveWindowsTab.add(interactiveWindowsButtonsPanel, BorderLayout.SOUTH)

        themeComboBox = JComboBox()
        themeComboBox.addItem("Dark")
        themeComboBox.addItem("Light")
        themeComboBox.addItem("GTK")
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

        flatThemeBackgroundColorButton = JButton(localize("Change"))
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
                    newDarkTheme.getProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
                } else {
                    newLightTheme.getProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
                }
            )
        }

        flatThemeBackgroundColorRightPanel = JPanel()
        flatThemeBackgroundColorRightPanel.layout = BoxLayout(flatThemeBackgroundColorRightPanel, BoxLayout.X_AXIS)
        flatThemeBackgroundColorRightPanel.add(flatThemeBackgroundColorTextField)
        flatThemeBackgroundColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeBackgroundColorRightPanel.add(flatThemeBackgroundColorPreview)
        flatThemeBackgroundColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeBackgroundColorRightPanel.add(flatThemeBackgroundColorButton)

        flatThemeBackgroundColorPanel = JPanel(BorderLayout())
        flatThemeBackgroundColorPanel.add(JLabel(localize("BackgroundColor")), BorderLayout.WEST)
        flatThemeBackgroundColorPanel.add(flatThemeBackgroundColorRightPanel, BorderLayout.EAST)

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

        flatThemeTextColorButton = JButton(localize("Change"))
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
                    newDarkTheme.getProperty("@foreground", "#ffffff")
                } else {
                    newLightTheme.getProperty("@foreground", "#000000")
                }
            )
        }

        flatThemeTextColorRightPanel = JPanel()
        flatThemeTextColorRightPanel.layout = BoxLayout(flatThemeTextColorRightPanel, BoxLayout.X_AXIS)
        flatThemeTextColorRightPanel.add(flatThemeTextColorTextField)
        flatThemeTextColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeTextColorRightPanel.add(flatThemeTextColorPreview)
        flatThemeTextColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeTextColorRightPanel.add(flatThemeTextColorButton)

        flatThemeTextColorPanel = JPanel(BorderLayout())
        flatThemeTextColorPanel.add(JLabel(localize("TextColor")), BorderLayout.WEST)
        flatThemeTextColorPanel.add(flatThemeTextColorRightPanel, BorderLayout.EAST)

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

        flatThemeAccentColorButton = JButton(localize("Change"))
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
                    newDarkTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                } else {
                    newLightTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                }
            )
        }

        flatThemeAccentColorRightPanel = JPanel()
        flatThemeAccentColorRightPanel.layout = BoxLayout(flatThemeAccentColorRightPanel, BoxLayout.X_AXIS)
        flatThemeAccentColorRightPanel.add(flatThemeAccentColorTextField)
        flatThemeAccentColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeAccentColorRightPanel.add(flatThemeAccentColorPreview)
        flatThemeAccentColorRightPanel.add(Box.createHorizontalStrut(3))
        flatThemeAccentColorRightPanel.add(flatThemeAccentColorButton)

        flatThemeAccentColorPanel = JPanel(BorderLayout())
        flatThemeAccentColorPanel.add(JLabel(localize("AccentColor")), BorderLayout.WEST)
        flatThemeAccentColorPanel.add(flatThemeAccentColorRightPanel, BorderLayout.EAST)

        flatThemeColorsPanel = JPanel()
        flatThemeColorsPanel.layout = BoxLayout(flatThemeColorsPanel, BoxLayout.Y_AXIS)
        flatThemeColorsPanel.add(Box.createVerticalStrut(3))
        flatThemeColorsPanel.add(flatThemeBackgroundColorPanel)
        flatThemeColorsPanel.add(Box.createVerticalStrut(3))
        flatThemeColorsPanel.add(flatThemeTextColorPanel)
        flatThemeColorsPanel.add(Box.createVerticalStrut(3))
        flatThemeColorsPanel.add(flatThemeAccentColorPanel)

        matchGtkThemeButton = JButton(localize("MatchGtkTheme"))
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

        resetFlatThemeButton = JButton(localize("Reset"))
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

        flatThemeButtonsPanel = JPanel(FlowLayout())
        flatThemeButtonsPanel.add(resetFlatThemeButton)
        flatThemeButtonsPanel.add(matchGtkThemeButton)

        flatThemeCard = JPanel(BorderLayout())
        flatThemeCard.add(flatThemeColorsPanel, BorderLayout.NORTH)
        flatThemeCard.add(flatThemeButtonsPanel, BorderLayout.SOUTH)

        gtkThemeCard = JPanel(GridBagLayout())
        gtkThemeCard.add(JLabel("This theme cannot be customized."))

        themeCardLayout = CardLayout()
        themeCardsPanel = JPanel(themeCardLayout)
        themeCardsPanel.add(flatThemeCard, "Flat")
        themeCardsPanel.add(gtkThemeCard, "Gtk")

        themeComboBox.selectedIndex = when (getProperty("Theme", "FlatDark")) {
            "FlatDark" -> 0
            "FlatLight" -> 1
            "Gtk" -> 2
            else -> 0
        }

        themeTab = JPanel()
        themeTab.layout = BoxLayout(themeTab, BoxLayout.Y_AXIS)
        themeTab.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        themeTab.add(themeComboBox)
        themeTab.add(themeCardsPanel)

        val defaultEnvironmentName = when (NativeFactory.default) {
            "kde" -> "KDE"
            "wayland" -> "Wayland"
            else -> "Cross-platform"
        }

        val automaticEnvironmentName = "Automatic ($defaultEnvironmentName)"
        val kdeEnvironmentName = "KDE"
        val waylandEnvironmentName = "Wayland"
        val genericEnvironmentName = "Cross-platform"
        val virtualEnvironmentName = "Windowed"

        environmentComboBox = JComboBox<String>()
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

        environmentHelpButton = JButton("?")
        environmentHelpButton.putClientProperty("JButton.buttonType", "help")
        environmentHelpButton.addActionListener {
            val environment = when (newProperties.getProperty("Environment", "")) {
                "kde" -> "kde"
                "wayland" -> "wayland"
                "generic" -> "generic"
                "virtual" -> "virtual"
                else -> NativeFactory.default
            }

            val message = when (environment) {
                "kde" -> "The KDE environment allows Shimeji to interact with windows on KDE Plasma."
                "wayland" -> "The Wayland environment displays Shimeji on Wayland layer surfaces. It only works on Wayland compositors that implement the wlr-layer-shell protocol."
                "generic" -> "The cross-platform environment should work on any operating system and desktop environment."
                "virtual" -> "The windowed environment displays all Shimeji in a single window."
                else -> ""
            }

            JOptionPane.showMessageDialog(this, message, localize("About"), JOptionPane.INFORMATION_MESSAGE)
        }

        environmentPanel = JPanel()
        environmentPanel.layout = BoxLayout(environmentPanel, BoxLayout.X_AXIS)
        environmentPanel.add(environmentComboBox)
        environmentPanel.add(Box.createHorizontalStrut(5))
        environmentPanel.add(environmentHelpButton)

        useWaylandLayersCheckBoxKde = JCheckBox("Use Wayland Layers")
        useWaylandLayersCheckBoxKde.isVisible = Platform.sessionType == "wayland"
        useWaylandLayersCheckBoxKde.isSelected = getProperty("UseWaylandLayersKde", true) && useWaylandLayersCheckBoxKde.isVisible
        useWaylandLayersCheckBoxKde.horizontalAlignment = SwingConstants.LEFT
        useWaylandLayersCheckBoxKde.addActionListener {
            newProperties.setProperty("UseWaylandLayersKde", useWaylandLayersCheckBoxKde.isSelected.toString())
            layerPanelKde.isVisible = useWaylandLayersCheckBoxKde.isSelected
        }

        layerComboBoxKde = JComboBox<String>()
        layerComboBoxKde.addItem("Background")
        layerComboBoxKde.addItem("Bottom")
        layerComboBoxKde.addItem("Top")
        layerComboBoxKde.addItem("Overlay")
        layerComboBoxKde.selectedIndex = getProperty("MascotLayerWayland", 3)
        layerComboBoxKde.addActionListener {
            newProperties.setProperty("MascotLayerWayland", layerComboBoxKde.selectedIndex.toString())
            layerComboBoxWayland.selectedIndex = layerComboBoxKde.selectedIndex
        }

        layerPanelKde = JPanel(BorderLayout())
        layerPanelKde.isVisible = useWaylandLayersCheckBoxKde.isSelected
        layerPanelKde.alignmentX = LEFT_ALIGNMENT
        layerPanelKde.add(JLabel("Layer"), BorderLayout.WEST)
        layerPanelKde.add(layerComboBoxKde, BorderLayout.EAST)

        screenDimensionsPanelKde = ScreenDimensionsPanel()
        screenDimensionsPanelKde.alignmentX = LEFT_ALIGNMENT

        kdeEnvironmentSettingsPanel = JPanel()
        kdeEnvironmentSettingsPanel.layout = BoxLayout(kdeEnvironmentSettingsPanel, BoxLayout.Y_AXIS)
        kdeEnvironmentSettingsPanel.add(useWaylandLayersCheckBoxKde)
        kdeEnvironmentSettingsPanel.add(layerPanelKde)
        kdeEnvironmentSettingsPanel.add(screenDimensionsPanelKde)

        kdeEnvironmentCard = JPanel(BorderLayout())
        kdeEnvironmentCard.add(kdeEnvironmentSettingsPanel, BorderLayout.NORTH)

        layerComboBoxWayland = JComboBox<String>()
        layerComboBoxWayland.addItem("Background")
        layerComboBoxWayland.addItem("Bottom")
        layerComboBoxWayland.addItem("Top")
        layerComboBoxWayland.addItem("Overlay")
        layerComboBoxWayland.selectedIndex = getProperty("MascotLayerWayland", 3)
        layerComboBoxWayland.addActionListener {
            newProperties.setProperty("MascotLayerWayland", layerComboBoxWayland.selectedIndex.toString())
            layerComboBoxKde.selectedIndex = layerComboBoxWayland.selectedIndex
        }

        layerPanelWayland = JPanel(BorderLayout())
        layerPanelWayland.alignmentX = LEFT_ALIGNMENT
        layerPanelWayland.add(JLabel("Layer"), BorderLayout.WEST)
        layerPanelWayland.add(layerComboBoxWayland, BorderLayout.EAST)

        screenDimensionsPanelWayland = ScreenDimensionsPanel()
        screenDimensionsPanelWayland.alignmentX = LEFT_ALIGNMENT

        waylandEnvironmentSettingsPanel = JPanel()
        waylandEnvironmentSettingsPanel.layout = BoxLayout(waylandEnvironmentSettingsPanel, BoxLayout.Y_AXIS)
        waylandEnvironmentSettingsPanel.add(layerPanelWayland)
        waylandEnvironmentSettingsPanel.add(screenDimensionsPanelWayland)

        waylandEnvironmentCard = JPanel(BorderLayout())
        waylandEnvironmentCard.add(waylandEnvironmentSettingsPanel, BorderLayout.NORTH)

        genericEnvironmentSettingsPanel = JPanel()
        genericEnvironmentSettingsPanel.layout = BoxLayout(genericEnvironmentSettingsPanel, BoxLayout.Y_AXIS)
        genericEnvironmentSettingsPanel.add(ScreenDimensionsPanel())

        genericEnvironmentCard = JPanel(BorderLayout())
        genericEnvironmentCard.add(genericEnvironmentSettingsPanel, BorderLayout.NORTH)

        val (windowWidth, windowHeight) = getProperty("WindowSize", "600x500").split('x').map { it.toIntOrNull() ?: 0 }

        widthSpinner = JSpinner()
        widthSpinner.value = windowWidth
        widthSpinner.addChangeListener {
            val (_, windowHeight) = newProperties.getProperty("WindowSize", "600x500").split('x')
            newProperties.setProperty("WindowSize", "${widthSpinner.value}x${windowHeight}")
        }

        heightSpinner = JSpinner()
        heightSpinner.value = windowHeight
        heightSpinner.addChangeListener {
            val (windowWidth, _) = newProperties.getProperty("WindowSize", "600x500").split('x')
            newProperties.setProperty("WindowSize", "${windowWidth}x${heightSpinner.value}")
        }

        windowDimensionsRightPanel = JPanel()
        windowDimensionsRightPanel.layout = BoxLayout(windowDimensionsRightPanel, BoxLayout.X_AXIS)
        windowDimensionsRightPanel.add(widthSpinner)
        windowDimensionsRightPanel.add(Box.createHorizontalStrut(3))
        windowDimensionsRightPanel.add(JLabel("x"))
        windowDimensionsRightPanel.add(Box.createHorizontalStrut(3))
        windowDimensionsRightPanel.add(heightSpinner)

        windowDimensionsPanel = JPanel(BorderLayout())
        windowDimensionsPanel.alignmentX = LEFT_ALIGNMENT
        windowDimensionsPanel.add(JLabel(localize("Dimensions")), BorderLayout.WEST)
        windowDimensionsPanel.add(windowDimensionsRightPanel, BorderLayout.EAST)

        windowBackgroundColorTextField = object : JTextField(getProperty("Background", "#00FF00")) {
            override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
        }
        windowBackgroundColorTextField.addActionListener {
            val color = runCatching { Color.decode(windowBackgroundColorTextField.text) }.getOrNull()

            if (color != null) {
                newProperties.setProperty("Background", String.format("#%06X", color.rgb and 0xFFFFFF))
                repaint()
            }
        }

        windowBackgroundColorButton = JButton(localize("Change"))
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

        windowBackgroundColorPreview = object : JPanel() {
            init {
                border = BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED)
            }

            override fun getPreferredSize() = Dimension(
                windowBackgroundColorButton.preferredSize.height,
                windowBackgroundColorButton.preferredSize.height
            )

            override fun getBackground() = Color.decode(newProperties.getProperty("Background", "#00FF00"))
        }

        windowBackgroundColorRightPanel = JPanel()
        windowBackgroundColorRightPanel.layout = BoxLayout(windowBackgroundColorRightPanel, BoxLayout.X_AXIS)
        windowBackgroundColorRightPanel.add(windowBackgroundColorTextField)
        windowBackgroundColorRightPanel.add(Box.createHorizontalStrut(3))
        windowBackgroundColorRightPanel.add(windowBackgroundColorPreview)
        windowBackgroundColorRightPanel.add(Box.createHorizontalStrut(3))
        windowBackgroundColorRightPanel.add(windowBackgroundColorButton)

        windowBackgroundColorPanel = JPanel(BorderLayout())
        windowBackgroundColorPanel.add(JLabel(localize("Color")), BorderLayout.WEST)
        windowBackgroundColorPanel.add(windowBackgroundColorRightPanel, BorderLayout.EAST)

        windowBackgroundImageLabel = JLabel(localize("Image"))
        windowBackgroundImageLabel.verticalAlignment = SwingConstants.TOP

        windowBackgroundImagePreview = JLabel()
        windowBackgroundImagePreview.border = BorderFactory.createSoftBevelBorder(BevelBorder.LOWERED)

        refreshBackgroundImagePreview()

        windowBackgroundImagePreviewPanel = JPanel(BorderLayout())
        windowBackgroundImagePreviewPanel.preferredSize = Dimension(96, 96)
        windowBackgroundImagePreviewPanel.add(windowBackgroundImagePreview)

        windowBackgroundModeComboBox = JComboBox<String>()
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

        removeWindowBackgroundImageButton = JButton(localize("Remove"))
        removeWindowBackgroundImageButton.isEnabled = windowBackgroundModeComboBox.isEnabled
        removeWindowBackgroundImageButton.addActionListener {
            newProperties.setProperty("BackgroundImage", "")
            refreshBackgroundImagePreview()
            windowBackgroundModeComboBox.isEnabled = false
            removeWindowBackgroundImageButton.isEnabled = false
        }

        changeWindowBackgroundImageButton = JButton(localize("Change"))
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

        windowBackgroundImageRightPanel = JPanel(GridBagLayout())
        windowBackgroundColorRightPanel.alignmentX = RIGHT_ALIGNMENT
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

        windowBackgroundImagePanel = JPanel(BorderLayout())
        windowBackgroundImagePanel.add(windowBackgroundImageLabel, BorderLayout.WEST)
        windowBackgroundImagePanel.add(windowBackgroundImageRightPanel, BorderLayout.EAST)

        windowBackgroundPanel = JPanel()
        windowBackgroundPanel.layout = BoxLayout(windowBackgroundPanel, BoxLayout.Y_AXIS)
        windowBackgroundPanel.border = BorderFactory.createEmptyBorder(6, 12, 0, 0)
        windowBackgroundPanel.alignmentX = LEFT_ALIGNMENT
        windowBackgroundPanel.add(windowBackgroundColorPanel)
        windowBackgroundPanel.add(Box.createVerticalStrut(3))
        windowBackgroundPanel.add(windowBackgroundImagePanel)

        virtualEnvironmentSettingsPanel = JPanel()
        virtualEnvironmentSettingsPanel.layout = BoxLayout(virtualEnvironmentSettingsPanel, BoxLayout.Y_AXIS)
        virtualEnvironmentSettingsPanel.add(windowDimensionsPanel)
        virtualEnvironmentSettingsPanel.add(JLabel(localize("Background")))
        virtualEnvironmentSettingsPanel.add(windowBackgroundPanel)

        virtualEnvironmentCard = JPanel(BorderLayout())
        virtualEnvironmentCard.add(virtualEnvironmentSettingsPanel, BorderLayout.NORTH)

        environmentCardLayout = CardLayout()
        environmentCardsPanel = JPanel(environmentCardLayout)
        environmentCardsPanel.add(kdeEnvironmentCard, "kde")
        environmentCardsPanel.add(waylandEnvironmentCard, "wayland")
        environmentCardsPanel.add(genericEnvironmentCard, "generic")
        environmentCardsPanel.add(virtualEnvironmentCard, "virtual")

        environmentCardLayout.show(environmentCardsPanel, NativeFactory.current)

        environmentTab = JPanel()
        environmentTab.layout = BoxLayout(environmentTab, BoxLayout.Y_AXIS)
        environmentTab.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        environmentTab.add(environmentPanel)
        environmentTab.add(Box.createVerticalStrut(3))
        environmentTab.add(environmentCardsPanel)

        aboutIcon = JLabel()
        aboutIcon.icon = ImageIcon(icon.getScaledInstance(96, 96, Image.SCALE_DEFAULT))
        aboutIcon.alignmentX = CENTER_ALIGNMENT

        titleLabel = JLabel("ShimeLinux")
        titleLabel.font = titleLabel.font.deriveFont(Font.BOLD, titleLabel.font.size + 10.0f)
        titleLabel.alignmentX = CENTER_ALIGNMENT

        versionLabel = JLabel("v${BuildConfig.VERSION}")
        versionLabel.alignmentX = CENTER_ALIGNMENT

        aboutTab = JPanel()
        aboutTab.layout = BoxLayout(aboutTab, BoxLayout.Y_AXIS)
        aboutTab.add(Box.createVerticalGlue())
        aboutTab.add(aboutIcon)
        aboutTab.add(titleLabel)
        aboutTab.add(versionLabel)
        aboutTab.add(Box.createVerticalGlue())

        tabbedPane = object : JTabbedPane() {
            override fun getPreferredSize() = super.preferredSize.apply { width = 450 }
        }
        tabbedPane.addTab(localize("General"), generalTab)
        if (NativeFactory.current == "kde") {
            tabbedPane.addTab(localize("InteractiveWindows"), interactiveWindowsTab)
        }
        tabbedPane.addTab(localize("Theme"), themeTab)
        tabbedPane.addTab(localize("Environment"), environmentTab)
        tabbedPane.addTab(localize("About"), aboutTab)

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

        buttonsPanel = JPanel(FlowLayout())
        buttonsPanel.add(doneButton)
        buttonsPanel.add(cancelButton)

        add(tabbedPane, BorderLayout.CENTER)
        add(buttonsPanel, BorderLayout.SOUTH)
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
