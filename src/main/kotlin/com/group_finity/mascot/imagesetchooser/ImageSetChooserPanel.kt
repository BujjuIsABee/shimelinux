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
    val imageSet: String,
    actions: String,
    behaviors: String,
    imageLocation: String,
    captionText: String
) : JPanel() {
    private val textPanel: JPanel
    private val imageLabel: JLabel
    private val captionLabel: JLabel
    private val checkbox: JCheckBox = JCheckBox()

    init {
        minimumSize = Dimension(248, 80)
        preferredSize = Dimension(248, 80)
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        border = BorderFactory.createLineBorder(UIManager.getColor("Table.gridColor"))

        imageLabel = JLabel()
        imageLabel.icon = runCatching {
            ImageIO.read(File(imageLocation))
        }.getOrNull()?.let {
            ImageIcon(it.getScaledInstance(60, 60, Image.SCALE_DEFAULT))
        }

        captionLabel = JLabel(captionText)
        captionLabel.font = captionLabel.font.deriveFont(Font.BOLD)

        textPanel = JPanel()
        textPanel.layout = BoxLayout(textPanel, BoxLayout.Y_AXIS)
        textPanel.add(captionLabel)
        textPanel.add(JLabel(actions))
        textPanel.add(JLabel(behaviors))

        add(checkbox)
        add(imageLabel)
        add(Box.createHorizontalStrut(4))
        add(textPanel)
    }

    fun setCheckbox(value: Boolean) {
        checkbox.isSelected = value
    }
}
