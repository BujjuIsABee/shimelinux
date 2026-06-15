/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot

import io.github.bujjuisabee.shimelinux.KdeEnvironment
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Frame
import java.awt.Image
import javax.imageio.ImageIO
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
import kotlin.io.path.outputStream

class SettingsWindow(parent: Frame, modal: Boolean) : JDialog(parent, modal) {
    var isEnvironmentReloadRequired = false
    var isImageReloadRequired = false
    var isInteractiveWindowReloadRequired = false

    private var alwaysShowShimejiChooser = Main.instance.properties.getProperty("AlwaysShowShimejiChooser", "false").toBoolean()
    private var alwaysShowInformationScreen = Main.instance.properties.getProperty("AlwaysShowInformationScreen", "false").toBoolean()
    private var scaling = Main.instance.properties.getProperty("Scaling", "1.0").toDouble()
    private var opacity = Main.instance.properties.getProperty("Opacity", "1.0").toDouble()
    private var filter = Main.instance.properties.getProperty("Filter", "Replicate")
    private var theme = Main.instance.properties.getProperty("Theme", "GTK")
    private val initialTheme = theme

    private lateinit var tabsPane: JTabbedPane
    private lateinit var generalTab: JPanel
    private lateinit var alwaysShowShimejiChooserCheckBox: JCheckBox
    private lateinit var alwaysShowInformationScreenCheckBox: JCheckBox
    private lateinit var scalingLabel: JLabel
    private lateinit var scalingSlider: JSlider
    private lateinit var opacityLabel: JLabel
    private lateinit var opacitySlider: JSlider
    private lateinit var filterLabel: JLabel
    private lateinit var filterButtonGroup: ButtonGroup
    private lateinit var filterReplicate: JRadioButton
    private lateinit var filterSmooth: JRadioButton
    private lateinit var interactiveWindowsTab: JPanel
    private lateinit var interactiveWindowsTabsPane: JTabbedPane
    private lateinit var whitelistPane: JScrollPane
    private lateinit var blacklistPane: JScrollPane
    private lateinit var interactiveWindows: JList<String>
    private lateinit var interactiveWindowsBlacklist: JList<String>
    private lateinit var interactiveWindowsButtonsPanel: JPanel
    private lateinit var addButton: JButton
    private lateinit var removeButton: JButton
    private lateinit var themeTab: JPanel
    private lateinit var themeButtonGroup: ButtonGroup
    private lateinit var themeGtk: JRadioButton
    private lateinit var themeNimbus: JRadioButton
    private lateinit var themeMetal: JRadioButton
    private lateinit var aboutTab: JPanel
    private lateinit var aboutCenterPanel: JPanel
    private lateinit var aboutImage: JLabel
    private lateinit var shimelinuxLabel: JLabel
    private lateinit var versionLabel: JLabel
    private lateinit var buttonsPanel: JPanel
    private lateinit var doneButton: JButton
    private lateinit var cancelButton: JButton

    init {
        initComponents()

        val icon = ImageIO.read(this::class.java.getResourceAsStream("/icon.png"))
        setIconImage(icon)

        title = Main.instance.languageBundle.getString("Settings")
    }

    fun display() {
        isVisible = true
    }

    private fun initComponents() {
        this.contentPane = JPanel(BorderLayout())

        tabsPane = JTabbedPane()

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

            scaling = scalingSlider.value / 10.0
            isImageReloadRequired = true
        }

        opacityLabel = JLabel(Main.instance.languageBundle.getString("Opacity"))
        opacitySlider = JSlider()
        opacitySlider.majorTickSpacing = 10
        opacitySlider.minorTickSpacing = 5
        opacitySlider.paintTicks = true
        opacitySlider.snapToTicks = true
        opacitySlider.value = (opacity * 100.0).toInt()
        opacitySlider.addChangeListener {
            opacity = opacitySlider.value / 100.0
            isImageReloadRequired = true
        }

        filterLabel = JLabel(Main.instance.languageBundle.getString("FilterOptions"))

        filterReplicate = JRadioButton("Pixelated")
        filterReplicate.isSelected = filter == "Replicate"
        filterReplicate.addChangeListener {
            if (filterReplicate.isSelected) {
                filter = "Replicate"
            }
            isImageReloadRequired = true
        }

