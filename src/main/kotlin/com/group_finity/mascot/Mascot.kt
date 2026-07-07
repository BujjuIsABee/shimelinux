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

package com.group_finity.mascot

import com.group_finity.mascot.behavior.Behavior
import com.group_finity.mascot.environment.MascotEnvironment
import com.group_finity.mascot.exception.CantBeAliveException
import com.group_finity.mascot.hotspot.Hotspot
import com.group_finity.mascot.image.MascotImage
import com.group_finity.mascot.script.VariableMap
import com.group_finity.mascot.sound.Sounds
import java.awt.Cursor
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Level
import java.util.logging.Logger
import javax.swing.JCheckBoxMenuItem
import javax.swing.JMenu
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener

class Mascot(var imageSet: String) {
    private val id = lastId.incrementAndGet()
    private val window = NativeFactory.instance.newTransparentWindow()
    private var debugWindow: DebugWindow? = null
    val environment = MascotEnvironment(this)
    val variables = VariableMap()
    val affordances = mutableListOf<String>()
    val hotspots = mutableListOf<Hotspot>()
    val isHotspotClicked get() = cursorPosition != null
    var manager: Manager? = null
    var anchor = Point(0, 0)
    var image: MascotImage? = null
    var sound: String? = null
    var isLookRight = false
    var isPaused = false
    var isDragging = false
    private var isAnimating = true
        get() = field && !isPaused
    var cursorPosition: Point? = null
        set(value) {
            field = value
            if (value == null) {
                refreshCursor(false)
            } else {
                refreshCursor(value)
            }
        }
    val bounds: Rectangle
        get() {
            val image = image
            if (image != null) {
                val top = anchor.y - image.center.y
                val left = anchor.x - image.center.x
                return Rectangle(left, top, image.size.width, image.size.height)
            } else {
                return window.asComponent().bounds
            }
        }
    var behavior: Behavior? = null
        set(value) {
            field = value?.also { it.init(this) }
        }
    var time = 0
        private set

    @Suppress("unused")
    val count: Int
        get() = manager?.getCount(imageSet) ?: 0

    @Suppress("unused")
    val totalCount: Int
        get() = manager?.count ?: 0

    init {
        log.log(Level.INFO, "Created a mascot ($this)")

        window.setAlwaysOnTop(true)
        window.asComponent().addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                if (e.isPopupTrigger) {
                    SwingUtilities.invokeLater {
                        showPopup(e.x, e.y)
                    }
                } else {
                    val behavior = behavior
                    if (!isPaused && behavior != null) {
                        try {
                            behavior.mousePressed(e)
                        } catch (e: CantBeAliveException) {
                            log.log(Level.SEVERE, "Fatal Error", e)
                            Main.showError("SevereShimejiErrorErrorMessage".localize(), e)
                            dispose()
                        }
                    }
                }
            }

