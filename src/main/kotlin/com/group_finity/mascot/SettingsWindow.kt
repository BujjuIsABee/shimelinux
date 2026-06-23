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
import java.awt.Color
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Frame
import java.awt.GridBagLayout
import java.awt.Image
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
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.text.replace

class SettingsWindow(parent: Frame?, modal: Boolean) : JDialog(parent, modal) {
    private val lang = Main.instance.languageBundle

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
    private val themeTab: JPanel
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
    private val themeFooterPanel: JPanel
    private val menuScalingLabel: JLabel
    private val menuScalingSlider: JSlider
    private val resetButtonPanel: JPanel
    private val resetButton: JButton
    private val aboutTab: JPanel
    private val infoPanel: JPanel
    private val aboutImageLabel: JLabel
    private val shimelinuxLabel: JLabel
    private val versionLabel: JLabel
    private val footerPanel: JPanel
    private val doneButton: JButton
    private val cancelButton: JButton

    private var alwaysShowShimejiChooser = Main.instance.properties.getProperty("AlwaysShowShimejiChooser", "false").toBoolean()
    private var alwaysShowInformationScreen = Main.instance.properties.getProperty("AlwaysShowInformationScreen", "false").toBoolean()
    private var scaling = Main.instance.properties.getProperty("Scaling", "1.0").toDouble()
    private var opacity = Main.instance.properties.getProperty("Opacity", "1.0").toDouble()
    private var filter = Main.instance.properties.getProperty("Filter", "Nearest")
    private var theme = Main.instance.properties.getProperty("Theme", "FlatDark")
    private var menuScaling = Main.instance.properties.getProperty("MenuDPI", "96").toInt()
    private val initialTheme = theme
    private val darkTheme = Properties()
    private val lightTheme = Properties()
    private val initialDarkBackgroundColor: String
    private val initialDarkTextColor: String
    private val initialDarkAccentColor: String
    private val initialLightBackgroundColor: String
    private val initialLightTextColor: String
    private val initialLightAccentColor: String

    var isRestartRequired = false
    var isImageReloadRequired = false
    var isInteractiveWindowReloadRequired = false

    init {
        val icon = this::class.java.getResourceAsStream("/img/icon.png").use { ImageIO.read(it) }
        setIconImage(icon)
        title = lang.getString("Settings")
        layout = BorderLayout()

        try {
            Main.getPath("conf", "theme", "FlatDarkLaf.properties").inputStream().use {
                darkTheme.load(it)
            }

            Main.getPath("conf", "theme", "FlatLightLaf.properties").inputStream().use {
                lightTheme.load(it)
            }
        } catch (_: Exception) {
        }

        // Store initial theme colors
        initialDarkBackgroundColor = darkTheme.getProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
        initialDarkTextColor = darkTheme.getProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
        initialDarkAccentColor = darkTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
        initialLightBackgroundColor = lightTheme.getProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
        initialLightTextColor = lightTheme.getProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
        initialLightAccentColor = lightTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)

        alwaysShowShimejiChooserCheckBox = JCheckBox(lang.getString("AlwaysShowShimejiChooser"))
        alwaysShowShimejiChooserCheckBox.isSelected = alwaysShowShimejiChooser
        alwaysShowShimejiChooserCheckBox.addChangeListener {
            alwaysShowShimejiChooser = alwaysShowShimejiChooserCheckBox.isSelected
        }

        alwaysShowInformationScreenCheckBox = JCheckBox(lang.getString("AlwaysShowInformationScreen"))
        alwaysShowInformationScreenCheckBox.isSelected = alwaysShowInformationScreen
        alwaysShowInformationScreenCheckBox.addChangeListener {
            alwaysShowInformationScreen = alwaysShowInformationScreenCheckBox.isSelected
        }

        scalingSlider = JSlider()
        scalingSlider.maximum = 80
        scalingSlider.majorTickSpacing = 10
        scalingSlider.minorTickSpacing = 5
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

        opacitySlider = JSlider()
        opacitySlider.majorTickSpacing = 10
        opacitySlider.minorTickSpacing = 5
        opacitySlider.paintTicks = true
        opacitySlider.snapToTicks = true
        opacitySlider.value = (opacity * 100.0).toInt()
        opacitySlider.addChangeListener {
            if (opacitySlider.value / 100.0 != opacity) {
                opacity = opacitySlider.value / 100.0
                isImageReloadRequired = true
            }
        }