        filterSmooth = JRadioButton("Smooth")
        filterSmooth.isSelected = filter == "Smooth"
        filterSmooth.addChangeListener {
            if (filterSmooth.isSelected) {
                filter = "Smooth"
            }
            isImageReloadRequired = true
        }

        filterButtonGroup = ButtonGroup()
        filterButtonGroup.add(filterReplicate)
        filterButtonGroup.add(filterSmooth)

        generalTab = JPanel()
        generalTab.layout = BoxLayout(generalTab, BoxLayout.Y_AXIS)
        generalTab.add(alwaysShowShimejiChooserCheckBox)
        generalTab.add(alwaysShowInformationScreenCheckBox)
        generalTab.add(scalingLabel)
        generalTab.add(scalingSlider)
        generalTab.add(opacityLabel)
        generalTab.add(opacitySlider)
        generalTab.add(filterLabel)
        generalTab.add(filterReplicate)
        generalTab.add(filterSmooth)

        val whitelistModel = DefaultListModel<String>()
        for (title in Main.instance.properties.getProperty("InteractiveWindows", "").split('/')) {
            if (title.isNotBlank()) {
                whitelistModel.add(whitelistModel.size, title)
            }
        }

        interactiveWindows = JList(whitelistModel)
        whitelistPane = JScrollPane(interactiveWindows)

        val blacklistModel = DefaultListModel<String>()
        for (title in Main.instance.properties.getProperty("InteractiveWindowsBlacklist", "").split('/')) {
            if (title.isNotBlank()) {
                blacklistModel.add(blacklistModel.size, title)
            }
        }

        interactiveWindowsBlacklist = JList(blacklistModel)
        blacklistPane = JScrollPane(interactiveWindowsBlacklist)

        interactiveWindowsTabsPane = JTabbedPane()
        interactiveWindowsTabsPane.addTab(Main.instance.languageBundle.getString("Whitelist"), whitelistPane)
        interactiveWindowsTabsPane.addTab(Main.instance.languageBundle.getString("Blacklist"), blacklistPane)

        addButton = JButton(Main.instance.languageBundle.getString("Add"))
        addButton.preferredSize = Dimension(130, 26)
        addButton.addActionListener {
            val input = JOptionPane.showInputDialog(
                rootPane,
                Main.instance.languageBundle.getString("InteractiveWindowHintMessage"),
                Main.instance.languageBundle.getString(
                    if (interactiveWindowsTabsPane.selectedIndex == 0) {
                        "AddInteractiveWindow"
                    } else {
                        "BlacklistInteractiveWindow"
                    }
                ),
                JOptionPane.QUESTION_MESSAGE
            )

            if (!input.isNullOrBlank() && !input.contains("/")) {
                if (interactiveWindowsTabsPane.selectedIndex == 0) {
                    whitelistModel.add(whitelistModel.size, input.trim())
                    interactiveWindows.model = whitelistModel
                } else {
                    blacklistModel.add(blacklistModel.size, input.trim())
                    interactiveWindowsBlacklist.model = blacklistModel
                }
            }

            isInteractiveWindowReloadRequired = true
        }

        removeButton = JButton(Main.instance.languageBundle.getString("Remove"))
        removeButton.preferredSize = Dimension(130, 26)
        removeButton.addActionListener {
            if (interactiveWindowsTabsPane.selectedIndex == 0) {
                if (interactiveWindows.selectedIndex != -1) {
                    whitelistModel.remove(interactiveWindows.selectedIndex)
                    interactiveWindows.model = whitelistModel
                }
            } else {
                if (interactiveWindowsBlacklist.selectedIndex != -1) {
                    blacklistModel.remove(interactiveWindowsBlacklist.selectedIndex)
                    interactiveWindowsBlacklist.model = blacklistModel
                }
            }

            isInteractiveWindowReloadRequired = true
        }

        interactiveWindowsButtonsPanel = JPanel(FlowLayout())
        interactiveWindowsButtonsPanel.add(addButton)
        interactiveWindowsButtonsPanel.add(removeButton)

        interactiveWindowsTab = JPanel(BorderLayout())
        interactiveWindowsTab.add(interactiveWindowsTabsPane, BorderLayout.CENTER)
        interactiveWindowsTab.add(interactiveWindowsButtonsPanel, BorderLayout.SOUTH)

