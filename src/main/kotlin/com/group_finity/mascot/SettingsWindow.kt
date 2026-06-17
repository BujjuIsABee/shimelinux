/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot

import java.awt.BorderLayout
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

class SettingsWindow(parent: Frame, modal: Boolean) : JDialog(parent, modal) {
    var isEnvironmentReloadRequired = false
    var isImageReloadRequired = false
    var isInteractiveWindowReloadRequired = false

    private var alwaysShowShimejiChooser = Main.instance.properties.getProperty("AlwaysShowShimejiChooser", "false").toBoolean()
    private var alwaysShowInformationScreen = Main.instance.properties.getProperty("AlwaysShowInformationScreen", "false").toBoolean()
    private var scaling = Main.instance.properties.getProperty("Scaling", "1.0").toDouble()
    private var opacity = Main.instance.properties.getProperty("Opacity", "1.0").toDouble()
    private var filter = Main.instance.properties.getProperty("Filter", "false")
    private var theme = Main.instance.properties.getProperty("Theme", "GTK")
    private val initialTheme = theme

    private val tabbedPane: JTabbedPane
    private val generalTabPanel: JPanel
    private val alwaysShowShimejiChooserCheckBox: JCheckBox
    private val alwaysShowInformationScreenCheckBox: JCheckBox
    private val scalingLabel: JLabel
    private val scalingSlider: JSlider
    private val opacityLabel: JLabel
    private val opacitySlider: JSlider
    private val filterLabel: JLabel
    private val filterButtonGroup: ButtonGroup
    private val nearestNeighborRadioButton: JRadioButton
    private val bicubicRadioButton: JRadioButton
    private val hqxRadioButton: JRadioButton
    private val interactiveWindowsTabPanel: JPanel
    private val whitelistBlacklistTabbedPane: JTabbedPane
    private val whitelistPane: JScrollPane
    private val blacklistPane: JScrollPane
    private val whitelistList: JList<String>
    private val blacklistList: JList<String>
    private val interactiveWindowsButtonsPanel: JPanel
    private val addInteractiveWindowButton: JButton
    private val removeInteractiveWindowButton: JButton
    private val themeTabPanel: JPanel
    private val themeButtonGroup: ButtonGroup
    private val gtkThemeRadioButton: JRadioButton
    private val nimbusThemeRadioButton: JRadioButton
    private val metalThemeRadioButton: JRadioButton
    private val aboutTabPanel: JPanel
    private val aboutPanel: JPanel
    private val aboutImageLabel: JLabel
    private val shimelinuxLabel: JLabel
    private val versionLabel: JLabel
    private val buttonsPanel: JPanel
    private val doneButton: JButton
    private val cancelButton: JButton

