/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
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
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Image
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
    var isEnvironmentReloadRequired = false
    var isImageReloadRequired = false
    var isInteractiveWindowReloadRequired = false

    private var alwaysShowShimejiChooser = Main.instance.properties.getProperty("AlwaysShowShimejiChooser", "false").toBoolean()
    private var alwaysShowInformationScreen = Main.instance.properties.getProperty("AlwaysShowInformationScreen", "false").toBoolean()
    private var scaling = Main.instance.properties.getProperty("Scaling", "1.0").toDouble()
    private var opacity = Main.instance.properties.getProperty("Opacity", "1.0").toDouble()
    private var filter = Main.instance.properties.getProperty("Filter", "Nearest")
    private var theme = Main.instance.properties.getProperty("Theme", "GTK")

    private val lightTheme = Properties()
    private val darkTheme = Properties()

    private val initialTheme = theme
    private val initialLightBackgroundColor: String
    private val initialLightTextColor: String
    private val initialLightAccentColor: String
    private val initialDarkBackgroundColor: String
    private val initialDarkTextColor: String
    private val initialDarkAccentColor: String

    init {
        val lang = Main.instance.languageBundle

        runCatching {
            Main.getPath("conf", "theme", "FlatLightLaf.properties").inputStream().use {
                lightTheme.load(it)
            }

            Main.getPath("conf", "theme", "FlatDarkLaf.properties").inputStream().use {
                darkTheme.load(it)
            }
        }

        initialLightBackgroundColor = lightTheme.getProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
        initialLightTextColor = lightTheme.getProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
        initialLightAccentColor = lightTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
        initialDarkBackgroundColor = darkTheme.getProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
        initialDarkTextColor = darkTheme.getProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
        initialDarkAccentColor = darkTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)

        //region General Tab
        val alwaysShowShimejiChooserCheckBox = JCheckBox(lang.getString("AlwaysShowShimejiChooser"))
        alwaysShowShimejiChooserCheckBox.isSelected = alwaysShowShimejiChooser
        alwaysShowShimejiChooserCheckBox.addChangeListener {
            alwaysShowShimejiChooser = alwaysShowShimejiChooserCheckBox.isSelected
        }

        val alwaysShowInformationScreenCheckBox = JCheckBox(lang.getString("AlwaysShowInformationScreen"))
        alwaysShowInformationScreenCheckBox.isSelected = alwaysShowInformationScreen
        alwaysShowInformationScreenCheckBox.addChangeListener {
            alwaysShowInformationScreen = alwaysShowInformationScreenCheckBox.isSelected
        }

        val scalingSlider = JSlider()
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

        val opacitySlider = JSlider()
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

        val nearestNeighborRadioButton = JRadioButton(lang.getString("NearestNeighbour"))
        nearestNeighborRadioButton.isSelected = filter == "Nearest"
        nearestNeighborRadioButton.addChangeListener {
            if (nearestNeighborRadioButton.isSelected && filter != "Nearest") {
                filter = "Nearest"
                isImageReloadRequired = true
            }
        }

        val bicubicRadioButton = JRadioButton(lang.getString("BicubicFilter"))
        bicubicRadioButton.isSelected = filter == "Bicubic"
        bicubicRadioButton.addChangeListener {
            if (bicubicRadioButton.isSelected && filter != "Bicubic") {
                filter = "Bicubic"
                isImageReloadRequired = true
            }
        }

        val hqxRadioButton = JRadioButton(lang.getString("Filter"))
        hqxRadioButton.isSelected = filter == "Hqx"
        hqxRadioButton.addChangeListener {
            if (hqxRadioButton.isSelected && filter != "Hqx") {
                filter = "hqx"
                isImageReloadRequired = true
            }
        }

        val filterButtonGroup = ButtonGroup()
        filterButtonGroup.add(nearestNeighborRadioButton)
        filterButtonGroup.add(bicubicRadioButton)
        filterButtonGroup.add(hqxRadioButton)

        val generalTabPanel = JPanel()
        generalTabPanel.layout = BoxLayout(generalTabPanel, BoxLayout.Y_AXIS)
        generalTabPanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        generalTabPanel.add(alwaysShowShimejiChooserCheckBox)
        generalTabPanel.add(alwaysShowInformationScreenCheckBox)
        generalTabPanel.add(JLabel(lang.getString("Scaling")))
        generalTabPanel.add(scalingSlider)
        generalTabPanel.add(JLabel(lang.getString("Opacity")))
        generalTabPanel.add(opacitySlider)
        generalTabPanel.add(JLabel(lang.getString("FilterOptions")))
        generalTabPanel.add(nearestNeighborRadioButton)
        generalTabPanel.add(bicubicRadioButton)
        generalTabPanel.add(hqxRadioButton)
        //endregion

        //region Interactive Windows Tab
        val whitelistModel = DefaultListModel<String>()
        for (title in Main.instance.properties.getProperty("InteractiveWindows", "").split('/')) {
            if (title.isNotBlank()) {
                whitelistModel.add(whitelistModel.size, title)
            }
        }

        val blacklistModel = DefaultListModel<String>()
        for (title in Main.instance.properties.getProperty("InteractiveWindowsBlacklist", "").split('/')) {
            if (title.isNotBlank()) {
                blacklistModel.add(blacklistModel.size, title)
            }
        }

        val whitelist = JList(whitelistModel)
        val blacklist = JList(blacklistModel)

        val interactiveWindowsTabs = JTabbedPane()
        interactiveWindowsTabs.addTab(lang.getString("Whitelist"), JScrollPane(whitelist))
        interactiveWindowsTabs.addTab(lang.getString("Blacklist"), JScrollPane(blacklist))

        val addInteractiveWindowButton = JButton(lang.getString("Add"))
        addInteractiveWindowButton.preferredSize = Dimension(130, 26)
        addInteractiveWindowButton.addActionListener {
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

        val removeInteractiveWindowButton = JButton(lang.getString("Remove"))
        removeInteractiveWindowButton.preferredSize = Dimension(130, 26)
        removeInteractiveWindowButton.addActionListener {
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

        val interactiveWindowsButtonsPanel = JPanel(FlowLayout())
        interactiveWindowsButtonsPanel.add(addInteractiveWindowButton)
        interactiveWindowsButtonsPanel.add(removeInteractiveWindowButton)

        val interactiveWindowsTabPanel = JPanel(BorderLayout())
        interactiveWindowsTabPanel.add(interactiveWindowsTabs, BorderLayout.CENTER)
        interactiveWindowsTabPanel.add(interactiveWindowsButtonsPanel, BorderLayout.SOUTH)
        //endregion

        //region Theme Tab
        val themeMap = mapOf(
            0 to "GTK",
            1 to "FlatLight",
            2 to "FlatDark"
        )

        val indexMap = themeMap.entries.associate { it.value to it.key }

        val themeComboBox = JComboBox<String>()
        themeComboBox.addItem("GTK")
        themeComboBox.addItem("Flat Light")
        themeComboBox.addItem("Flat Dark")

        val gtkCardPanel = JPanel(GridBagLayout())
        gtkCardPanel.add(JLabel(lang.getString("ThemeCannotBeCustomized")))

        val changeBackgroundColorButton = JButton(lang.getString("Change"))
        changeBackgroundColorButton.addActionListener {
            val color = JColorChooser.showDialog(
                this@SettingsWindow,
                lang.getString("ChooseBackgroundColour"),
                Color.decode(
                    if (themeComboBox.selectedIndex == 1) {
                        lightTheme.getProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
                    } else {
                        darkTheme.getProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
                    }
                ),
                false
            )

            if (color != null) {
                val selectedTheme = if (themeComboBox.selectedIndex == 1) lightTheme else darkTheme
                selectedTheme.setProperty("@background", getHex(color))
                refreshTheme()
            }
        }

        val backgroundColorPreview = object : JPanel() {
            override fun getPreferredSize() = Dimension(
                changeBackgroundColorButton.preferredSize.height,
                changeBackgroundColorButton.preferredSize.height
            )

            override fun getBackground() = Color.decode(
                if (themeComboBox.selectedIndex == 1) {
                    lightTheme.getProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
                } else {
                    darkTheme.getProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
                }
            )
        }
        backgroundColorPreview.border = BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1)

        val editBackgroundColorPanel = JPanel()
        editBackgroundColorPanel.layout = BoxLayout(editBackgroundColorPanel, BoxLayout.X_AXIS)
        editBackgroundColorPanel.add(backgroundColorPreview)
        editBackgroundColorPanel.add(Box.createHorizontalStrut(3))
        editBackgroundColorPanel.add(changeBackgroundColorButton)

        val editBackgroundColorCell = JPanel(FlowLayout(FlowLayout.RIGHT))
        editBackgroundColorCell.add(editBackgroundColorPanel)

        val changeTextColorButton = JButton(lang.getString("Change"))
        changeTextColorButton.addActionListener {
            val color = JColorChooser.showDialog(
                this@SettingsWindow,
                lang.getString("ChooseTextColour"),
                Color.decode(
                    if (themeComboBox.selectedIndex == 1) {
                        lightTheme.getProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
                    } else {
                        darkTheme.getProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
                    }
                ),
                false
            )

            if (color != null) {
                val selectedTheme = if (themeComboBox.selectedIndex == 1) lightTheme else darkTheme
                selectedTheme.setProperty("@foreground", getHex(color))
                refreshTheme()
            }
        }

        val textColorPreview = object : JPanel() {
            override fun getPreferredSize() = Dimension(
                changeTextColorButton.preferredSize.height,
                changeTextColorButton.preferredSize.height
            )

            override fun getBackground() = Color.decode(
                if (themeComboBox.selectedIndex == 1) {
                    lightTheme.getProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
                } else {
                    darkTheme.getProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
                }
            )
        }
        textColorPreview.border = BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1)

        val editTextColorPanel = JPanel()
        editTextColorPanel.layout = BoxLayout(editTextColorPanel, BoxLayout.X_AXIS)
        editTextColorPanel.add(textColorPreview)
        editTextColorPanel.add(Box.createHorizontalStrut(3))
        editTextColorPanel.add(changeTextColorButton)

        val editTextColorCell = JPanel(FlowLayout(FlowLayout.RIGHT))
        editTextColorCell.add(editTextColorPanel)

        val changeAccentColorButton = JButton(lang.getString("Change"))
        changeAccentColorButton.addActionListener {
            val color = JColorChooser.showDialog(
                this@SettingsWindow,
                lang.getString("ChooseAccentColour"),
                Color.decode(
                    if (themeComboBox.selectedIndex == 1) {
                        lightTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                    } else {
                        darkTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                    }
                ),
                false
            )

            if (color != null) {
                val selectedTheme = if (themeComboBox.selectedIndex == 1) lightTheme else darkTheme
                selectedTheme.setProperty("@accentColor", getHex(color))
                refreshTheme()
            }
        }

        val accentColorPreview = object : JPanel() {
            override fun getPreferredSize() = Dimension(
                changeAccentColorButton.preferredSize.height,
                changeAccentColorButton.preferredSize.height
            )

            override fun getBackground() = Color.decode(
                if (themeComboBox.selectedIndex == 1) {
                    lightTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                } else {
                    darkTheme.getProperty("@accentColor", DEFAULT_ACCENT_COLOR)
                }
            )
        }
        accentColorPreview.border = BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1)

        val editAccentColorPanel = JPanel()
        editAccentColorPanel.layout = BoxLayout(editAccentColorPanel, BoxLayout.X_AXIS)
        editAccentColorPanel.add(accentColorPreview)
        editAccentColorPanel.add(Box.createHorizontalStrut(3))
        editAccentColorPanel.add(changeAccentColorButton)

        val editAccentColorCell = JPanel(FlowLayout(FlowLayout.RIGHT))
        editAccentColorCell.add(editAccentColorPanel)

        val themeCustomizer = JPanel(GridLayout(3, 2))
        themeCustomizer.add(JLabel(lang.getString("BackgroundColour")))
        themeCustomizer.add(editBackgroundColorCell)
        themeCustomizer.add(JLabel(lang.getString("TextColour")))
        themeCustomizer.add(editTextColorCell)
        themeCustomizer.add(JLabel(lang.getString("AccentColour")))
        themeCustomizer.add(editAccentColorCell)

        val resetButton = JButton(lang.getString("Reset"))
        resetButton.addActionListener {
            if (themeComboBox.selectedIndex == 1) {
                lightTheme.setProperty("@background", DEFAULT_LIGHT_BACKGROUND_COLOR)
                lightTheme.setProperty("@foreground", DEFAULT_LIGHT_TEXT_COLOR)
                lightTheme.setProperty("@accentColor", DEFAULT_ACCENT_COLOR)
            } else {
                darkTheme.setProperty("@background", DEFAULT_DARK_BACKGROUND_COLOR)
                darkTheme.setProperty("@foreground", DEFAULT_DARK_TEXT_COLOR)
                darkTheme.setProperty("@accentColor", DEFAULT_ACCENT_COLOR)
            }

            refreshTheme()
        }

        val resetButtonPanel = JPanel(FlowLayout())
        resetButtonPanel.add(resetButton)

        val flatCardPanel = JPanel(BorderLayout())
        flatCardPanel.add(themeCustomizer, BorderLayout.NORTH)
        flatCardPanel.add(resetButtonPanel, BorderLayout.SOUTH)

        val cardLayout = CardLayout()
        val cardsPanel = JPanel(cardLayout)

        cardsPanel.add(gtkCardPanel, "GtkCard")
        cardsPanel.add(flatCardPanel, "FlatCard")

        themeComboBox.addItemListener {
            theme = themeMap[themeComboBox.selectedIndex] ?: "GTK"
            refreshTheme()

            cardLayout.show(cardsPanel, if (theme == "GTK") "GtkCard" else "FlatCard")
        }

        themeComboBox.selectedIndex = indexMap[theme] ?: 0

        val themeTabPanel = JPanel(BorderLayout())
        themeTabPanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        themeTabPanel.add(themeComboBox, BorderLayout.NORTH)
        themeTabPanel.add(cardsPanel, BorderLayout.CENTER)
        //endregion

        //region About Tab
        val aboutImageLabel = JLabel()
        val icon = this::class.java.getResourceAsStream("/img/icon.png").use { ImageIO.read(it) }
        aboutImageLabel.icon = ImageIcon(icon.getScaledInstance(96, 96, Image.SCALE_DEFAULT))
        aboutImageLabel.alignmentX = CENTER_ALIGNMENT

        val shimelinuxLabel = JLabel("ShimeLinux")
        shimelinuxLabel.font = shimelinuxLabel.font.deriveFont(Font.BOLD, shimelinuxLabel.font.size + 10.0f)
        shimelinuxLabel.alignmentX = CENTER_ALIGNMENT

        val versionLabel = JLabel("v0.2.0")
        versionLabel.alignmentX = CENTER_ALIGNMENT

        val aboutPanel = JPanel()
        aboutPanel.layout = BoxLayout(aboutPanel, BoxLayout.Y_AXIS)
        aboutPanel.add(aboutImageLabel)
        aboutPanel.add(shimelinuxLabel)
        aboutPanel.add(versionLabel)

        val aboutTabPanel = JPanel(GridBagLayout())
        aboutTabPanel.add(aboutPanel)
        //endregion

        val tabs = JTabbedPane()
        tabs.addTab(lang.getString("General"), generalTabPanel)
        tabs.addTab(lang.getString("InteractiveWindows"), interactiveWindowsTabPanel)
        tabs.addTab(lang.getString("Theme"), themeTabPanel)
        tabs.addTab(lang.getString("About"), aboutTabPanel)

        // Don't show interactive windows tab unless the KDE environment is used
        if (System.getenv("XDG_CURRENT_DESKTOP") != "KDE") {
            interactiveWindowsTabPanel.isVisible = false
        }

        val doneButton = JButton(lang.getString("Done"))
        doneButton.addActionListener {
            Main.instance.properties.setProperty("AlwaysShowShimejiChooser", alwaysShowShimejiChooser.toString())
            Main.instance.properties.setProperty("AlwaysShowInformationScreen", alwaysShowInformationScreen.toString())
            Main.instance.properties.setProperty("Scaling", scaling.toString())
            Main.instance.properties.setProperty("Opacity", opacity.toString())
            Main.instance.properties.setProperty("Filter", filter.toString())
            Main.instance.properties.setProperty("Theme", theme)

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
                Main.instance.properties.store(it, "ShimeLinux Configuration Options")
            }

            dispose()
        }

        val cancelButton = JButton(lang.getString("Cancel"))
        cancelButton.addActionListener {
            // Reset theme
            lightTheme.setProperty("@background", initialLightBackgroundColor)
            lightTheme.setProperty("@foreground", initialLightTextColor)
            lightTheme.setProperty("@accentColor", initialLightAccentColor)
            darkTheme.setProperty("@background", initialDarkBackgroundColor)
            darkTheme.setProperty("@foreground", initialDarkTextColor)
            darkTheme.setProperty("@accentColor", initialDarkAccentColor)
            theme = initialTheme
            refreshTheme()

            isEnvironmentReloadRequired = false
            isImageReloadRequired = false
            isInteractiveWindowReloadRequired = false

            dispose()
        }

        val buttonsPanel = JPanel(FlowLayout())
        buttonsPanel.add(doneButton)
        buttonsPanel.add(cancelButton)

        setIconImage(icon)
        title = lang.getString("Settings")
        contentPane.layout = BorderLayout()
        add(tabs, BorderLayout.CENTER)
        add(buttonsPanel, BorderLayout.SOUTH)
        pack()
        setLocationRelativeTo(null)
    }

    private fun refreshTheme() = runCatching {
        Main.getPath("conf", "theme", "FlatLightLaf.properties").outputStream().use {
            lightTheme.store(it, null)
        }

        Main.getPath("conf", "theme", "FlatDarkLaf.properties").outputStream().use {
            darkTheme.store(it, null)
        }

        UIManager.setLookAndFeel(
            when (theme) {
                "GTK" -> "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
                "FlatLight" -> "com.formdev.flatlaf.FlatLightLaf"
                "FlatDark" -> "com.formdev.flatlaf.FlatDarkLaf"
                else -> "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
            }
        )

        FlatLaf.updateUI()
        SwingUtilities.updateComponentTreeUI(this)
    }

    companion object {
        private const val DEFAULT_LIGHT_BACKGROUND_COLOR = "#ffffff"
        private const val DEFAULT_LIGHT_TEXT_COLOR = "#000000"
        private const val DEFAULT_DARK_BACKGROUND_COLOR = "#202020"
        private const val DEFAULT_DARK_TEXT_COLOR = "#ffffff"
        private const val DEFAULT_ACCENT_COLOR = "#3c83c5"

        private fun getHex(color: Color) = String
            .format("#%06X", color.rgb and 0xFFFFFF)
            .replace("\\", "")
    }
}