        themeGtk = JRadioButton("GTK")
        themeGtk.isSelected = theme == "GTK"
        themeGtk.addActionListener {
            if (themeGtk.isSelected) {
                theme = "GTK"
                refreshTheme()
            }
        }

        themeNimbus = JRadioButton("Nimbus")
        themeNimbus.isSelected = theme == "Nimbus"
        themeNimbus.addActionListener {
            if (themeNimbus.isSelected) {
                theme = "Nimbus"
                refreshTheme()
            }
        }

        themeMetal = JRadioButton("Metal")
        themeMetal.isSelected = theme == "Metal"
        themeMetal.addActionListener {
            if (themeMetal.isSelected) {
                theme = "Metal"
                refreshTheme()
            }
        }

        themeButtonGroup = ButtonGroup()
        themeButtonGroup.add(themeGtk)
        themeButtonGroup.add(themeNimbus)
        themeButtonGroup.add(themeMetal)

        themeTab = JPanel()
        themeTab.layout = BoxLayout(themeTab, BoxLayout.Y_AXIS)
        themeTab.add(themeGtk)
        themeTab.add(themeNimbus)
        themeTab.add(themeMetal)

        val image = ImageIO.read(this::class.java.getResourceAsStream("/icon.png"))
        aboutImage = JLabel()
        aboutImage.icon = ImageIcon(image.getScaledInstance(96, 96, Image.SCALE_DEFAULT))
        aboutImage.alignmentX = CENTER_ALIGNMENT

        shimelinuxLabel = JLabel("ShimeLinux")
        shimelinuxLabel.font = shimelinuxLabel.font.deriveFont(Font.BOLD, 24.0f)
        shimelinuxLabel.alignmentX = CENTER_ALIGNMENT
        versionLabel = JLabel("v0.1.0")
        versionLabel.alignmentX = CENTER_ALIGNMENT

        aboutCenterPanel = JPanel()
        aboutCenterPanel.layout = BoxLayout(aboutCenterPanel, BoxLayout.Y_AXIS)
        aboutCenterPanel.add(aboutImage)
        aboutCenterPanel.add(shimelinuxLabel)
        aboutCenterPanel.add(versionLabel)

        aboutTab = JPanel()
        aboutTab.add(aboutCenterPanel)

        tabsPane.addTab(Main.instance.languageBundle.getString("General"), generalTab)
        if (NativeFactory.instance.getEnvironment() is KdeEnvironment) {
            tabsPane.addTab(Main.instance.languageBundle.getString("InteractiveWindows"), interactiveWindowsTab)
        }
        tabsPane.addTab(Main.instance.languageBundle.getString("Theme"), themeTab)
        tabsPane.addTab(Main.instance.languageBundle.getString("About"), aboutTab)

        doneButton = JButton(Main.instance.languageBundle.getString("Done"))
        doneButton.addActionListener {
            Main.instance.properties.setProperty("AlwaysShowShimejiChooser", alwaysShowShimejiChooser.toString())
            Main.instance.properties.setProperty("AlwaysShowInformationScreen", alwaysShowInformationScreen.toString())
            Main.instance.properties.setProperty("Scaling", scaling.toString())
            Main.instance.properties.setProperty("Opacity", opacity.toString())
            Main.instance.properties.setProperty("Filter", filter)
            Main.instance.properties.setProperty("Theme", theme)

            val whitelistBuilder = StringBuilder()
            for (title in whitelistModel.elements()) {
                if (whitelistBuilder.isNotBlank()) {
                    whitelistBuilder.append("/")
                }
                whitelistBuilder.append(title)
            }

            val blacklistBuilder = StringBuilder()
            for (title in blacklistModel.elements()) {
                if (blacklistBuilder.isNotBlank()) {
                    blacklistBuilder.append("/")
                }
                blacklistBuilder.append(title)
            }

            Main.instance.properties.setProperty("InteractiveWindows", whitelistBuilder.toString())
            Main.instance.properties.setProperty("InteractiveWindowsBlacklist", blacklistBuilder.toString())

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

        add(tabsPane, BorderLayout.CENTER)
        add(buttonsPanel, BorderLayout.SOUTH)

        pack()
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
        } catch (_: Exception) {
        }
    }
}
