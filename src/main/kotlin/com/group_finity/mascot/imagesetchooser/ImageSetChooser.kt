/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.imagesetchooser

import com.group_finity.mascot.Main
import com.group_finity.mascot.config.Configuration
import com.group_finity.mascot.config.Entry
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Desktop
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.Frame
import java.awt.GridLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.imageio.ImageIO
import javax.swing.BoxLayout
import javax.swing.DefaultListModel
import javax.swing.DefaultListSelectionModel
import javax.swing.JButton
import javax.swing.JDialog
import javax.swing.JLabel
import javax.swing.JList
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.UIManager
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class ImageSetChooser(parent: Frame, modal: Boolean) : JDialog(parent, modal) {
    private val configPath = Main.getPath("conf", "settings.properties")
    private val topDir = Main.getPath("img")
    private var imageSets = ArrayList<String>()
    private var selectAllSets = false
    private var cancelled = true

    private var topLabelsPanel: JPanel
    private var clearSelectLabelsPanel: JPanel
    private var bottomButtonsPanel: JPanel
    private var selectImageSetsLabel: JLabel
    private var clearAllLabel: JLabel
    private var selectAllLabel: JLabel
    private var list1: ShimejiList
    private var list2: ShimejiList
    private var scrollPane: JScrollPane
    private var addButton: JButton
    private var useSelectedButton: JButton
    private var useAllButton: JButton
    private var cancelButton: JButton

    init {
        val icon = this::class.java.getResourceAsStream("/img/icon.png").use { ImageIO.read(it) }
        setIconImage(icon)

        title = Main.instance.languageBundle.getString("ShimejiImageSetChooser")
        minimumSize = Dimension(670, 495)
        contentPane = JPanel(BorderLayout())
        defaultCloseOperation = DISPOSE_ON_CLOSE

        list1 = ShimejiList(DefaultListModel<ImageSetChooserPanel>())
        list2 = ShimejiList(DefaultListModel<ImageSetChooserPanel>())

        val listPanel = JPanel(GridLayout(1, 2, 0, 0))
        listPanel.add(list1)
        listPanel.add(list2)

        scrollPane = JScrollPane(listPanel)
        scrollPane.preferredSize = Dimension(518, 100)

        selectImageSetsLabel = JLabel(Main.instance.languageBundle.getString("SelectImageSetsToUse"))

        clearAllLabel = JLabel("<html><u>" + Main.instance.languageBundle.getString("ClearAll") + "</u></html>")
        clearAllLabel.cursor = Cursor(Cursor.HAND_CURSOR)
        clearAllLabel.foreground = UIManager.getColor("textHighlight")
        clearAllLabel.font = clearAllLabel.font.deriveFont(Font.BOLD)
        clearAllLabel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                list1.clearSelection()
                list2.clearSelection()
            }
        })

        selectAllLabel = JLabel("<html><u>" + Main.instance.languageBundle.getString("SelectAll") + "</u></html>")
        selectAllLabel.cursor = Cursor(Cursor.HAND_CURSOR)
        selectAllLabel.foreground = UIManager.getColor("textHighlight")
        selectAllLabel.font = selectAllLabel.font.deriveFont(Font.BOLD)
        selectAllLabel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                list1.setSelectionInterval(0, list1.model.size - 1)
                list2.setSelectionInterval(0, list2.model.size - 1)
            }
        })

        clearSelectLabelsPanel = JPanel(FlowLayout())
        clearSelectLabelsPanel.layout = BoxLayout(clearSelectLabelsPanel, BoxLayout.X_AXIS)
        clearSelectLabelsPanel.add(clearAllLabel)
        clearSelectLabelsPanel.add(JLabel(" / "))
        clearSelectLabelsPanel.add(selectAllLabel)

        topLabelsPanel = JPanel(BorderLayout())
        topLabelsPanel.add(selectImageSetsLabel, BorderLayout.WEST)
        topLabelsPanel.add(clearSelectLabelsPanel, BorderLayout.EAST)

        addButton = JButton(Main.instance.languageBundle.getString("Add"))
        addButton.addActionListener {
            val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
            var failed = false
            try {
                if (desktop != null) {
                    desktop.open(Main.getPath("img").toFile())
                    cancelled = true
                    dispose()
                } else {
                    failed = true
                }
            } catch (_: Exception) {
                failed = true
            }

            if (failed) {
                JOptionPane.showMessageDialog(
                    this@ImageSetChooser,
                    Main.getPath("img").toString(),
                    "Add Shimeji Here:",
                    JOptionPane.PLAIN_MESSAGE
                )
            }
        }

        useSelectedButton = JButton(Main.instance.languageBundle.getString("UseSelected"))
        useSelectedButton.addActionListener {
            imageSets.clear()

            for (selection in list1.selectedValuesList) {
                if (selection is ImageSetChooserPanel) {
                    imageSets.add(checkNotNull(selection.imageSetName))
                }
            }

            for (selection in list2.selectedValuesList) {
                if (selection is ImageSetChooserPanel) {
                    imageSets.add(checkNotNull(selection.imageSetName))
                }
            }

            updateConfigFile()
            cancelled = false
            dispose()
        }

        useAllButton = JButton(Main.instance.languageBundle.getString("UseAll"))
        useAllButton.addActionListener {
            cancelled = false
            dispose()
        }

        cancelButton = JButton(Main.instance.languageBundle.getString("Cancel"))
        cancelButton.addActionListener {
            dispose()
        }

        bottomButtonsPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        bottomButtonsPanel.add(addButton)
        bottomButtonsPanel.add(useSelectedButton)
        bottomButtonsPanel.add(useAllButton)
        bottomButtonsPanel.add(cancelButton)

        add(topLabelsPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
        add(bottomButtonsPanel, BorderLayout.SOUTH)

        val activeImageSets = readConfigFile()

        val children = topDir.toFile().listFiles().filter {
            it.isDirectory &&
            !it.absolutePath.contains("unused", true)
        }.map { it.name }

        var onList1 = true
        var row = 0
        val si1 = mutableListOf<Int>()
        val si2 = mutableListOf<Int>()

        for (imageSet in children) {
            // Determine actions file
            var filePath = Main.getPath("conf")
            var actionsPath = filePath.resolve("actions.xml")
            if (filePath.resolve("\u52D5\u4F5C.xml").exists()) {
                actionsPath = filePath.resolve("\u52D5\u4F5C.xml")
            }

            filePath = Main.getPath("conf", imageSet)
            if (filePath.resolve("actions.xml").exists()) {
                actionsPath = filePath.resolve("actions.xml")
            } else if (filePath.resolve("\u52D5\u4F5C.xml").exists()) {
                actionsPath = filePath.resolve("\u52D5\u4F5C.xml")
            } else if (filePath.resolve("\u00D5\u00EF\u00F2\u00F5\u00A2\u00A3.xml").exists()) {
                actionsPath = filePath.resolve("\u00D5\u00EF\u00F2\u00F5\u00A2\u00A3.xml")
            } else if (filePath.resolve("\u00A6-\u00BA@.xml").exists()) {
                actionsPath = filePath.resolve("\u00A6-\u00BA@.xml")
            } else if (filePath.resolve("\u00F4\u00AB\u00EC\u00FD.xml").exists()) {
                actionsPath = filePath.resolve("\u00F4\u00AB\u00EC\u00FD.xml")
            } else if (filePath.resolve("one.xml").exists()) {
                actionsPath = filePath.resolve("one.xml")
            } else if (filePath.resolve("1.xml").exists()) {
                actionsPath = filePath.resolve("1.xml")
            }

            filePath = Main.getPath("img", imageSet, "conf")
            if (filePath.resolve("actions.xml").exists()) {
                actionsPath = filePath.resolve("actions.xml")
            } else if (filePath.resolve("\u52D5\u4F5C.xml").exists()) {
                actionsPath = filePath.resolve("\u52D5\u4F5C.xml")
            } else if (filePath.resolve("\u00D5\u00EF\u00F2\u00F5\u00A2\u00A3.xml").exists()) {
                actionsPath = filePath.resolve("\u00D5\u00EF\u00F2\u00F5\u00A2\u00A3.xml")
            } else if (filePath.resolve("\u00A6-\u00BA@.xml").exists()) {
                actionsPath = filePath.resolve("\u00A6-\u00BA@.xml")
            } else if (filePath.resolve("\u00F4\u00AB\u00EC\u00FD.xml").exists()) {
                actionsPath = filePath.resolve("\u00F4\u00AB\u00EC\u00FD.xml")
            } else if (filePath.resolve("one.xml").exists()) {
                actionsPath = filePath.resolve("one.xml")
            } else if (filePath.resolve("1.xml").exists()) {
                actionsPath = filePath.resolve("1.xml")
            }

            val actionsFile = "./${actionsPath.subpath(4, actionsPath.nameCount)}"

            // Determine behaviors file
            filePath = Main.getPath("conf")
            var behaviorsPath = filePath.resolve("behaviors.xml")
            if (filePath.resolve("\u884C\u52D5.xml").exists()) {
                behaviorsPath = filePath.resolve("\u884C\u52D5.xml")
            }

            filePath = Main.getPath("conf", imageSet)
            if (filePath.resolve("behaviors.xml").exists()) {
                behaviorsPath = filePath.resolve("behaviors.xml")
            } else if (filePath.resolve("behavior.xml").exists()) {
                behaviorsPath = filePath.resolve("behavior.xml")
            } else if (filePath.resolve("\u884C\u52D5.xml").exists()) {
                behaviorsPath = filePath.resolve("\u884C\u52D5.xml")
            } else if (filePath.resolve("\u00DE\u00ED\u00EE\u00D5\u00EF\u00F2.xml").exists()) {
                behaviorsPath = filePath.resolve("\u00DE\u00ED\u00EE\u00D5\u00EF\u00F2.xml")
            } else if (filePath.resolve("\u00AA\u00B5\u00A6-.xml").exists()) {
                behaviorsPath = filePath.resolve("\u00AA\u00B5\u00A6-.xml")
            } else if (filePath.resolve("\u00ECs\u00F4\u00AB.xml").exists()) {
                behaviorsPath = filePath.resolve("\u00ECs\u00F4\u00AB.xml")
            } else if (filePath.resolve("two.xml").exists()) {
                behaviorsPath = filePath.resolve("two.xml")
            } else if (filePath.resolve("2.xml").exists()) {
                behaviorsPath = filePath.resolve("2.xml")
            }

            filePath = Main.getPath("img", imageSet, "conf")
            if (filePath.resolve("behaviors.xml").exists()) {
                behaviorsPath = filePath.resolve("behaviors.xml")
            } else if (filePath.resolve("behavior.xml").exists()) {
                behaviorsPath = filePath.resolve("behavior.xml")
            } else if (filePath.resolve("\u884C\u52D5.xml").exists()) {
                behaviorsPath = filePath.resolve("\u884C\u52D5.xml")
            } else if (filePath.resolve("\u00DE\u00ED\u00EE\u00D5\u00EF\u00F2.xml").exists()) {
                behaviorsPath = filePath.resolve("\u00DE\u00ED\u00EE\u00D5\u00EF\u00F2.xml")
            } else if (filePath.resolve("\u00AA\u00B5\u00A6-.xml").exists()) {
                behaviorsPath = filePath.resolve("\u00AA\u00B5\u00A6-.xml")
            } else if (filePath.resolve("\u00ECs\u00F4\u00AB.xml").exists()) {
                behaviorsPath = filePath.resolve("\u00ECs\u00F4\u00AB.xml")
            } else if (filePath.resolve("two.xml").exists()) {
                behaviorsPath = filePath.resolve("two.xml")
            } else if (filePath.resolve("2.xml").exists()) {
                behaviorsPath = filePath.resolve("2.xml")
            }

            val behaviorsFile = "./${behaviorsPath.subpath(4, behaviorsPath.nameCount)}"

            // Determine information file
            filePath = Main.getPath("conf")
            var infoPath = filePath.resolve("info.xml")

            filePath = Main.getPath("conf", imageSet)
            if (filePath.resolve("info.xml").exists()) {
                infoPath = filePath.resolve("info.xml")
            }

            filePath = Main.getPath("img", imageSet, "conf")
            if (filePath.resolve("info.xml").exists()) {
                infoPath = filePath.resolve("info.xml")
            }

            var imageFile = topDir.resolve(Path(imageSet, "shime1.png")).toString()
            var caption = imageSet

            runCatching {
                val config = Configuration()

                if (infoPath.exists()) {
                    val information = infoPath.inputStream().use {
                        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it)
                    }

                    config.load(Entry(information.documentElement), imageSet)
                }

                config.getInformation(config.schema.getString("Name"))?.let {
                    caption = it
                }
                config.getInformation(config.schema.getString("PreviewImage"))?.let {
                    imageFile = topDir.resolve(Path(imageSet, it)).toString()
                }
            }

            if (onList1) {
                onList1 = false
                list1.addShimeji(
                    imageSet,
                    actionsFile,
                    behaviorsFile,
                    imageFile,
                    caption
                )

                if (activeImageSets.contains(imageSet) || selectAllSets) {
                    si1.add(row)
                }
            } else {
                onList1 = true
                list2.addShimeji(
                    imageSet,
                    actionsFile,
                    behaviorsFile,
                    imageFile,
                    caption
                )

                if (activeImageSets.contains(imageSet) || selectAllSets) {
                    si2.add(row)
                }
                row++
            }
            imageSets.add(imageSet)
        }

        setUpList(list1)
        list1.selectedIndices = si1.toIntArray()

        setUpList(list2)
        list2.selectedIndices = si2.toIntArray()
    }

    fun display(): MutableList<String>? {
        isVisible = true
        return if (cancelled) null else imageSets
    }

    private fun readConfigFile(): MutableList<String> {
        val activeImageSets = mutableListOf<String>()
        activeImageSets.addAll(Main.instance.properties.getProperty("ActiveShimeji", "").split('/'))
        selectAllSets = activeImageSets[0].trim().isEmpty()
        return activeImageSets
    }

    private fun updateConfigFile() {
        runCatching {
            val activeShimeji = imageSets
                .toString()
                .replace("[", "")
                .replace("]", "")
                .replace(", ", "/")

            Main.instance.properties.setProperty("ActiveShimeji", activeShimeji)

            configPath.outputStream().use {
                Main.instance.properties.store(it, "ShimeLinux Configuration Options")
            }
        }
    }

    private fun setUpList(list: JList<ImageSetChooserPanel>) {
        list.selectionModel = object : DefaultListSelectionModel() {
            override fun setSelectionInterval(index0: Int, index1: Int) {
                if (isSelectedIndex(index0)) {
                    super.removeSelectionInterval(index0, index1)
                } else {
                    super.addSelectionInterval(index0, index1)
                }
            }
        }
    }
}
