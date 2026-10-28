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
import java.io.IOException
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
import javax.swing.SpinnerNumberModel
import javax.swing.SwingConstants
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.border.BevelBorder
import kotlin.collections.map
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.math.round

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
    private val themeComboBox: JComboBox<String>
    private val flatThemePanel: JPanel
    private val flatThemeSettingsPanel: JPanel
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
    private val menuScalingPanel: JPanel
    private val menuScalingSpinner: JSpinner
    private val flatThemeButtonsPanel: JPanel
    private val useGtkColorsButton: JButton
    private val resetFlatThemeButton: JButton
    private val environmentTab: JPanel
    private val environmentPanel: JPanel
    private val environmentComboBox: JComboBox<String>
    private val environmentHelpButton: JButton
    private val environmentCardLayout: CardLayout
    private val environmentCardsPanel: JPanel
    private val kdeEnvironmentCard: KdeEnvironmentCard
    private val waylandEnvironmentCard: WaylandEnvironmentCard
    private val genericEnvironmentCard: GenericEnvironmentCard
    private val virtualEnvironmentCard: VirtualEnvironmentCard
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

    /**
     * Whether a restart is required to apply the changes.
     */
    var isRestartRequired = false

    /**
     * Whether [NativeFactory.instance] needs to be reset to apply the changes.
     */
    var isEnvironmentReloadRequired = false

    /**
     * Whether all Shimeji image sets need to be reloaded to apply the changes.
     */
    var isImageReloadRequired = false

    /**
     * Whether the cache of allowed/disallowed interactive window titles needs to be reset to apply the changes.
     */
    var isInteractiveWindowReloadRequired = false

    init {
        val icon = loadResource("img/icon.png").use { ImageIO.read(it) }
        setIconImage(icon)
        title = localize("Settings")
        minimumSize = Dimension(450, 373)
        isResizable = !Platform.isTilingWindowManager
        layout = BorderLayout()

        try {
            getPath("conf", "theme", "FlatDarkLaf.properties").inputStream().use { oldDarkTheme.load(it) }
            getPath("conf", "theme", "FlatLightLaf.properties").inputStream().use { oldLightTheme.load(it) }
        } catch (_: IOException) {
        }

        newDarkTheme.putAll(oldDarkTheme)
        newLightTheme.putAll(oldLightTheme)
        newProperties.putAll(Main.properties)

        alwaysShowShimejiChooserCheckBox = JCheckBox(localize("AlwaysShowShimejiChooser"))
        alwaysShowShimejiChooserCheckBox.isSelected = getProperty("AlwaysShowShimejiChooser", false)
        alwaysShowShimejiChooserCheckBox.addChangeListener {
            setNewProperty("AlwaysShowShimejiChooser", alwaysShowShimejiChooserCheckBox.isSelected)
        }

        alwaysShowInformationScreenCheckBox = JCheckBox(localize("AlwaysShowInformationScreen"))
        alwaysShowInformationScreenCheckBox.isSelected = getProperty("AlwaysShowInformationScreen", false)
        alwaysShowInformationScreenCheckBox.addChangeListener {
            setNewProperty("AlwaysShowInformationScreen", alwaysShowInformationScreenCheckBox.isSelected)
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

            setNewProperty("Scaling", scalingSlider.value / 10.0)
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
            setNewProperty("Opacity", opacitySlider.value / 100.0)
        }

        val filter = getProperty("Filter", "Nearest")

        nearestNeighborRadioButton = JRadioButton(localize("NearestNeighbor"))
        nearestNeighborRadioButton.isSelected = filter == "Nearest"
        nearestNeighborRadioButton.addChangeListener {
            if (nearestNeighborRadioButton.isSelected) {
                setNewProperty("Filter", "Nearest")
            }
        }

        bicubicRadioButton = JRadioButton(localize("BicubicFilter"))
        bicubicRadioButton.isSelected = filter == "Bicubic"
        bicubicRadioButton.addChangeListener {
            if (bicubicRadioButton.isSelected) {
                setNewProperty("Filter", "Bicubic")
            }
        }

        hqxRadioButton = JRadioButton(localize("HqxFilter"))
        hqxRadioButton.isSelected = filter == "Hqx"
        hqxRadioButton.addChangeListener {
            if (hqxRadioButton.isSelected) {
                setNewProperty("Filter", "Hqx")
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

        val interactiveWindowsWhitelistModel = DefaultListModel<String>()
        interactiveWindowsWhitelistModel.addAll(getProperty("InteractiveWindows", "").split('/').filter { it.isNotBlank() })
        interactiveWindowsWhitelist = JList(interactiveWindowsWhitelistModel)

        val interactiveWindowsBlacklistModel = DefaultListModel<String>()
        interactiveWindowsBlacklistModel.addAll(getProperty("InteractiveWindowsBlacklist", "").split('/').filter { it.isNotBlank() })
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
                    setNewProperty("InteractiveWindows", interactiveWindowsWhitelistModel.elements().toList().joinToString("/"))
                } else {
                    interactiveWindowsBlacklistModel.addElement(input.trim())
                    setNewProperty("InteractiveWindowsBlacklist", interactiveWindowsBlacklistModel.elements().toList().joinToString("/"))
                }
            }
        }

        removeInteractiveWindowButton = JButton(localize("Remove"))
        removeInteractiveWindowButton.addActionListener {
            if (interactiveWindowsTabbedPane.selectedIndex == 0) {
                if (interactiveWindowsWhitelist.selectedIndex != -1) {
                    interactiveWindowsWhitelistModel.remove(interactiveWindowsWhitelist.selectedIndex)
                    setNewProperty("InteractiveWindows", interactiveWindowsWhitelistModel.elements().toList().joinToString("/"))
                }
            } else {
                if (interactiveWindowsBlacklist.selectedIndex != -1) {
                    interactiveWindowsBlacklistModel.remove(interactiveWindowsBlacklist.selectedIndex)
                    setNewProperty("InteractiveWindowsBlacklist", interactiveWindowsBlacklistModel.elements().toList().joinToString("/"))
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
        themeComboBox.addItem(localize("FlatDarkTheme"))
        themeComboBox.addItem(localize("FlatLightTheme"))
        themeComboBox.addActionListener {
            setNewProperty("Theme", if (themeComboBox.selectedIndex == 0) "FlatDark" else "FlatLight")
            refreshTheme()
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
                    newDarkTheme.getProperty("@background", "#202020")
                } else {
                    newLightTheme.getProperty("@background", "#ffffff")
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
                        newDarkTheme.getProperty("@background", "#202020")
                    } else {
                        newLightTheme.getProperty("@background", "#ffffff")
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

        flatThemeTextColorButton = JButton(localize("Change"))
        flatThemeTextColorButton.addActionListener {
            val color = JColorChooser.showDialog(
                this,
                localize("ChooseTextColor"),
                Color.decode(
                    if (themeComboBox.selectedIndex == 0) {
                        newDarkTheme.getProperty("@foreground", "#ffffff")
                    } else {
                        newLightTheme.getProperty("@foreground", "#000000")
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
                    newDarkTheme.getProperty("@accentColor", "#3c83c5")
                } else {
                    newLightTheme.getProperty("@accentColor", "#3c83c5")
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
                        newDarkTheme.getProperty("@accentColor", "#3c83c5")
                    } else {
                        newLightTheme.getProperty("@accentColor", "#3c83c5")
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

        menuScalingSpinner = object : JSpinner(SpinnerNumberModel(1.0, 1.0, 3.0, 1.0)) {
            override fun getPreferredSize(): Dimension = super.preferredSize.apply { width = flatThemeBackgroundColorButton.preferredSize.width }
        }
        menuScalingSpinner.value = getNewProperty("MenuScaling", System.getProperty("sun.java2d.uiScale") ?: "1").toInt().toDouble()
        menuScalingSpinner.editor = JSpinner.NumberEditor(menuScalingSpinner, "0%")
        menuScalingSpinner.addChangeListener {
            val newValue = round(menuScalingSpinner.value as Double)
            menuScalingSpinner.value = newValue
            setNewProperty("MenuScaling", newValue.toInt())
        }

        menuScalingPanel = JPanel(BorderLayout())
        menuScalingPanel.add(JLabel(localize("MenuScaling")), BorderLayout.WEST)
        menuScalingPanel.add(menuScalingSpinner, BorderLayout.EAST)

        flatThemeSettingsPanel = JPanel()
        flatThemeSettingsPanel.layout = BoxLayout(flatThemeSettingsPanel, BoxLayout.Y_AXIS)
        flatThemeSettingsPanel.add(Box.createVerticalStrut(3))
        flatThemeSettingsPanel.add(flatThemeBackgroundColorPanel)
        flatThemeSettingsPanel.add(Box.createVerticalStrut(3))
        flatThemeSettingsPanel.add(flatThemeTextColorPanel)
        flatThemeSettingsPanel.add(Box.createVerticalStrut(3))
        flatThemeSettingsPanel.add(flatThemeAccentColorPanel)
        flatThemeSettingsPanel.add(Box.createVerticalStrut(3))
        flatThemeSettingsPanel.add(menuScalingPanel)

        resetFlatThemeButton = JButton(localize("Reset"))
        resetFlatThemeButton.addActionListener {
            if (themeComboBox.selectedIndex == 0) {
                newDarkTheme.setProperty("@background", "#202020")
                newDarkTheme.setProperty("@foreground", "#ffffff")
                newDarkTheme.setProperty("@accentColor", "#3c83c5")
            } else {
                newLightTheme.setProperty("@background", "#ffffff")
                newLightTheme.setProperty("@foreground", "#000000")
                newLightTheme.setProperty("@accentColor", "#3c83c5")
            }
            refreshTheme()
        }

        useGtkColorsButton = JButton(localize("UseGtkColors"))
        useGtkColorsButton.addActionListener {
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

        flatThemeButtonsPanel = JPanel(FlowLayout())
        flatThemeButtonsPanel.add(resetFlatThemeButton)
        if (!Platform.isFlatpak) {
            flatThemeButtonsPanel.add(useGtkColorsButton)
        }

        flatThemePanel = JPanel(BorderLayout())
        flatThemePanel.add(flatThemeSettingsPanel, BorderLayout.NORTH)
        flatThemePanel.add(flatThemeButtonsPanel, BorderLayout.SOUTH)

        themeComboBox.selectedIndex = when (getProperty("Theme", "FlatDark")) {
            "FlatDark" -> 0
            "FlatLight" -> 1
            else -> 0
        }

        themeTab = JPanel()
        themeTab.layout = BoxLayout(themeTab, BoxLayout.Y_AXIS)
        themeTab.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        themeTab.add(themeComboBox)
        themeTab.add(flatThemePanel)

        val kdeEnvironmentName = localize("KdeEnvironment")
        val waylandEnvironmentName = localize("WaylandEnvironment")
        val genericEnvironmentName = localize("GenericEnvironment")
        val virtualEnvironmentName = localize("VirtualEnvironment")
        val automaticEnvironmentName = buildString {
            append(localize("AutomaticEnvironment"))
            append(" (")
            append(
                when (defaultEnvironment) {
                    "kde" -> kdeEnvironmentName
                    "wayland" -> waylandEnvironmentName
                    else -> genericEnvironmentName
                }
            )
            append(')')
        }

        environmentComboBox = JComboBox()
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
            setNewProperty(
                "Environment", when (environmentComboBox.selectedItem as String) {
                    kdeEnvironmentName -> "kde"
                    waylandEnvironmentName -> "wayland"
                    genericEnvironmentName -> "generic"
                    virtualEnvironmentName -> "virtual"
                    else -> ""
                }
            )

            val environment = when (getNewProperty("Environment", "")) {
                "kde" -> "kde"
                "wayland" -> "wayland"
                "generic" -> "generic"
                "virtual" -> "virtual"
                else -> defaultEnvironment
            }

            when (environment) {
                "kde" -> kdeEnvironmentCard.update()
                "wayland" -> waylandEnvironmentCard.update()
                "generic" -> genericEnvironmentCard.update()
                "virtual" -> virtualEnvironmentCard.update()
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

        environmentHelpButton = JButton()
        environmentHelpButton.putClientProperty("JButton.buttonType", "help")
        environmentHelpButton.addActionListener {
            val environment = when (getNewProperty("Environment", "")) {
                "kde" -> "kde"
                "wayland" -> "wayland"
                "generic" -> "generic"
                "virtual" -> "virtual"
                else -> defaultEnvironment
            }

            val message = when (environment) {
                "kde" -> localize("KdeEnvironmentHelpText")
                "wayland" -> localize("WaylandEnvironmentHelpText")
                "generic" -> localize("GenericEnvironmentHelpText")
                "virtual" -> localize("VirtualEnvironmentHelpText")
                else -> ""
            }

            JOptionPane.showMessageDialog(this, message, localize("About"), JOptionPane.INFORMATION_MESSAGE)
        }

        environmentPanel = JPanel()
        environmentPanel.layout = BoxLayout(environmentPanel, BoxLayout.X_AXIS)
        environmentPanel.add(environmentComboBox)
        environmentPanel.add(Box.createHorizontalStrut(5))
        environmentPanel.add(environmentHelpButton)

        kdeEnvironmentCard = KdeEnvironmentCard()
        kdeEnvironmentCard.update()

        waylandEnvironmentCard = WaylandEnvironmentCard()
        waylandEnvironmentCard.update()

        genericEnvironmentCard = GenericEnvironmentCard()
        genericEnvironmentCard.update()

        virtualEnvironmentCard = VirtualEnvironmentCard()
        virtualEnvironmentCard.update()

        environmentCardLayout = CardLayout()
        environmentCardsPanel = JPanel(environmentCardLayout)
        environmentCardsPanel.add(kdeEnvironmentCard, "kde")
        environmentCardsPanel.add(waylandEnvironmentCard, "wayland")
        environmentCardsPanel.add(genericEnvironmentCard, "generic")
        environmentCardsPanel.add(virtualEnvironmentCard, "virtual")

        environmentCardLayout.show(environmentCardsPanel, currentEnvironment)

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

        tabbedPane = JTabbedPane()
        tabbedPane.addTab(localize("General"), generalTab)
        if (currentEnvironment == "kde") {
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
        if (getProperty("Scaling", "1.0") != getNewProperty("Scaling", "1.0") ||
            getProperty("Opacity", "1.0") != getNewProperty("Opacity", "1.0") ||
            getProperty("Filter", "Nearest") != getNewProperty("Filter", "Nearest")
        ) {
            isImageReloadRequired = true
        }

        if (getProperty("InteractiveWindows", "") != getNewProperty("InteractiveWindows", "") ||
            getProperty("InteractiveWindowsBlacklist", "") != getNewProperty("InteractiveWindowsBlacklist", "")
        ) {
            isInteractiveWindowReloadRequired = true
        }

        if (getProperty("Environment", "") != getNewProperty("Environment", "") ||
            getProperty("UseWaylandLayersKde", "true") != getNewProperty("UseWaylandLayersKde", "true") ||
            getProperty("MascotLayerWayland", "3") != getNewProperty("MascotLayerWayland", "3") ||
            getProperty("WindowSize", "600x500") != getNewProperty("WindowSize", "600x500") ||
            getProperty("Background", "#00FF00") != getNewProperty("Background", "#00FF00") ||
            getProperty("BackgroundImage", "") != getNewProperty("BackgroundImage", "") ||
            getProperty("BackgroundMode", "Center") != getNewProperty("BackgroundMode", "Center")
        ) {
            isEnvironmentReloadRequired = true
        }

        if (getProperty("MenuScaling", System.getProperty("sun.java2d.uiScale") ?: "1") != getNewProperty("MenuScaling", System.getProperty("sun.java2d.uiScale") ?: "1")) {
            isRestartRequired = true
        }

        Main.properties.putAll(newProperties)
        Main.updateConfigFile()
    }

    private fun cancelChanges() {
        setNewProperty("Theme", getProperty("Theme", "FlatDark"))
        newDarkTheme.putAll(oldDarkTheme)
        newLightTheme.putAll(oldLightTheme)
        refreshTheme()
    }

    private fun refreshTheme() {
        try {
            val theme = getNewProperty("Theme", "FlatDark")

            FlatLaf.setGlobalExtraDefaults(mapOf())

            // Update text fields
            if (theme == "FlatDark") {
                flatThemeBackgroundColorTextField.text = newDarkTheme.getProperty("@background", "#202020")
                flatThemeTextColorTextField.text = newDarkTheme.getProperty("@foreground", "#ffffff")
                flatThemeAccentColorTextField.text = newDarkTheme.getProperty("@accentColor", "#3c83c5")
            } else {
                flatThemeBackgroundColorTextField.text = newLightTheme.getProperty("@background", "#ffffff")
                flatThemeTextColorTextField.text = newLightTheme.getProperty("@foreground", "#000000")
                flatThemeAccentColorTextField.text = newLightTheme.getProperty("@accentColor", "#3c83c5")
            }

            // Save theme properties
            getPath("conf", "theme", "FlatDarkLaf.properties").outputStream().use {
                newDarkTheme.store(it, "Flat Dark Theme")
            }
            getPath("conf", "theme", "FlatLightLaf.properties").outputStream().use {
                newLightTheme.store(it, "Flat Light Theme")
            }

            UIManager.setLookAndFeel(
                when (theme) {
                    "FlatDark" -> "com.formdev.flatlaf.FlatDarkLaf"
                    "FlatLight" -> "com.formdev.flatlaf.FlatLightLaf"
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

    private fun <T> setNewProperty(key: String, value: T) {
        newProperties[key] = value.toString()
    }

    private inline fun <reified T> getNewProperty(key: String, defaultValue: T) : T =
        newProperties.getProperty(key, defaultValue.toString()).let { value ->
            when (T::class) {
                Int::class -> value.toInt()
                Double::class -> value.toDouble()
                Boolean::class -> value.toBoolean()
                else -> value
            } as? T ?: defaultValue
        }

    private inner class ScreenDimensionsPanel : JPanel() {
        private val overrideScreenDimensionsCheckBox: JCheckBox
        private val screenPositionPanel: JPanel
        private val screenPositionRightPanel: JPanel
        private val screenXSpinner: JSpinner
        private val screenYSpinner: JSpinner
        private val screenSizePanel: JPanel
        private val screenSizeRightPanel: JPanel
        private val screenWidthSpinner: JSpinner
        private val screenHeightSpinner: JSpinner

        init {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            alignmentX = LEFT_ALIGNMENT

            overrideScreenDimensionsCheckBox = JCheckBox(localize("OverrideScreenDimensions"))
            overrideScreenDimensionsCheckBox.horizontalAlignment = SwingConstants.LEFT
            overrideScreenDimensionsCheckBox.addActionListener {
                setNewProperty("OverrideScreenDimensions", overrideScreenDimensionsCheckBox.isSelected)
                screenPositionPanel.isVisible = overrideScreenDimensionsCheckBox.isSelected
                screenSizePanel.isVisible = overrideScreenDimensionsCheckBox.isSelected
            }

            screenXSpinner = object : JSpinner() {
                override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
            }
            screenXSpinner.addChangeListener {
                setNewProperty("ScreenX", screenXSpinner.value as Int)
            }

            screenYSpinner = object : JSpinner() {
                override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
            }
            screenYSpinner.addChangeListener {
                setNewProperty("ScreenY", screenYSpinner.value as Int)
            }

            screenPositionRightPanel = JPanel()
            screenPositionRightPanel.layout = BoxLayout(screenPositionRightPanel, BoxLayout.X_AXIS)
            screenPositionRightPanel.add(screenXSpinner)
            screenPositionRightPanel.add(Box.createHorizontalStrut(3))
            screenPositionRightPanel.add(JLabel("x"))
            screenPositionRightPanel.add(Box.createHorizontalStrut(3))
            screenPositionRightPanel.add(screenYSpinner)

            screenPositionPanel = JPanel(BorderLayout())
            screenPositionPanel.alignmentX = LEFT_ALIGNMENT
            screenPositionPanel.add(JLabel(localize("Position")), BorderLayout.WEST)
            screenPositionPanel.add(screenPositionRightPanel, BorderLayout.EAST)

            screenWidthSpinner = object : JSpinner() {
                override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
            }
            screenWidthSpinner.addChangeListener {
                setNewProperty("ScreenWidth", screenWidthSpinner.value as Int)
            }

            screenHeightSpinner = object : JSpinner() {
                override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
            }
            screenHeightSpinner.addChangeListener {
                setNewProperty("ScreenHeight", screenHeightSpinner.value as Int)
            }

            screenSizeRightPanel = JPanel()
            screenSizeRightPanel.layout = BoxLayout(screenSizeRightPanel, BoxLayout.X_AXIS)
            screenSizeRightPanel.add(screenWidthSpinner)
            screenSizeRightPanel.add(Box.createHorizontalStrut(3))
            screenSizeRightPanel.add(JLabel("x"))
            screenSizeRightPanel.add(Box.createHorizontalStrut(3))
            screenSizeRightPanel.add(screenHeightSpinner)

            screenSizePanel = JPanel(BorderLayout())
            screenSizePanel.alignmentX = LEFT_ALIGNMENT
            screenSizePanel.add(JLabel(localize("Size")), BorderLayout.WEST)
            screenSizePanel.add(screenSizeRightPanel, BorderLayout.EAST)

            add(overrideScreenDimensionsCheckBox)
            add(screenPositionPanel)
            add(Box.createVerticalStrut(3))
            add(screenSizePanel)
        }

        fun update() {
            overrideScreenDimensionsCheckBox.isSelected = getNewProperty("OverrideScreenDimensions", false)
            screenPositionPanel.isVisible = overrideScreenDimensionsCheckBox.isSelected
            screenXSpinner.value = getNewProperty("ScreenX", NativeFactory.instance.environment.screen.left)
            screenYSpinner.value = getNewProperty("ScreenY", NativeFactory.instance.environment.screen.top)
            screenSizePanel.isVisible = overrideScreenDimensionsCheckBox.isSelected
            screenWidthSpinner.value = getNewProperty("ScreenWidth", NativeFactory.instance.environment.screen.width)
            screenHeightSpinner.value = getNewProperty("ScreenHeight", NativeFactory.instance.environment.screen.height)
        }
    }

    private inner class KdeEnvironmentCard : JPanel() {
        private val kdeEnvironmentSettingsPanel: JPanel
        private val useWaylandLayersCheckBox: JCheckBox
        private val layerPanel: JPanel
        private val layerComboBox: JComboBox<String>
        private val screenDimensionsPanel: ScreenDimensionsPanel

        init {
            layout = BorderLayout()

            useWaylandLayersCheckBox = JCheckBox(localize("UseWaylandLayers"))
            useWaylandLayersCheckBox.isVisible = Platform.supportsWaylandLayerSurfaces
            useWaylandLayersCheckBox.horizontalAlignment = SwingConstants.LEFT
            useWaylandLayersCheckBox.addActionListener {
                setNewProperty("UseWaylandLayersKde", useWaylandLayersCheckBox.isSelected)
                layerPanel.isVisible = useWaylandLayersCheckBox.isSelected
            }

            layerComboBox = JComboBox()
            layerComboBox.addItem(localize("BackgroundLayer"))
            layerComboBox.addItem(localize("BottomLayer"))
            layerComboBox.addItem(localize("TopLayer"))
            layerComboBox.addItem(localize("OverlayLayer"))
            layerComboBox.addActionListener {
                setNewProperty("MascotLayerWayland", layerComboBox.selectedIndex)
            }

            layerPanel = JPanel(BorderLayout())
            layerPanel.alignmentX = LEFT_ALIGNMENT
            layerPanel.add(JLabel(localize("Layer")), BorderLayout.WEST)
            layerPanel.add(layerComboBox, BorderLayout.EAST)

            screenDimensionsPanel = ScreenDimensionsPanel()
            screenDimensionsPanel.alignmentX = LEFT_ALIGNMENT

            kdeEnvironmentSettingsPanel = JPanel()
            kdeEnvironmentSettingsPanel.layout = BoxLayout(kdeEnvironmentSettingsPanel, BoxLayout.Y_AXIS)
            kdeEnvironmentSettingsPanel.add(useWaylandLayersCheckBox)
            kdeEnvironmentSettingsPanel.add(layerPanel)
            kdeEnvironmentSettingsPanel.add(screenDimensionsPanel)

            add(kdeEnvironmentSettingsPanel, BorderLayout.NORTH)
        }

        fun update() {
            useWaylandLayersCheckBox.isSelected = getNewProperty("UseWaylandLayersKde", true)
            layerPanel.isVisible = useWaylandLayersCheckBox.isSelected
            layerComboBox.selectedIndex = getNewProperty("MascotLayerWayland", 3)
            screenDimensionsPanel.update()
        }
    }

    private inner class WaylandEnvironmentCard : JPanel() {
        private val waylandEnvironmentSettingsPanel: JPanel
        private val layerPanel: JPanel
        private val layerComboBox: JComboBox<String>
        private val screenDimensionsPanel: ScreenDimensionsPanel

        init {
            layout = BorderLayout()

            layerComboBox = JComboBox()
            layerComboBox.addItem("Background")
            layerComboBox.addItem("Bottom")
            layerComboBox.addItem("Top")
            layerComboBox.addItem("Overlay")
            layerComboBox.addActionListener {
                setNewProperty("MascotLayerWayland", layerComboBox.selectedIndex)
            }

            layerPanel = JPanel(BorderLayout())
            layerPanel.alignmentX = LEFT_ALIGNMENT
            layerPanel.add(JLabel(localize("Layer")), BorderLayout.WEST)
            layerPanel.add(layerComboBox, BorderLayout.EAST)

            screenDimensionsPanel = ScreenDimensionsPanel()
            screenDimensionsPanel.alignmentX = LEFT_ALIGNMENT

            waylandEnvironmentSettingsPanel = JPanel()
            waylandEnvironmentSettingsPanel.layout = BoxLayout(waylandEnvironmentSettingsPanel, BoxLayout.Y_AXIS)
            waylandEnvironmentSettingsPanel.add(layerPanel)
            waylandEnvironmentSettingsPanel.add(screenDimensionsPanel)

            add(waylandEnvironmentSettingsPanel, BorderLayout.NORTH)
        }

        fun update() {
            layerComboBox.selectedIndex = getNewProperty("MascotLayerWayland", 3)
            screenDimensionsPanel.update()
        }
    }

    private inner class GenericEnvironmentCard : JPanel() {
        private val genericEnvironmentSettingsPanel: JPanel
        private val screenDimensionsPanel: ScreenDimensionsPanel

        init {
            layout = BorderLayout()

            screenDimensionsPanel = ScreenDimensionsPanel()
            screenDimensionsPanel.alignmentX = LEFT_ALIGNMENT

            genericEnvironmentSettingsPanel = JPanel()
            genericEnvironmentSettingsPanel.layout = BoxLayout(genericEnvironmentSettingsPanel, BoxLayout.Y_AXIS)
            genericEnvironmentSettingsPanel.add(screenDimensionsPanel)

            add(genericEnvironmentSettingsPanel, BorderLayout.NORTH)
        }

        fun update() {
            screenDimensionsPanel.update()
        }
    }

    private inner class VirtualEnvironmentCard : JPanel() {
        private val virtualEnvironmentSettingsPanel: JPanel
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
        private val windowBackgroundModeComboBox: JComboBox<String>
        private val removeWindowBackgroundImageButton: JButton
        private val changeWindowBackgroundImageButton: JButton

        init {
            layout = BorderLayout()

            widthSpinner = JSpinner()
            widthSpinner.addChangeListener {
                val (_, windowHeight) = getNewProperty("WindowSize", "600x500").split('x')
                setNewProperty("WindowSize", "${widthSpinner.value}x${windowHeight}")
            }

            heightSpinner = JSpinner()
            heightSpinner.addChangeListener {
                val (windowWidth, _) = getNewProperty("WindowSize", "600x500").split('x')
                setNewProperty("WindowSize", "${windowWidth}x${heightSpinner.value}")
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

            windowBackgroundColorTextField = object : JTextField() {
                override fun getPreferredSize() = super.preferredSize.apply { width = 69 }
            }
            windowBackgroundColorTextField.addActionListener {
                val color = runCatching { Color.decode(windowBackgroundColorTextField.text) }.getOrNull()

                if (color != null) {
                    setNewProperty("Background", String.format("#%06X", color.rgb and 0xFFFFFF))
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

                override fun getBackground() = Color.decode(getNewProperty("Background", "#00FF00"))
            }

            windowBackgroundColorButton = object : JButton(localize("Change")) {
                override fun getPreferredSize(): Dimension = windowBackgroundModeComboBox.preferredSize
            }
            windowBackgroundColorButton.addActionListener {
                val color = JColorChooser.showDialog(
                    this,
                    localize("ChooseBackgroundColor"),
                    Color.decode(getNewProperty("Background", "#00FF00")),
                    false
                )

                if (color != null) {
                    val hex = String.format("#%06X", color.rgb and 0xFFFFFF)
                    setNewProperty("Background", hex)
                    windowBackgroundColorTextField.text = hex
                    repaint()
                }
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

            windowBackgroundImagePreviewPanel = JPanel(BorderLayout())
            windowBackgroundImagePreviewPanel.preferredSize = Dimension(96, 96)
            windowBackgroundImagePreviewPanel.add(windowBackgroundImagePreview)

            windowBackgroundModeComboBox = JComboBox()
            windowBackgroundModeComboBox.addItem(localize("BackgroundModeCenter"))
            windowBackgroundModeComboBox.addItem(localize("BackgroundModeFit"))
            windowBackgroundModeComboBox.addItem(localize("BackgroundModeStretch"))
            windowBackgroundModeComboBox.addItem(localize("BackgroundModeFill"))
            windowBackgroundModeComboBox.addItemListener {
                setNewProperty(
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
            removeWindowBackgroundImageButton.addActionListener {
                setNewProperty("BackgroundImage", "")
                refreshBackgroundImagePreview()
                windowBackgroundModeComboBox.isEnabled = false
                removeWindowBackgroundImageButton.isEnabled = false
            }

            changeWindowBackgroundImageButton = JButton(localize("Change"))
            changeWindowBackgroundImageButton.addActionListener {
                val dialog = JFileChooser()
                dialog.dialogTitle = localize("ChooseBackgroundImage")

                if (dialog.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    setNewProperty("BackgroundImage", dialog.selectedFile.canonicalPath)
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

            add(virtualEnvironmentSettingsPanel, BorderLayout.NORTH)
        }

        fun update() {
            val (windowWidth, windowHeight) = getProperty("WindowSize", "600x500").split('x').map { it.toIntOrNull() ?: 0 }
            widthSpinner.value = windowWidth
            heightSpinner.value = windowHeight
            windowBackgroundColorTextField.text = getNewProperty("Background", "#00FF00")
            refreshBackgroundImagePreview()
            windowBackgroundModeComboBox.isEnabled = getNewProperty("BackgroundImage", "") != ""
            windowBackgroundModeComboBox.selectedIndex =
                when (getNewProperty("BackgroundMode", "Center")) {
                    "Center" -> 0
                    "Fit" -> 1
                    "Stretch" -> 2
                    "Fill" -> 3
                    else -> 0
                }
            removeWindowBackgroundImageButton.isEnabled = windowBackgroundModeComboBox.isEnabled
        }

        private fun refreshBackgroundImagePreview() {
            var image = ImageIcon(getNewProperty("BackgroundImage", "")).image
            val size = Dimension(96, 96)
            val mode = getNewProperty("BackgroundMode", "Center")

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
    }
}
