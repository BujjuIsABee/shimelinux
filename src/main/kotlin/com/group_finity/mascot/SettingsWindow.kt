/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot

import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Frame
import java.awt.GridBagLayout
import java.awt.Image
import javax.imageio.ImageIO
import javax.swing.BorderFactory
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
import javax.swing.UnsupportedLookAndFeelException
import kotlin.io.path.outputStream
import kotlin.text.replace

class SettingsWindow(parent: Frame?, modal: Boolean) : JDialog(parent, modal) {
    private val conf = Main.instance.properties

    private var alwaysShowShimejiChooser = conf.getProperty("AlwaysShowShimejiChooser", "false").toBoolean()
    private var alwaysShowInformationScreen = conf.getProperty("AlwaysShowInformationScreen", "false").toBoolean()
    private var scaling = conf.getProperty("Scaling", "1.0").toDouble()
    private var opacity = conf.getProperty("Opacity", "1.0").toDouble()
    private var filter = conf.getProperty("Filter", "false")
    private var theme = conf.getProperty("Theme", "GTK")

    private val initialTheme = theme
    var isEnvironmentReloadRequired = false
    var isImageReloadRequired = false
    var isInteractiveWindowReloadRequired = false

    init {
        val lang = Main.instance.languageBundle

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
        nearestNeighborRadioButton.isSelected = filter.equals("false", true) || filter.equals("nearest", true)
        nearestNeighborRadioButton.addChangeListener {
            if (nearestNeighborRadioButton.isSelected) {
                if (!filter.equals("false", true) &&
                    !filter.equals("nearest", true)
                ) {
                    filter = "nearest"
                    isImageReloadRequired = true
                }
            }
        }

        val bicubicRadioButton = JRadioButton(lang.getString("BicubicFilter"))
        bicubicRadioButton.isSelected = filter.equals("bicubic", true)
        bicubicRadioButton.addChangeListener {
            if (bicubicRadioButton.isSelected) {
                if (!filter.equals("bicubic", true)) {
                    filter = "bicubic"
                    isImageReloadRequired = true
                }
            }
        }

        val hqxRadioButton = JRadioButton(lang.getString("Filter"))
        hqxRadioButton.isSelected = filter.equals("true", true) || filter.equals("hqx", true)
        hqxRadioButton.addChangeListener {
            if (hqxRadioButton.isSelected) {
                if (!filter.equals("true", true) &&
                    !filter.equals("hqx", true)
                ) {
                    filter = "hqx"
                    isImageReloadRequired = true
                }
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
        for (title in conf.getProperty("InteractiveWindows", "").split('/')) {
            if (title.isNotBlank()) {
                whitelistModel.add(whitelistModel.size, title)
            }
        }

        val blacklistModel = DefaultListModel<String>()
        for (title in conf.getProperty("InteractiveWindowsBlacklist", "").split('/')) {
            if (title.isNotBlank()) {
                blacklistModel.add(blacklistModel.size, title)
            }
        }

        val whitelistList = JList(whitelistModel)
        val blacklistList = JList(blacklistModel)

        val whitelistBlacklistTabbedPane = JTabbedPane()
        whitelistBlacklistTabbedPane.addTab(lang.getString("Whitelist"), JScrollPane(whitelistList))
        whitelistBlacklistTabbedPane.addTab(lang.getString("Blacklist"), JScrollPane(blacklistList))

        val addInteractiveWindowButton = JButton(lang.getString("Add"))
        addInteractiveWindowButton.preferredSize = Dimension(130, 26)
        addInteractiveWindowButton.addActionListener {
            val input = JOptionPane.showInputDialog(
                rootPane,
                lang.getString("InteractiveWindowHintMessage"),
                lang.getString(
                    if (whitelistBlacklistTabbedPane.selectedIndex == 0) {
                        "AddInteractiveWindow"
                    } else {
                        "BlacklistInteractiveWindow"
                    }
                ),
                JOptionPane.QUESTION_MESSAGE
            )

            if (!input.isNullOrBlank() && !input.contains("/")) {
                if (whitelistBlacklistTabbedPane.selectedIndex == 0) {
                    whitelistModel.add(whitelistModel.size, input.trim())
                    whitelistList.model = whitelistModel
                } else {
                    blacklistModel.add(blacklistModel.size, input.trim())
                    blacklistList.model = blacklistModel
                }
            }

            isInteractiveWindowReloadRequired = true
        }

        val removeInteractiveWindowButton = JButton(lang.getString("Remove"))
        removeInteractiveWindowButton.preferredSize = Dimension(130, 26)
        removeInteractiveWindowButton.addActionListener {
            if (whitelistBlacklistTabbedPane.selectedIndex == 0) {
                if (whitelistList.selectedIndex != -1) {
                    whitelistModel.remove(whitelistList.selectedIndex)
                    whitelistList.model = whitelistModel
                }
            } else {
                if (blacklistList.selectedIndex != -1) {
                    blacklistModel.remove(blacklistList.selectedIndex)
                    blacklistList.model = blacklistModel
                }
            }

            isInteractiveWindowReloadRequired = true
        }

        val interactiveWindowsButtonsPanel = JPanel(FlowLayout())
        interactiveWindowsButtonsPanel.add(addInteractiveWindowButton)
        interactiveWindowsButtonsPanel.add(removeInteractiveWindowButton)

        val interactiveWindowsTabPanel = JPanel(BorderLayout())
        interactiveWindowsTabPanel.add(whitelistBlacklistTabbedPane, BorderLayout.CENTER)
        interactiveWindowsTabPanel.add(interactiveWindowsButtonsPanel, BorderLayout.SOUTH)
        //endregion

        //region Theme Tab
        val gtkCardPanel = JPanel(GridBagLayout())
        gtkCardPanel.add(JLabel("This theme cannot be customized"))

        val flatLightCardPanel = JPanel()
        flatLightCardPanel.layout = BoxLayout(flatLightCardPanel, BoxLayout.Y_AXIS)
        flatLightCardPanel.add(JColorChooser())

        val flatDarkCardPanel = JPanel()
        flatDarkCardPanel.layout = BoxLayout(flatDarkCardPanel, BoxLayout.Y_AXIS)
        flatDarkCardPanel.add(JColorChooser())

        val themeCardsPanel = JPanel()
        val cardLayout = CardLayout()
        themeCardsPanel.layout = cardLayout
        themeCardsPanel.add(gtkCardPanel, "GTK")
        themeCardsPanel.add(flatLightCardPanel, "Flat (Light)")
        themeCardsPanel.add(flatDarkCardPanel, "Flat (Dark)")

        val themeMap = mapOf(
            themeCardsPanel.components.indexOf(gtkCardPanel) to "GTK",
            themeCardsPanel.components.indexOf(flatLightCardPanel) to "FlatLight",
            themeCardsPanel.components.indexOf(flatDarkCardPanel) to "FlatDark"
        )

        val indexMap = themeMap.entries.associate { it.value to it.key }

        val themeDropdown = JComboBox<String>()
        themeDropdown.addItem("GTK")
        themeDropdown.addItem("Flat (Light)")
        themeDropdown.addItem("Flat (Dark)")
        themeDropdown.addItemListener {
            theme = themeMap[themeDropdown.selectedIndex] ?: "GTK"

            refreshTheme()
            cardLayout.show(themeCardsPanel, themeDropdown.selectedItem?.toString() ?: "GTK")
        }

        themeDropdown.selectedIndex = indexMap[theme] ?: 0

        val themeTabPanel = JPanel()
        themeTabPanel.layout = BoxLayout(themeTabPanel, BoxLayout.Y_AXIS)
        themeTabPanel.add(themeDropdown)
        themeTabPanel.add(themeCardsPanel)
        //endregion

        //region About Tab
        val image = this::class.java.getResourceAsStream("/img/icon.png").use { ImageIO.read(it) }
        val aboutImageLabel = JLabel()
        aboutImageLabel.icon = ImageIcon(image.getScaledInstance(96, 96, Image.SCALE_DEFAULT))
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

        val tabbedPane = JTabbedPane()
        tabbedPane.addTab(lang.getString("General"), generalTabPanel)
        tabbedPane.addTab(lang.getString("InteractiveWindows"), interactiveWindowsTabPanel)
        tabbedPane.addTab(lang.getString("Theme"), themeTabPanel)
        tabbedPane.addTab(lang.getString("About"), aboutTabPanel)

        // Don't show interactive windows tab unless the KDE environment is used
        if (System.getenv("XDG_CURRENT_DESKTOP") != "KDE") {
            interactiveWindowsTabPanel.isVisible = false
        }

        val doneButton = JButton(lang.getString("Done"))
        doneButton.addActionListener {
            conf.setProperty("AlwaysShowShimejiChooser", alwaysShowShimejiChooser.toString())
            conf.setProperty("AlwaysShowInformationScreen", alwaysShowInformationScreen.toString())
            conf.setProperty("Scaling", scaling.toString())
            conf.setProperty("Opacity", opacity.toString())
            conf.setProperty("Filter", filter.toString())
            conf.setProperty("Theme", theme)

            val whitelist = whitelistModel.elements().toList().toString()
                .replace("[", "")
                .replace("]", "")
                .replace(", ", "/")

            val blacklist = blacklistModel.elements().toList().toString()
                .replace("[", "")
                .replace("]", "")
                .replace(", ", "/")

            conf.setProperty("InteractiveWindows", whitelist)
            conf.setProperty("InteractiveWindowsBlacklist", blacklist)

            Main.getPath("conf", "settings.properties").outputStream().use {
                conf.store(it, "ShimeLinux Configuration Options")
            }

            dispose()
        }

        val cancelButton = JButton(lang.getString("Cancel"))
        cancelButton.addActionListener {
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

        val icon = this::class.java.getResourceAsStream("/img/icon.png").use { ImageIO.read(it) }
        setIconImage(icon)
        title = lang.getString("Settings")
        contentPane.layout = BorderLayout()
        add(tabbedPane, BorderLayout.CENTER)
        add(buttonsPanel, BorderLayout.SOUTH)
        pack()
    }

    private fun refreshTheme() = runCatching {
        UIManager.setLookAndFeel(
            when (theme) {
                "GTK" -> "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
                "FlatLight" -> "com.formdev.flatlaf.FlatLightLaf"
                "FlatDark" -> "com.formdev.flatlaf.FlatDarkLaf"
                else -> "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
            }
        )
        SwingUtilities.updateComponentTreeUI(this)
    }
}
