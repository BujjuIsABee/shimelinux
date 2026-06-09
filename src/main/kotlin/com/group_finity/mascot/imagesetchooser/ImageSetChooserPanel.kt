/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.imagesetchooser

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.Image
import java.io.File
import javax.imageio.ImageIO
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.ImageIcon
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.UIManager

class ImageSetChooserPanel : JPanel {
    private lateinit var checkbox: JCheckBox
    private lateinit var image: JLabel
    private lateinit var caption: JLabel
    private lateinit var actionsFile: JLabel
    private lateinit var behaviorsFile: JLabel

    var imageSetName: String? = null
        private set

    constructor() {
        initComponents()
    }

    constructor(imageSet: String, actions: String, behaviors: String, imageLocation: String, captionText: String) {
        initComponents()

        imageSetName = imageSet
        caption.text = captionText
        caption.font = caption.font.deriveFont(Font.BOLD)
        actionsFile.text = actions
        behaviorsFile.text = behaviors
        try {
            val icon = ImageIO.read(File(imageLocation))
            image.icon = ImageIcon(icon.getScaledInstance(60, 60, Image.SCALE_DEFAULT))
        } catch (_: Exception) {
        }
    }

    fun setCheckbox(value: Boolean) {
        checkbox.isSelected = value
    }

    private fun initComponents() {
        minimumSize = Dimension(248, 80)
        preferredSize = Dimension(248, 80)
        layout = BoxLayout(this, BoxLayout.X_AXIS)

        checkbox = JCheckBox()
        image = JLabel()
        caption = JLabel()
        actionsFile = JLabel()
        behaviorsFile = JLabel()

        image.border = BorderFactory.createLineBorder(UIManager.getColor("textHighlight"))

        val textPanel = JPanel()
        textPanel.layout = BoxLayout(textPanel, BoxLayout.Y_AXIS)
        textPanel.add(caption)
        textPanel.add(actionsFile)
        textPanel.add(behaviorsFile)

        add(checkbox)
        add(image)
        add(Box.createRigidArea(Dimension(4, 0)))
        add(textPanel)
    }
}