            override fun mouseReleased(e: MouseEvent) {
                if (e.isPopupTrigger) {
                    SwingUtilities.invokeLater {
                        showPopup(e.x, e.y)
                    }
                } else {
                    val behavior = behavior
                    if (!isPaused && behavior != null) {
                        try {
                            behavior.mouseReleased(e)
                        } catch (e: CantBeAliveException) {
                            log.log(Level.SEVERE, "Fatal Error", e)
                            Main.showError("SevereShimejiErrorErrorMessage".localize(), e)
                            dispose()
                        }
                    }
                }
            }
        })
        window.asComponent().addMouseMotionListener(object : MouseMotionListener {
            override fun mouseMoved(e: MouseEvent) {
                if (isPaused) {
                    refreshCursor(false)
                } else {
                    if (isHotspotClicked) {
                        cursorPosition = e.point
                    } else {
                        refreshCursor(e.point)
                    }
                }
            }

            override fun mouseDragged(e: MouseEvent) {
                if (isPaused) {
                    refreshCursor(false)
                } else {
                    if (isHotspotClicked) {
                        cursorPosition = e.point
                    } else {
                        refreshCursor(e.point)
                    }
                }
            }
        })
    }

    private fun showPopup(x: Int, y: Int) {
        val popup = JPopupMenu()

        popup.addPopupMenuListener(object : PopupMenuListener {
            override fun popupMenuCanceled(e: PopupMenuEvent) {}

            override fun popupMenuWillBecomeInvisible(e: PopupMenuEvent) {
                isAnimating = true
            }

            override fun popupMenuWillBecomeVisible(e: PopupMenuEvent) {
                isAnimating = false
            }
        })

        val chooseShimejiMenu = JMenuItem("ChooseShimeji".localize())
        chooseShimejiMenu.addActionListener {
            Main.instance.chooseShimejiMenuClicked()
        }

        val settingsMenu = JMenuItem("Settings".localize())
        settingsMenu.addActionListener {
            Main.instance.settingsMenuClicked()
        }

        val callAnotherMenu = JMenuItem("CallAnother".localize())
        callAnotherMenu.addActionListener {
            Main.instance.createMascot(imageSet)
        }

        val followCursorMenu = JMenuItem("FollowCursor".localize())
        followCursorMenu.addActionListener {
            manager?.setBehaviorAll(getConfiguration(imageSet), "ChaseMouse", imageSet)
        }

        val restoreWindowsMenu = JMenuItem("RestoreWindows".localize())
        restoreWindowsMenu.addActionListener {
            NativeFactory.instance.environment.restoreIE()
        }

        val debugMenu = JMenuItem("RevealStatistics".localize())
        debugMenu.addActionListener {
            if (debugWindow == null) {
                debugWindow = DebugWindow(imageSet)
            }
            debugWindow?.isVisible = true
        }

        val dismissMenu = JMenuItem("Dismiss".localize())
        dismissMenu.addActionListener {
            dispose()
        }

        val dismissOthersMenu = JMenuItem("DismissOthers".localize())
        dismissOthersMenu.addActionListener {
            manager?.remainOne(imageSet)
        }

        val dismissAllOthersMenu = JMenuItem("DismissAllOthers".localize())
        dismissAllOthersMenu.addActionListener {
            manager?.remainOne(this)
        }

        val dismissAllMenu = JMenuItem("DismissAll".localize())
        dismissAllMenu.addActionListener {
            Main.instance.exit()
        }

        val pauseMenu = JMenuItem(
            if (isAnimating) {
                "PauseAnimations".localize()
            } else {
                "ResumeAnimations".localize()
            }
        )
        pauseMenu.addActionListener {
            isPaused = !isPaused
        }

        val behaviorsSubmenu = JMenu("SetBehavior".localize())
        val allowedSubmenu = JMenu("AllowedBehaviors".localize())
        val config = getConfiguration(imageSet)
        for (behaviorName in config.behaviorNames) {
            try {
                if (!config.isBehaviorHidden(behaviorName)) {
                    val caption = behaviorName.replace("([a-z])(IE)?([A-Z])", "$1 $2 $3").replace("  ", " ")
                    if (config.isBehaviorEnabled(behaviorName, this) && !behaviorName.contains('/')) {
                        val item = JMenuItem(if (Main.instance.languageBundle.containsKey(behaviorName)) behaviorName.localize() else caption)
                        item.addActionListener {
                            try {
                                behavior = config.buildBehavior(behaviorName)
                            } catch (e: Exception) {
                                log.log(Level.SEVERE, "Failed to set behavior ($this)")
                                Main.showError("CouldNotSetBehaviorErrorMessage".localize(), e)
                            }
                        }
                        behaviorsSubmenu.add(item)
                    }
                    if (config.isBehaviorToggleable(behaviorName) && !behaviorName.contains('/')) {
                        val toggleItem = JCheckBoxMenuItem(caption, config.isBehaviorEnabled(behaviorName, this))
                        toggleItem.addActionListener {
                            Main.instance.setMascotBehaviorEnabled(
                                behaviorName,
                                this,
                                !config.isBehaviorEnabled(behaviorName, this)
                            )
                        }
                        allowedSubmenu.add(toggleItem)
                    }
                }
            } catch (_: Exception) {
            }
        }

        if (Main.instance.noTrayIcon) {
            popup.add(chooseShimejiMenu)
            popup.add(settingsMenu)
            popup.addSeparator()
        }

        popup.add(callAnotherMenu)
        popup.addSeparator()
        popup.add(followCursorMenu)
        popup.add(restoreWindowsMenu)
        popup.add(debugMenu)
        popup.addSeparator()
        if (behaviorsSubmenu.menuComponentCount > 0) {
            popup.add(behaviorsSubmenu)
        }
        if (allowedSubmenu.menuComponentCount > 0) {
            popup.add(allowedSubmenu)
        }
        popup.addSeparator()
        popup.add(pauseMenu)
        popup.addSeparator()
        popup.add(dismissMenu)
        popup.add(dismissOthersMenu)
        popup.add(dismissAllOthersMenu)
        popup.add(dismissAllMenu)

        window.asComponent().requestFocus()
        popup.isLightWeightPopupEnabled = false
        popup.show(window.asComponent(), x, y)
    }

    fun tick() {
        if (isAnimating) {
            try {
                behavior?.next()
            } catch (e: CantBeAliveException) {
                log.log(Level.SEVERE, "Fatal Error", e)
                Main.showError("CouldNotGetNextBehaviorErrorMessage".localize(), e)
                dispose()
            }
            time++
        }

        debugWindow?.let { debugWindow ->
            behavior?.let { behavior ->
                debugWindow.setBehavior(
                    behavior.toString()
                        .substring(10, behavior.toString().length - 1)
                        .replace("([a-z])(IE)?([A-Z])", "$1 $2 $3")
                        .replace("  ", " ")
                )
            }

            debugWindow.setShimejiX(anchor.x)
            debugWindow.setShimejiY(anchor.y)

            val activeWindow = environment.activeIE
            debugWindow.setWindowTitle(environment.activeIETitle)
            debugWindow.setWindowX(activeWindow.left)
            debugWindow.setWindowY(activeWindow.top)
            debugWindow.setWindowWidth(activeWindow.width)
            debugWindow.setWindowHeight(activeWindow.height)

            val workArea = environment.workArea
            debugWindow.setEnvironmentX(workArea.left)
            debugWindow.setEnvironmentY(workArea.top)
            debugWindow.setEnvironmentWidth(workArea.width)
            debugWindow.setEnvironmentHeight(workArea.height)
        }
    }

    fun apply() {
        if (isAnimating) {
            val image = image
            if (image != null) {
                // Set the window's position and size
                window.asComponent().bounds = bounds

                // Show the image
                window.setImage(image.image)
                window.asComponent().isVisible = true
                window.updateImage()
            } else {
                window.asComponent().isVisible = false
            }

            val sound = sound
            if (!Sounds.isMuted && sound != null && Sounds.contains(sound)) {
                Sounds.getSound(sound)?.let { clip ->
                    if (!clip.isRunning) {
                        clip.stop()
                        clip.microsecondPosition = 0
                        clip.start()
                    }
                }
            }
        }
    }

    fun dispose() {
        log.log(Level.INFO, "Destroying mascot: $this")

        debugWindow?.let {
            it.isVisible = false
            debugWindow = null
        }

        isAnimating = false
        window.dispose()
        affordances.clear()
        manager?.remove(this)
    }

    private fun refreshCursor(position: Point) {
        var useHand = false
        for (hotspot in hotspots) {
            val isEnabled = getConfiguration(imageSet).isBehaviorEnabled(hotspot.behavior, this)
            if (hotspot.contains(this, position) && isEnabled) {
                useHand = true
                break
            }
        }
        refreshCursor(useHand)
    }

    private fun refreshCursor(useHand: Boolean) {
        window.asComponent().cursor = Cursor.getPredefinedCursor(if (useHand) Cursor.HAND_CURSOR else Cursor.DEFAULT_CURSOR)
    }

    override fun toString() = "Mascot ($id, $imageSet)"

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
        private val lastId = AtomicInteger()
    }
}
