/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.imagesetchooser

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

class ImageSetChooserPanel(
    imageSet: String,
    actions: String,
    behaviors: String,
    imageLocation: String,
    captionText: String
) : JPanel() {
    private val checkbox: JCheckBox
    private val image: JLabel
    private val caption: JLabel

    var imageSetName: String? = null
        private set

    init {
        imageSetName = imageSet
        minimumSize = Dimension(248, 80)
        preferredSize = Dimension(248, 80)
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        border = BorderFactory.createLineBorder(UIManager.getColor("textHighlight"))

        checkbox = JCheckBox()

        image = JLabel()
        runCatching {
            val icon = ImageIO.read(File(imageLocation))
            image.icon = ImageIcon(icon.getScaledInstance(60, 60, Image.SCALE_DEFAULT))
        }

        caption = JLabel(captionText)
        caption.font = caption.font.deriveFont(Font.BOLD)

        val textPanel = JPanel()
        textPanel.layout = BoxLayout(textPanel, BoxLayout.Y_AXIS)
        textPanel.add(caption)
        textPanel.add(JLabel(actions))
        textPanel.add(JLabel(behaviors))

        add(checkbox)
        add(image)
        add(Box.createHorizontalStrut(4))
        add(textPanel)
    }

    fun setCheckbox(value: Boolean) {
        checkbox.isSelected = value
    }
}
