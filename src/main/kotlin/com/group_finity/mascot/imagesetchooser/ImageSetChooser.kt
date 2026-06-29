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

package com.group_finity.mascot.imagesetchooser

import com.group_finity.mascot.Main
import com.group_finity.mascot.config.Configuration
import com.group_finity.mascot.config.Entry
import com.group_finity.mascot.getPath
import com.group_finity.mascot.getProperty
import com.group_finity.mascot.loadResource
import com.group_finity.mascot.localize
import com.group_finity.mascot.setProperty
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

class ImageSetChooser(parent: Frame?, modal: Boolean) : JDialog(parent, modal) {
    private val headerPanel: JPanel
    private val labelsPanel: JPanel
    private val clearAllLabel: JLabel
    private val selectAllLabel: JLabel
    private val listScrollPane: JScrollPane
    private val listPanel: JPanel
    private val list1: ShimejiList
    private val list2: ShimejiList
    private val footerPanel: JPanel
    private val moreButton: JButton
    private val useSelectedButton: JButton
    private val useAllButton: JButton
    private val cancelButton: JButton

    private val confPath = getPath("conf", "settings.properties")
    private val topDir = getPath("img")
    private var imageSets = ArrayList<String>()
    private var selectAllSets = false
    private var cancelled = true

    init {
        val icon = loadResource("/img/icon.png").use { ImageIO.read(it) }
        setIconImage(icon)
        title = "ShimejiImageSetChooser".localize()
        minimumSize = Dimension(670, 495)
        contentPane.layout = BorderLayout()
        defaultCloseOperation = DISPOSE_ON_CLOSE

        list1 = ShimejiList(DefaultListModel<ImageSetChooserPanel>())
        list2 = ShimejiList(DefaultListModel<ImageSetChooserPanel>())

        listPanel = JPanel(GridLayout(1, 2, 0, 0))
        listPanel.add(list1)
        listPanel.add(list2)

        listScrollPane = JScrollPane(listPanel)
        listScrollPane.preferredSize = Dimension(518, 100)
        listScrollPane.verticalScrollBar.unitIncrement = 10

        clearAllLabel = JLabel("<html><u>" + "ClearAll".localize() + "</u></html>")
        clearAllLabel.cursor = Cursor(Cursor.HAND_CURSOR)
        clearAllLabel.foreground = UIManager.getColor("textHighlight")
        clearAllLabel.font = clearAllLabel.font.deriveFont(Font.BOLD)
        clearAllLabel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                list1.clearSelection()
                list2.clearSelection()
            }
        })

        selectAllLabel = JLabel("<html><u>" + "SelectAll".localize() + "</u></html>")
        selectAllLabel.cursor = Cursor(Cursor.HAND_CURSOR)
        selectAllLabel.foreground = UIManager.getColor("textHighlight")
        selectAllLabel.font = selectAllLabel.font.deriveFont(Font.BOLD)
        selectAllLabel.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                list1.setSelectionInterval(0, list1.model.size - 1)
                list2.setSelectionInterval(0, list2.model.size - 1)
            }
        })

        labelsPanel = JPanel(FlowLayout())
        labelsPanel.layout = BoxLayout(labelsPanel, BoxLayout.X_AXIS)
        labelsPanel.add(clearAllLabel)
        labelsPanel.add(JLabel(" / "))
        labelsPanel.add(selectAllLabel)

        headerPanel = JPanel(BorderLayout())
        headerPanel.add(JLabel("SelectImageSetsToUse".localize()), BorderLayout.WEST)
        headerPanel.add(labelsPanel, BorderLayout.EAST)

        moreButton = JButton("More".localize())
        moreButton.addActionListener { handleMore() }

        useSelectedButton = JButton("UseSelected".localize())
        useSelectedButton.addActionListener { handleUseSelected() }

        useAllButton = JButton("UseAll".localize())
        useAllButton.addActionListener {
            cancelled = false
            dispose()
        }

        cancelButton = JButton("Cancel".localize())
        cancelButton.addActionListener { dispose() }

        footerPanel = JPanel(FlowLayout(FlowLayout.CENTER))
        footerPanel.add(moreButton)
        footerPanel.add(useSelectedButton)
        footerPanel.add(useAllButton)
        footerPanel.add(cancelButton)

        val children = topDir.toFile().listFiles()
            .filter { it.isDirectory && !it.absolutePath.contains("unused", true) }
            .map { it.name }

        val activeImageSets = readConfigFile()
        var onList1 = true
        var row = 0
        val si1 = mutableListOf<Int>()
        val si2 = mutableListOf<Int>()

        for (imageSet in children) {
            // Determine actions file
            val actionsNames = listOf(
                "actions.xml",
                "\u52D5\u4F5C.xml",
                "\u00D5\u00EF\u00F2\u00F5\u00A2\u00A3.xml",
                "\u00A6-\u00BA@.xml",
                "\u00F4\u00AB\u00EC\u00FD.xml",
                "one.xml",
                "1.xml"
            )

            var filePath = getPath("conf")
            var actionsPath = filePath.resolve("\u52D5\u4F5C.xml").takeIf { it.exists() } ?: filePath.resolve("actions.xml")

            filePath = getPath("conf", imageSet)
            actionsPath = actionsNames.map { filePath.resolve(it) }.firstOrNull { it.exists() } ?: actionsPath

            filePath = getPath("img", imageSet, "conf")
            actionsPath = actionsNames.map { filePath.resolve(it) }.firstOrNull { it.exists() } ?: actionsPath

            val actionsFile = "./${actionsPath.subpath(4, actionsPath.nameCount)}"

            // Determine behaviors file
            val behaviorsNames = listOf(
                "behaviors.xml",
                "\u884C\u52D5.xml",
                "\u00DE\u00ED\u00EE\u00D5\u00EF\u00F2.xml",
                "\u00AA\u00B5\u00A6-.xml",
                "\u00ECs\u00F4\u00AB.xml",
                "two.xml",
                "2.xml"
            )

            filePath = getPath("conf")
            var behaviorsPath = filePath.resolve("\u884C\u52D5.xml").takeIf { it.exists() } ?: filePath.resolve("behaviors.xml")

            filePath = getPath("conf", imageSet)
            behaviorsPath = behaviorsNames.map { filePath.resolve(it) }.firstOrNull { it.exists() } ?: behaviorsPath

            filePath = getPath("img", imageSet, "conf")
            behaviorsPath = behaviorsNames.map { filePath.resolve(it) }.firstOrNull { it.exists() } ?: behaviorsPath

            val behaviorsFile = "./${behaviorsPath.subpath(4, behaviorsPath.nameCount)}"

            // Determine information file
            filePath = getPath("conf")
            var infoPath = filePath.resolve("info.xml")

            filePath = getPath("conf", imageSet)
            infoPath = filePath.resolve("info.xml").takeIf { it.exists() } ?: infoPath

            filePath = getPath("img", imageSet, "conf")
            infoPath = filePath.resolve("info.xml").takeIf { it.exists() } ?: infoPath

            var imageFile = topDir.resolve(Path(imageSet, "shime1.png")).toString()
            var caption = imageSet

            try {
                val configuration = Configuration()

                if (infoPath.exists()) {
                    val information = infoPath.inputStream().use { DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it) }
                    configuration.load(Entry(information.documentElement), imageSet)
                }

                configuration.getInformation(configuration.schema.getString("Name"))?.let {
                    caption = it
                }
                configuration.getInformation(configuration.schema.getString("PreviewImage"))?.let {
                    imageFile = topDir.resolve(Path(imageSet, it)).toString()
                }
            } catch (_: Exception) {
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

        add(headerPanel, BorderLayout.NORTH)
        add(listScrollPane, BorderLayout.CENTER)
        add(footerPanel, BorderLayout.SOUTH)
        setLocationRelativeTo(null)
    }

    fun display(): MutableList<String>? {
        isVisible = true
        return if (cancelled) null else imageSets
    }

    private fun readConfigFile(): MutableList<String> {
        val activeImageSets = mutableListOf<String>()
        activeImageSets.addAll(getProperty("ActiveShimeji", "").split('/'))
        selectAllSets = activeImageSets[0].trim().isEmpty()
        return activeImageSets
    }

    private fun updateConfigFile() {
        runCatching {
            setProperty(
                "ActiveShimeji",
                imageSets
                    .toString()
                    .replace("[", "")
                    .replace("]", "")
                    .replace(", ", "/")
            )
            confPath.outputStream().use { Main.instance.properties.store(it, "Configuration Options") }
        }
    }

    private fun handleMore() {
        val desktop = if (Desktop.isDesktopSupported()) Desktop.getDesktop() else null
        try {
            checkNotNull(desktop).open(getPath("img").toFile())
            cancelled = true
            dispose()
        } catch (_: Exception) {
            JOptionPane.showMessageDialog(
                this@ImageSetChooser,
                getPath("img").toString(),
                "Add Shimeji Here:",
                JOptionPane.PLAIN_MESSAGE
            )
        }
    }

    private fun handleUseSelected() {
        imageSets.clear()

        for (selection in list1.selectedValuesList) {
            if (selection is ImageSetChooserPanel) {
                imageSets.add(checkNotNull(selection.imageSet))
            }
        }

        for (selection in list2.selectedValuesList) {
            if (selection is ImageSetChooserPanel) {
                imageSets.add(checkNotNull(selection.imageSet))
            }
        }

        updateConfigFile()
        cancelled = false
        dispose()
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