        nearestNeighborRadioButton = JRadioButton(lang.getString("NearestNeighbour"))
        nearestNeighborRadioButton.isSelected = filter == "Nearest"
        nearestNeighborRadioButton.addChangeListener {
            if (nearestNeighborRadioButton.isSelected && filter != "Nearest") {
                filter = "Nearest"
                isImageReloadRequired = true
            }
        }

        bicubicRadioButton = JRadioButton(lang.getString("BicubicFilter"))
        bicubicRadioButton.isSelected = filter == "Bicubic"
        bicubicRadioButton.addChangeListener {
            if (bicubicRadioButton.isSelected && filter != "Bicubic") {
                filter = "Bicubic"
                isImageReloadRequired = true
            }
        }

        hqxRadioButton = JRadioButton(lang.getString("Filter"))
        hqxRadioButton.isSelected = filter == "Hqx"
        hqxRadioButton.addChangeListener {
            if (hqxRadioButton.isSelected && filter != "Hqx") {
                filter = "hqx"
                isImageReloadRequired = true
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
        generalTab.add(JLabel(lang.getString("Scaling")))
        generalTab.add(scalingSlider)
        generalTab.add(JLabel(lang.getString("Opacity")))
        generalTab.add(opacitySlider)
        generalTab.add(JLabel(lang.getString("FilterOptions")))
        generalTab.add(nearestNeighborRadioButton)
        generalTab.add(bicubicRadioButton)
        generalTab.add(hqxRadioButton)

        whitelistModel = DefaultListModel<String>()
        for (title in Main.instance.properties.getProperty("InteractiveWindows", "").split('/')) {
            if (title.isNotBlank()) {
                whitelistModel.add(whitelistModel.size, title)
            }
        }

        blacklistModel = DefaultListModel<String>()
        for (title in Main.instance.properties.getProperty("InteractiveWindowsBlacklist", "").split('/')) {
            if (title.isNotBlank()) {
                blacklistModel.add(blacklistModel.size, title)
            }
        }

        whitelist = JList(whitelistModel)
        blacklist = JList(blacklistModel)

        interactiveWindowsTabs = JTabbedPane()
        interactiveWindowsTabs.addTab(lang.getString("Whitelist"), JScrollPane(whitelist))
        interactiveWindowsTabs.addTab(lang.getString("Blacklist"), JScrollPane(blacklist))

        addInteractiveWindowButton = JButton(lang.getString("Add"))
        addInteractiveWindowButton.preferredSize = Dimension(130, 26)
        addInteractiveWindowButton.addActionListener { handleAddInteractiveWindowButtonAction() }

        removeInteractiveWindowButton = JButton(lang.getString("Remove"))
        removeInteractiveWindowButton.preferredSize = Dimension(130, 26)
        removeInteractiveWindowButton.addActionListener { handleRemoveInteractiveWindowButtonAction() }

        interactiveWindowsButtonsPanel = JPanel(FlowLayout())
        interactiveWindowsButtonsPanel.add(addInteractiveWindowButton)
        interactiveWindowsButtonsPanel.add(removeInteractiveWindowButton)

        interactiveWindowsTab = JPanel(BorderLayout())
        interactiveWindowsTab.add(interactiveWindowsTabs, BorderLayout.CENTER)
        interactiveWindowsTab.add(interactiveWindowsButtonsPanel, BorderLayout.SOUTH)

        themeComboBox = JComboBox<String>()
        themeComboBox.addItem("Flat Dark")
        themeComboBox.addItem("Flat Light")
        themeComboBox.addItem("GTK")

        backgroundColorButton = JButton(lang.getString("Change"))
        backgroundColorButton.addActionListener { handleChangeBackgroundColorButtonAction() }

        textColorButton = JButton(lang.getString("Change"))
        textColorButton.addActionListener { handleChangeTextColorButtonAction() }

        accentColorButton = JButton(lang.getString("Change"))
        accentColorButton.addActionListener { handleChangeAccentColorButtonAction() }

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
        backgroundColorPanel.add(JLabel(lang.getString("BackgroundColour")), BorderLayout.WEST)
        backgroundColorPanel.add(backgroundColorRightPanel, BorderLayout.EAST)

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
        textColorPanel.add(JLabel(lang.getString("TextColour")), BorderLayout.WEST)
        textColorPanel.add(textColorRightPanel, BorderLayout.EAST)

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
        accentColorPanel.add(JLabel(lang.getString("AccentColour")), BorderLayout.WEST)
        accentColorPanel.add(accentColorRightPanel, BorderLayout.EAST)

        flatThemeColorsPanel = JPanel()
        flatThemeColorsPanel.layout = BoxLayout(flatThemeColorsPanel, BoxLayout.Y_AXIS)
        flatThemeColorsPanel.add(Box.createVerticalStrut(6))
        flatThemeColorsPanel.add(backgroundColorPanel)
        flatThemeColorsPanel.add(Box.createVerticalStrut(6))
        flatThemeColorsPanel.add(textColorPanel)
        flatThemeColorsPanel.add(Box.createVerticalStrut(6))
        flatThemeColorsPanel.add(accentColorPanel)

        flatThemePanel = JPanel(BorderLayout())
        flatThemePanel.add(flatThemeColorsPanel, BorderLayout.NORTH)

        val themeMap = mapOf(0 to "FlatDark", 1 to "FlatLight", 2 to "GTK")
        val indexMap = themeMap.entries.associate { it.value to it.key }
        themeComboBox.addItemListener {
            theme = themeMap[themeComboBox.selectedIndex] ?: "FlatDark"
            refreshTheme()
            flatThemePanel.isVisible = themeComboBox.selectedIndex != 2
        }

        // Set the selected index after adding the item listener so the correct card is shown
        themeComboBox.selectedIndex = indexMap[theme] ?: 0

        resetButton = JButton(lang.getString("Reset"))
        resetButton.addActionListener { handleResetButtonAction() }

        resetButtonPanel = JPanel()
        resetButtonPanel.alignmentX = CENTER_ALIGNMENT
        resetButtonPanel.add(resetButton)

        menuScalingLabel = JLabel(lang.getString("MenuScaling"))
        menuScalingLabel.alignmentX = CENTER_ALIGNMENT

        menuScalingSlider = JSlider()
        menuScalingSlider.minimum = 1
        menuScalingSlider.maximum = 3
        menuScalingSlider.majorTickSpacing = 1
        menuScalingSlider.paintTicks = true
        menuScalingSlider.snapToTicks = true
        menuScalingSlider.paintLabels = true
        menuScalingSlider.labelTable = Hashtable(
            mapOf(
                1 to JLabel("1.0"),
                2 to JLabel("2.0"),
                3 to JLabel("3.0")
            )
        )
        menuScalingSlider.value = menuScaling / 96
        menuScalingSlider.addChangeListener {
            if (menuScalingSlider.value != menuScaling / 96) {
                menuScaling = menuScalingSlider.value * 96
                isRestartRequired = true
            }
        }

        themeFooterPanel = JPanel()
        themeFooterPanel.layout = BoxLayout(themeFooterPanel, BoxLayout.Y_AXIS)
        themeFooterPanel.add(menuScalingLabel)
        themeFooterPanel.add(menuScalingSlider)
        themeFooterPanel.add(resetButtonPanel)

        themeTab = JPanel(BorderLayout())
        themeTab.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        themeTab.add(themeComboBox, BorderLayout.NORTH)
        themeTab.add(flatThemePanel, BorderLayout.CENTER)
        themeTab.add(themeFooterPanel, BorderLayout.SOUTH)

        aboutImageLabel = JLabel()
        aboutImageLabel.icon = ImageIcon(icon.getScaledInstance(96, 96, Image.SCALE_DEFAULT))
        aboutImageLabel.alignmentX = CENTER_ALIGNMENT

        shimelinuxLabel = JLabel("ShimeLinux")
        shimelinuxLabel.font = shimelinuxLabel.font.deriveFont(Font.BOLD, shimelinuxLabel.font.size + 10.0f)
        shimelinuxLabel.alignmentX = CENTER_ALIGNMENT

        versionLabel = JLabel("v1.0.0")
        versionLabel.alignmentX = CENTER_ALIGNMENT

        infoPanel = JPanel()
        infoPanel.layout = BoxLayout(infoPanel, BoxLayout.Y_AXIS)
        infoPanel.add(aboutImageLabel)
        infoPanel.add(shimelinuxLabel)
        infoPanel.add(versionLabel)

        aboutTab = JPanel(GridBagLayout())
        aboutTab.add(infoPanel)

        mainTabs = JTabbedPane()
        mainTabs.addTab(lang.getString("General"), generalTab)
        mainTabs.addTab(lang.getString("InteractiveWindows"), interactiveWindowsTab)
        mainTabs.addTab(lang.getString("Theme"), themeTab)
        mainTabs.addTab(lang.getString("About"), aboutTab)

        // Don't show interactive windows tab unless the KDE environment is used
        if (System.getenv("XDG_CURRENT_DESKTOP") != "KDE") {
            interactiveWindowsTab.isVisible = false
        }

        doneButton = JButton(lang.getString("Done"))
        doneButton.addActionListener { handleDone() }

        cancelButton = JButton(lang.getString("Cancel"))
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
            lang.getString("InteractiveWindowHintMessage"),
            lang.getString(
                if (interactiveWindowsTabs.selectedIndex == 0) {
                    "AddInteractiveWindow"
                } else {
                    "BlacklistInteractiveWindow"
                }
            ),
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
            this@SettingsWindow,
            lang.getString("ChooseBackgroundColour"),
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
            this@SettingsWindow,
            lang.getString("ChooseTextColour"),
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
            this@SettingsWindow,
            lang.getString("ChooseAccentColour"),
            Color.decode(selectedTheme.getProperty("@accentColor", defaultColor)),
            false
        )

        if (color != null) {
            selectedTheme.setProperty("@accentColor", getHex(color))
            refreshTheme()
        }
    }

