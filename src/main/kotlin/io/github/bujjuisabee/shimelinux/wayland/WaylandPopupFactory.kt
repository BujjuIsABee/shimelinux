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
import io.github.bujjuisabee.shimelinux.wayland.lib.MouseEventReceiver
import java.awt.Component
import java.awt.Point
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import javax.swing.JMenu
import javax.swing.MenuElement
import javax.swing.MenuSelectionManager
import javax.swing.Popup
import javax.swing.PopupFactory
import javax.swing.SwingUtilities
import javax.swing.UIManager

/**
 * A popup factory that displays popups on Wayland layer surfaces.
 *
 * @author Bujju
 */
object WaylandPopupFactory : PopupFactory() {
    override fun getPopup(owner: Component?, contents: Component, x: Int, y: Int): Popup =
        WaylandPopup(owner, contents, x, y)

    private class WaylandPopup(
        private val owner: Component?,
        private val contents: Component,
        private val x: Int,
        private val y: Int,
    ) : Popup(owner, contents, x, y), MouseEventReceiver {
        private val layer = WaylandLayer(this, false)
        private var target: Component? = null
        private var submenu: WaylandPopup? = null
        private var menu: JMenu? = null

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

        override fun updateCursor(
            leftPressed: Boolean,
            rightPressed: Boolean,
            leftReleased: Boolean,
            rightReleased: Boolean,
            positionX: Int,
            positionY: Int
        ) {
            val target = SwingUtilities.getDeepestComponentAt(contents, positionX, positionY) ?: contents
            val targetPosition = SwingUtilities.convertPoint(contents, positionX, positionY, target)
            val targetIsSubmenu = target is JMenu

            // Check if the selected menu element has changed
            if (this.target != target) {
                this.target = target

                // Update submenu
                if (targetIsSubmenu) {
                    closeSubmenu()
                    openSubmenu(target)
                } else {
                    closeSubmenu()
                }

                // Update selection
                MenuSelectionManager.defaultManager().selectedPath =
                    setOf(owner, contents, target).filterIsInstance<MenuElement>().toTypedArray()

                updateImage()
            }

            // Dispatch mouse events to the selected menu element
            layer.dispatchEvents(
                target,
                leftPressed && !targetIsSubmenu,
                rightPressed && !targetIsSubmenu,
                leftReleased && !targetIsSubmenu,
                rightReleased && !targetIsSubmenu,
                targetPosition.x,
                targetPosition.y
            )
        }

        private fun updateImage() {
            // Draw the popup on an image buffer
            val buffer = BufferedImage(contents.width, contents.height, BufferedImage.TYPE_INT_ARGB)
            val g2d = buffer.createGraphics()
            contents.paint(g2d)
            g2d.dispose()

            // Display the image buffer on the layer surface
            layer.setImage((buffer.raster.dataBuffer as DataBufferInt).data)
        }

        private fun openSubmenu(menu: JMenu) {
            if (menu.getClientProperty("WaylandPopup.isShowing") != true) {
                menu.putClientProperty("WaylandPopup.isShowing", true)

                val location = getSubmenuOrigin(x, y, menu)
                val submenu = getPopup(contents, menu.popupMenu, location.x, location.y) as WaylandPopup
                submenu.menu = menu
                submenu.show()

                this.submenu = submenu
            }
        }

        private fun closeSubmenu() {
            val submenu = submenu ?: return
            submenu.menu?.putClientProperty("WaylandPopup.isShowing", false)
            submenu.hide()

            this.submenu = null
        }

        private fun getSubmenuOrigin(positionX: Int, positionY: Int, menu: JMenu): Point {
            val screenBounds = NativeFactory.instance.environment.screen.toRectangle()
            val submenuSize = menu.popupMenu.preferredSize

            val xOffset = UIManager.getInt("Menu.submenuPopupOffsetX")
            var x = positionX + menu.width + xOffset
            if (x + submenuSize.width >= screenBounds.x + screenBounds.width) {
                x = positionX - submenuSize.width - xOffset
            }

            val yOffset = UIManager.getInt("Menu.submenuPopupOffsetY")
            var y = positionY + menu.y + yOffset
            if (y + submenuSize.height >= screenBounds.y + screenBounds.height) {
                y = positionY + menu.y + menu.height - submenuSize.height - yOffset
            }

            return Point(
                x.coerceIn(screenBounds.x, screenBounds.x + screenBounds.width - submenuSize.width),
                y.coerceIn(screenBounds.y, screenBounds.y + screenBounds.height - submenuSize.height)
            )
        }
    }
}
