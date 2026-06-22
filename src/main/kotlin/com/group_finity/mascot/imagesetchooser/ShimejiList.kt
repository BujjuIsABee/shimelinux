/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.imagesetchooser

import java.awt.Component
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.ListCellRenderer

class ShimejiList(private val model: DefaultListModel<ImageSetChooserPanel>) :
    JList<ImageSetChooserPanel>(model) {
    init {
        cellRenderer = CellRenderer()
    }

    fun addShimeji(
        imageSet: String,
        actions: String,
        behaviors: String,
        imageLocation: String,
        captionText: String
    ) {
        model.add(
            model.size, ImageSetChooserPanel(
                imageSet,
                actions,
                behaviors,
                imageLocation,
                captionText
            )
        )
    }

    class CellRenderer : ListCellRenderer<ImageSetChooserPanel> {
        override fun getListCellRendererComponent(
            list: JList<out ImageSetChooserPanel>,
            component: ImageSetChooserPanel,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ) = component.also { it.setCheckbox(isSelected) }
    }
}
