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

package io.github.bujjuisabee.shimelinux.wayland

import com.group_finity.mascot.NativeFactory
import java.awt.Component
import java.awt.Point
import java.awt.Rectangle
import java.awt.image.BufferedImage
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.MenuElement
import javax.swing.MenuSelectionManager
import javax.swing.Popup
import javax.swing.SwingUtilities
import javax.swing.UIManager

/**
 * Displays a popup with a [WaylandLayer]
 *
 * @author Bujju
 */
class WaylandPopup(
    private val owner: Component?,
    private val contents: Component,
    private val x: Int,
    private val y: Int,
) : Popup(owner, contents, x, y) {
    private val layer = WaylandLayer(this, false)

    private var previousTarget: Component? = null
    private var submenu: WaylandPopup? = null
    private var parent: JMenu? = null

    override fun show() {
        contents.size = contents.preferredSize
        contents.doLayout()
        layer.setBounds(x, y, contents.width, contents.height)
        updateImage()
    }

    override fun hide() {
        super.hide()
        closeSubmenu()
        layer.dispose()
    }

    @Suppress("unused")
    fun updateCursor(
        leftPressed: Boolean,
        rightPressed: Boolean,
        leftReleased: Boolean,
        rightReleased: Boolean,
        positionX: Int,
        positionY: Int
    ) {
        val target = SwingUtilities.getDeepestComponentAt(contents, positionX, positionY) ?: contents
        val targetPosition = SwingUtilities.convertPoint(contents, positionX, positionY, target)

        if (previousTarget != target) {
            updateTarget(target)
            updateImage()
        }

        layer.dispatchEvents(
            target,
            leftPressed,
            rightPressed,
            leftReleased,
            rightReleased,
            targetPosition.x,
            targetPosition.y
        )
    }

    private fun updateTarget(target: Component) {
        if (target is JMenu) {
            if (target.getClientProperty("isShowing") != true) {
                val location = getSubmenuOrigin(x, y, target)
                val popup = WaylandPopupFactory.getPopup(contents, target.popupMenu, location.x, location.y)
                popup.parent = target
                popup.show()
                submenu = popup
                target.putClientProperty("isShowing", true)
            }
        } else {
            closeSubmenu()
        }

        val path = listOfNotNull(owner, contents, target.takeIf { it is JMenuItem }).filterIsInstance<MenuElement>()
        MenuSelectionManager.defaultManager().selectedPath = path.toTypedArray()

        previousTarget = target
    }

    private fun updateImage() {
        val width = contents.width
        val height = contents.height

        val buffer = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d = buffer.createGraphics()
        contents.paint(g2d)
        g2d.dispose()

        val rgb = buffer.getRGB(0, 0, width, height, null, 0, width)

        layer.setImage(rgb)
    }

    private fun closeSubmenu() {
        submenu?.let {
            (it.parent as JMenu).putClientProperty("isShowing", false)
            it.hide()
            submenu = null
        }
    }

    private fun adjustPopupLocationToFitScreen(x: Int, y: Int, popup: JPopupMenu): Point {
        val screenBounds = NativeFactory.instance.environment.workArea.toRectangle()
        val popupBounds = Rectangle(Point(x, y), popup.preferredSize)
        return Point(
            popupBounds.x.coerceIn(screenBounds.x, screenBounds.x + screenBounds.width - popupBounds.width),
            popupBounds.y.coerceIn(screenBounds.y, screenBounds.y + screenBounds.height - popupBounds.height)
        )
    }

    private fun getSubmenuOrigin(x: Int, y: Int, submenu: JMenu): Point {
        val x = x + contents.width + UIManager.getInt("Menu.submenuPopupOffsetX").coerceAtMost(-2)
        val y = y + submenu.y + UIManager.getInt("Menu.submenuPopupOffsetY")
        return adjustPopupLocationToFitScreen(x, y, submenu.popupMenu)
    }
}
