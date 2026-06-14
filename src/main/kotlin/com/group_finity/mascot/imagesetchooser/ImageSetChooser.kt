/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.imagesetchooser

import com.group_finity.mascot.Main
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
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.outputStream

class ImageSetChooser(parent: Frame, modal: Boolean) : JDialog(parent, modal) {
    private val configPath = Main.getPath("conf", "settings.properties")
    private val topDir = Main.getPath("img")
    private var imageSets = ArrayList<String>()
    private var selectAllSets = false
    private var cancelled = true

    private lateinit var topLabelsPanel: JPanel
    private lateinit var allLabelsPanel: JPanel
    private lateinit var label: JLabel
    private lateinit var clearAllLabel: JLabel
    private lateinit var slashLabel: JLabel
    private lateinit var selectAllLabel: JLabel
    private lateinit var list1: ShimejiList
    private lateinit var list2: ShimejiList
    private lateinit var scrollPane: JScrollPane
    private lateinit var bottonButtonsPanel: JPanel
    private lateinit var addShimejiButton: JButton
    private lateinit var useSelectedButton: JButton
    private lateinit var useAllButton: JButton
    private lateinit var cancelButton: JButton

    init {
        initComponents()

        val icon = ImageIO.read(this::class.java.getResourceAsStream("/icon.png"))
        setIconImage(icon)

        title = Main.instance.languageBundle.getString("ShimejiImageSetChooser")

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

            // Implement information

            var imageFile: String
            var caption: String
            try {
                // Implement information

                throw Exception("Ignore me!")
            } catch (_: Exception) {
                imageFile = topDir.resolve(Path(imageSet, "shime1.png")).toString()
                caption = imageSet
            }

            if (onList1) {
                onList1 = false
                list1.addShimeji(
                    imageSet,
                    "./" + actionsPath.subpath(4, actionsPath.nameCount).toString(),
                    "./" + behaviorsPath.subpath(4, behaviorsPath.nameCount).toString(),
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
                    "./" + actionsPath.subpath(4, actionsPath.nameCount).toString(),
                    "./" + behaviorsPath.subpath(4, behaviorsPath.nameCount).toString(),
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

    private fun initComponents() {
        defaultCloseOperation = DISPOSE_ON_CLOSE
        minimumSize = Dimension(670, 495)

        this.contentPane = JPanel(BorderLayout())

        list1 = ShimejiList(DefaultListModel<ImageSetChooserPanel>())
        list2 = ShimejiList(DefaultListModel<ImageSetChooserPanel>())
        val listPanel = JPanel(GridLayout(1, 2, 0, 0))
        listPanel.add(list1)
        listPanel.add(list2)

        scrollPane = JScrollPane(listPanel)
        scrollPane.preferredSize = Dimension(518, 100)

        label = JLabel(Main.instance.languageBundle.getString("SelectImageSetsToUse"))

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

        slashLabel = JLabel(" / ")

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

        allLabelsPanel = JPanel(FlowLayout())
        allLabelsPanel.layout = BoxLayout(allLabelsPanel, BoxLayout.X_AXIS)
        allLabelsPanel.add(clearAllLabel)
        allLabelsPanel.add(slashLabel)
        allLabelsPanel.add(selectAllLabel)

        topLabelsPanel = JPanel(BorderLayout())
        topLabelsPanel.add(label, BorderLayout.WEST)
        topLabelsPanel.add(this@ImageSetChooser.allLabelsPanel, BorderLayout.EAST)

        addShimejiButton = JButton("+")
        addShimejiButton.addActionListener {
            val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
            var failed = false
            try {
                if (desktop != null) {
                    desktop.open(Main.getPath("img").toFile())
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

        bottonButtonsPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        bottonButtonsPanel.add(addShimejiButton)
        bottonButtonsPanel.add(useSelectedButton)
        bottonButtonsPanel.add(useAllButton)
        bottonButtonsPanel.add(cancelButton)

        add(topLabelsPanel, BorderLayout.NORTH)
        add(scrollPane, BorderLayout.CENTER)
        add(bottonButtonsPanel, BorderLayout.SOUTH)
    }

    private fun readConfigFile(): MutableList<String> {
        val activeImageSets = mutableListOf<String>()
        activeImageSets.addAll(Main.instance.properties.getProperty("ActiveShimeji", "").split('/'))
        selectAllSets = activeImageSets[0].trim().isEmpty()
        return activeImageSets
    }

    private fun updateConfigFile() {
        try {
            val value = StringBuilder()
            for (imageSet in imageSets) {
                if (!value.isEmpty()) {
                    value.append('/')
                }
                value.append(imageSet)
            }

            configPath.outputStream().use { stream ->
                Main.instance.properties.setProperty("ActiveShimeji", value.toString())
                Main.instance.properties.store(stream, "ShimeLinux Configuration Options")
            }
        } catch (_: Exception) {
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