    private fun handleResetButtonAction() {
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

    private fun handleDone() {
        if (isRestartRequired) {
            val response = JOptionPane.showConfirmDialog(
                this,
                lang.getString("RestartRequiredMessage"),
                lang.getString("RestartRequired"),
                JOptionPane.YES_NO_OPTION
            )

            isRestartRequired = response == JOptionPane.YES_OPTION
        }

        Main.instance.properties.setProperty("AlwaysShowShimejiChooser", alwaysShowShimejiChooser.toString())
        Main.instance.properties.setProperty("AlwaysShowInformationScreen", alwaysShowInformationScreen.toString())
        Main.instance.properties.setProperty("Scaling", scaling.toString())
        Main.instance.properties.setProperty("Opacity", opacity.toString())
        Main.instance.properties.setProperty("Filter", filter.toString())
        Main.instance.properties.setProperty("Theme", theme)
        Main.instance.properties.setProperty("MenuDPI", menuScaling.toString())

        val whitelist = whitelistModel.elements().toList().toString()
            .replace("[", "")
            .replace("]", "")
            .replace(", ", "/")

        val blacklist = blacklistModel.elements().toList().toString()
            .replace("[", "")
            .replace("]", "")
            .replace(", ", "/")

        Main.instance.properties.setProperty("InteractiveWindows", whitelist)
        Main.instance.properties.setProperty("InteractiveWindowsBlacklist", blacklist)

        Main.getPath("conf", "settings.properties").outputStream().use {
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
        override fun getPreferredSize() = Dimension(button.preferredSize.height, button.preferredSize.height)

        override fun getBorder() = BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1)

        override fun getBackground() = Color.decode(
            if (themeComboBox.selectedIndex == 0) {
                darkTheme.getProperty(colorKey, colorDark)
            } else {
                lightTheme.getProperty(colorKey, colorLight)
            }
        )
    }

    private fun refreshTheme() {
        Main.getPath("conf", "theme", "FlatDarkLaf.properties").outputStream().use {
            darkTheme.store(it, "Flat Dark Theme")
        }

        Main.getPath("conf", "theme", "FlatLightLaf.properties").outputStream().use {
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
    }

    companion object {
        private const val DEFAULT_DARK_BACKGROUND_COLOR = "#202020"
        private const val DEFAULT_DARK_TEXT_COLOR = "#ffffff"
        private const val DEFAULT_LIGHT_BACKGROUND_COLOR = "#ffffff"
        private const val DEFAULT_LIGHT_TEXT_COLOR = "#000000"
        private const val DEFAULT_ACCENT_COLOR = "#3c83c5"

        private fun getHex(color: Color) = String.format("#%06X", color.rgb and 0xFFFFFF)
    }
}