    init {
        val icon = this::class.java.getResourceAsStream("/img/icon.png").use { ImageIO.read(it) }
        setIconImage(icon)

        title = Main.instance.languageBundle.getString("Settings")
        contentPane = JPanel(BorderLayout())

        alwaysShowShimejiChooserCheckBox = JCheckBox(Main.instance.languageBundle.getString("AlwaysShowShimejiChooser"))
        alwaysShowShimejiChooserCheckBox.isSelected = alwaysShowShimejiChooser
        alwaysShowShimejiChooserCheckBox.addChangeListener {
            alwaysShowShimejiChooser = alwaysShowShimejiChooserCheckBox.isSelected
        }

        alwaysShowInformationScreenCheckBox = JCheckBox(Main.instance.languageBundle.getString("AlwaysShowInformationScreen"))
        alwaysShowInformationScreenCheckBox.isSelected = alwaysShowInformationScreen
        alwaysShowInformationScreenCheckBox.addChangeListener {
            alwaysShowInformationScreen = alwaysShowInformationScreenCheckBox.isSelected
        }

        scalingLabel = JLabel(Main.instance.languageBundle.getString("Scaling"))
        scalingLabel.alignmentX = CENTER_ALIGNMENT

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

        opacityLabel = JLabel(Main.instance.languageBundle.getString("Opacity"))
        opacityLabel.alignmentX = CENTER_ALIGNMENT

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

        filterLabel = JLabel(Main.instance.languageBundle.getString("FilterOptions"))
        filterLabel.alignmentX = CENTER_ALIGNMENT

        nearestNeighborRadioButton = JRadioButton(Main.instance.languageBundle.getString("NearestNeighbour"))
        nearestNeighborRadioButton.isSelected = filter.equals("false", true) || filter.equals("nearest", true)
        nearestNeighborRadioButton.addChangeListener {
            if (nearestNeighborRadioButton.isSelected) {
                if (!filter.equals("false", true) && !filter.equals("nearest", true)) {
                    filter = "nearest"
                    isImageReloadRequired = true
                }
            }
        }

        bicubicRadioButton = JRadioButton(Main.instance.languageBundle.getString("BicubicFilter"))
        bicubicRadioButton.isSelected = filter.equals("bicubic", true)
        bicubicRadioButton.addChangeListener {
            if (bicubicRadioButton.isSelected) {
                if (!filter.equals("bicubic", true)) {
                    filter = "bicubic"
                    isImageReloadRequired = true
                }
            }
        }

        hqxRadioButton = JRadioButton(Main.instance.languageBundle.getString("Filter"))
        hqxRadioButton.isSelected = filter.equals("true", true) || filter.equals("hqx", true)
        hqxRadioButton.addChangeListener {
            if (hqxRadioButton.isSelected) {
                if (!filter.equals("true", true) && !filter.equals("hqx", true)) {
                    filter = "hqx"
                    isImageReloadRequired = true
                }
            }
        }

        filterButtonGroup = ButtonGroup()
        filterButtonGroup.add(nearestNeighborRadioButton)
        filterButtonGroup.add(bicubicRadioButton)
        filterButtonGroup.add(hqxRadioButton)

        generalTabPanel = JPanel()
        generalTabPanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        generalTabPanel.layout = BoxLayout(generalTabPanel, BoxLayout.Y_AXIS)
        generalTabPanel.add(alwaysShowShimejiChooserCheckBox)
        generalTabPanel.add(alwaysShowInformationScreenCheckBox)
        generalTabPanel.add(scalingLabel)
        generalTabPanel.add(scalingSlider)
        generalTabPanel.add(opacityLabel)
        generalTabPanel.add(opacitySlider)
        generalTabPanel.add(filterLabel)
        generalTabPanel.add(nearestNeighborRadioButton)
        generalTabPanel.add(bicubicRadioButton)
        generalTabPanel.add(hqxRadioButton)

        val whitelistModel = DefaultListModel<String>()
        for (title in Main.instance.properties.getProperty("InteractiveWindows", "").split('/')) {
            if (title.isNotBlank()) {
                whitelistModel.add(whitelistModel.size, title)
            }
        }

        whitelistList = JList(whitelistModel)
        whitelistPane = JScrollPane(whitelistList)

        val blacklistModel = DefaultListModel<String>()
        for (title in Main.instance.properties.getProperty("InteractiveWindowsBlacklist", "").split('/')) {
            if (title.isNotBlank()) {
                blacklistModel.add(blacklistModel.size, title)
            }
        }

        blacklistList = JList(blacklistModel)
        blacklistPane = JScrollPane(blacklistList)

        whitelistBlacklistTabbedPane = JTabbedPane()
        whitelistBlacklistTabbedPane.addTab(Main.instance.languageBundle.getString("Whitelist"), whitelistPane)
        whitelistBlacklistTabbedPane.addTab(Main.instance.languageBundle.getString("Blacklist"), blacklistPane)

        addInteractiveWindowButton = JButton(Main.instance.languageBundle.getString("Add"))
        addInteractiveWindowButton.preferredSize = Dimension(130, 26)
        addInteractiveWindowButton.addActionListener {
            val input = JOptionPane.showInputDialog(
                rootPane,
                Main.instance.languageBundle.getString("InteractiveWindowHintMessage"),
                Main.instance.languageBundle.getString(
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

        removeInteractiveWindowButton = JButton(Main.instance.languageBundle.getString("Remove"))
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

        interactiveWindowsButtonsPanel = JPanel(FlowLayout())
        interactiveWindowsButtonsPanel.add(addInteractiveWindowButton)
        interactiveWindowsButtonsPanel.add(removeInteractiveWindowButton)

        interactiveWindowsTabPanel = JPanel(BorderLayout())
        interactiveWindowsTabPanel.add(whitelistBlacklistTabbedPane, BorderLayout.CENTER)
        interactiveWindowsTabPanel.add(interactiveWindowsButtonsPanel, BorderLayout.SOUTH)

        gtkThemeRadioButton = JRadioButton("GTK")
        gtkThemeRadioButton.isSelected = theme == "GTK"
        gtkThemeRadioButton.addActionListener {
            if (gtkThemeRadioButton.isSelected) {
                if (theme != "GTK") {
                    theme = "GTK"
                    refreshTheme()
                }
            }
        }

        nimbusThemeRadioButton = JRadioButton("Nimbus")
        nimbusThemeRadioButton.isSelected = theme == "Nimbus"
        nimbusThemeRadioButton.addActionListener {
            if (nimbusThemeRadioButton.isSelected) {
                if (theme != "Nimbus") {
                    theme = "Nimbus"
                    refreshTheme()
                }
            }
        }

        metalThemeRadioButton = JRadioButton("Metal")
        metalThemeRadioButton.isSelected = theme == "Metal"
        metalThemeRadioButton.addActionListener {
            if (metalThemeRadioButton.isSelected) {
                if (theme != "Metal") {
                    theme = "Metal"
                    refreshTheme()
                }
            }
        }

        themeButtonGroup = ButtonGroup()
        themeButtonGroup.add(gtkThemeRadioButton)
        themeButtonGroup.add(nimbusThemeRadioButton)
        themeButtonGroup.add(metalThemeRadioButton)

        themeTabPanel = JPanel()
        themeTabPanel.border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        themeTabPanel.layout = BoxLayout(themeTabPanel, BoxLayout.Y_AXIS)
        themeTabPanel.add(gtkThemeRadioButton)
        themeTabPanel.add(nimbusThemeRadioButton)
        themeTabPanel.add(metalThemeRadioButton)

        val image = this::class.java.getResourceAsStream("/img/icon.png").use { ImageIO.read(it) }
        aboutImageLabel = JLabel()
        aboutImageLabel.icon = ImageIcon(image.getScaledInstance(96, 96, Image.SCALE_DEFAULT))
        aboutImageLabel.alignmentX = CENTER_ALIGNMENT

        shimelinuxLabel = JLabel("ShimeLinux")
        shimelinuxLabel.font = shimelinuxLabel.font.deriveFont(Font.BOLD, shimelinuxLabel.font.size + 10.0f)
        shimelinuxLabel.alignmentX = CENTER_ALIGNMENT

        versionLabel = JLabel("v0.2.0")
        versionLabel.alignmentX = CENTER_ALIGNMENT

        aboutPanel = JPanel()
        aboutPanel.layout = BoxLayout(aboutPanel, BoxLayout.Y_AXIS)
        aboutPanel.add(aboutImageLabel)
        aboutPanel.add(shimelinuxLabel)
        aboutPanel.add(versionLabel)

        aboutTabPanel = JPanel(GridBagLayout())
        aboutTabPanel.add(aboutPanel)

        tabbedPane = JTabbedPane()
        tabbedPane.addTab(Main.instance.languageBundle.getString("General"), generalTabPanel)
        tabbedPane.addTab(Main.instance.languageBundle.getString("InteractiveWindows"), interactiveWindowsTabPanel)
        tabbedPane.addTab(Main.instance.languageBundle.getString("Theme"), themeTabPanel)
        tabbedPane.addTab(Main.instance.languageBundle.getString("About"), aboutTabPanel)

        // Don't show interactive windows tab unless the KDE environment is used
        if (System.getenv("XDG_CURRENT_DESKTOP") != "KDE") {
            tabbedPane.remove(interactiveWindowsTabPanel)
        }

        doneButton = JButton(Main.instance.languageBundle.getString("Done"))
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

        cancelButton = JButton(Main.instance.languageBundle.getString("Cancel"))
        cancelButton.addActionListener {
            theme = initialTheme
            refreshTheme()
            dispose()
        }
        buttonsPanel = JPanel(FlowLayout())
        buttonsPanel.add(doneButton)
        buttonsPanel.add(cancelButton)

        add(tabbedPane, BorderLayout.CENTER)
        add(buttonsPanel, BorderLayout.SOUTH)

        pack()
    }

    fun display() {
        isVisible = true
    }

    private fun refreshTheme() {
        try {
            UIManager.setLookAndFeel(when (theme) {
                "GTK" -> "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
                "Nimbus" -> "javax.swing.plaf.nimbus.NimbusLookAndFeel"
                "Metal" -> "javax.swing.plaf.metal.MetalLookAndFeel"
                else -> "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
            })
            SwingUtilities.updateComponentTreeUI(this)
        } catch (_: UnsupportedLookAndFeelException) {
        }
    }
}
