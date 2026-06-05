/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
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
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities
import javax.swing.event.PopupMenuEvent
import javax.swing.event.PopupMenuListener

class Mascot(var imageSet: String) {
    private val id = lastId.incrementAndGet()
    private val window = NativeFactory.instance.newTransparentWindow()
    var manager: Manager? = null
    var anchor = Point(0, 0)
    var image: MascotImage? = null
    var isLookRight = false
    var behavior: Behavior? = null
        set(value) {
            field = value
            field!!.init(this)
        }
    var time = 0
        private set
    val count: Int
        get() = manager?.getCount(imageSet) ?: 0
    val totalCount: Int
        get() = manager?.count ?: 0
    private var isAnimating = true
    var isPaused = false
    var isDragging = false
    val environment = MascotEnvironment(this)
    var sound: String? = null
    val affordances = ArrayList<String>(5)
    val hotspots = ArrayList<Hotspot>(5)
    val isHotspotClicked
        get() = cursorPosition != null
    val bounds: Rectangle
        get() {
            if (image != null) {
                val top = anchor.y - image!!.center.y
                val left = anchor.x - image!!.center.x
                return Rectangle(left, top, image!!.size.width, image!!.size.height)
            } else {
                return window.asComponent().bounds
            }
        }
    var cursorPosition: Point? = null
        set(value) {
            field = value

            if (value == null) {
                refreshCursor(false)
            } else {
                refreshCursor(value)
            }
        }
    val variables: VariableMap? by lazy {
        return@lazy VariableMap()
    }

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
                    if (!isPaused && behavior != null) {
                        try {
                            behavior!!.mousePressed(e)
                        } catch (e: CantBeAliveException) {
                            log.log(Level.SEVERE, "Fatal Error", e)
                            Main.showError(Main.instance.languageBundle.getString("SevereShimejiErrorErrorMessage"), e)
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
                    if (!isPaused && behavior != null) {
                        try {
                            behavior!!.mouseReleased(e)
                        } catch (e: CantBeAliveException) {
                            log.log(Level.SEVERE, "Fatal Error", e)
                            Main.showError(Main.instance.languageBundle.getString("SevereShimejiErrorErrorMessage"), e)
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
        val lang = Main.instance.languageBundle

        popup.addPopupMenuListener(object : PopupMenuListener {
            override fun popupMenuCanceled(p0: PopupMenuEvent?) {
            }

            override fun popupMenuWillBecomeInvisible(p0: PopupMenuEvent?) {
                isAnimating = true
            }

            override fun popupMenuWillBecomeVisible(p0: PopupMenuEvent?) {
                isAnimating = false
            }
        })

        val callAnotherMenu = JMenuItem(lang.getString("CallAnother"))
        callAnotherMenu.addActionListener {
            // TODO: uncomment this
            // Main.instance.createMascot(imageSet)
        }

        val followCursorMenu = JMenuItem(lang.getString("FollowCursor"))
        followCursorMenu.addActionListener {
            manager?.setBehaviorAll(checkNotNull(Main.instance.getConfiguration(imageSet)), "ChaseMouse", imageSet)
        }

        val restoreWindowsMenu = JMenuItem(lang.getString("RestoreWindows"))
        restoreWindowsMenu.addActionListener {
            NativeFactory.instance.getEnvironment().restoreIE()
        }

        val dismissMenu = JMenuItem(lang.getString("Dismiss"))
        dismissMenu.addActionListener {
            dispose()
        }

        val dismissOthersMenu = JMenuItem(lang.getString("DismissOthers"))
        dismissOthersMenu.addActionListener {
            manager?.remainOne(imageSet)
        }

        val dismissAllOthersMenu = JMenuItem(lang.getString("DismissAllOthers"))
        dismissAllOthersMenu.addActionListener {
            manager?.remainOne(this)
        }

        val dismissAllMenu = JMenuItem(lang.getString("DismissAll"))
        dismissAllMenu.addActionListener {
            Main.instance.exit()
        }

        val pauseMenu = JMenuItem(if (isAnimating) {
            lang.getString("PauseAnimations")
        } else {
            lang.getString("ResumeAnimations")
        })
        pauseMenu.addActionListener {
            isPaused = !isPaused
        }

        // TODO: Implement behaviors submenu

        popup.add(callAnotherMenu)
        popup.addSeparator()
        popup.add(followCursorMenu)
        popup.add(restoreWindowsMenu)
        popup.addSeparator()
        // add submenu
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
        if (isAnimating && behavior != null) {
            try {
                behavior!!.next()
            } catch (e: CantBeAliveException) {
                log.log(Level.SEVERE, "Fatal Error", e)
                Main.showError(Main.instance.languageBundle.getString("CouldNotGetNextBehaviourErrorMessage"), e)
                dispose()
            }

            time++
        }
    }

    fun apply() {
        if (isAnimating) {
            if (image != null) {
                // Set the window's position and size
                window.asComponent().bounds = bounds

                // Show the image
                window.setImage(image!!.image)
                window.asComponent().isVisible = true
                window.updateImage()
            } else {
                window.asComponent().isVisible = false
            }

            if (!Sounds.isMuted && sound != null && Sounds.contains(sound!!)) {
                val clip = Sounds.getSound(sound!!)!!
                if (!clip.isRunning) {
                    clip.stop()
                    clip.microsecondPosition = 0
                    clip.start()
                }
            }
        }
    }

    fun dispose() {
        log.log(Level.INFO, "Destroying mascot: {$this}")

        isAnimating = false
        window.dispose()
        affordances.clear()
        manager?.remove(this)
    }

    private fun refreshCursor(position: Point) {
        var useHand = false
        for (hotspot in hotspots) {
            if (hotspot.contains(this, position) &&
                checkNotNull(Main.instance.getConfiguration(imageSet)).isBehaviorEnabled(hotspot.behavior, this)
            ) {
                useHand = true
                break
            }
        }
        refreshCursor(useHand)
    }

    private fun refreshCursor(useHand: Boolean) {
        window.asComponent().cursor =
            Cursor.getPredefinedCursor(if (useHand) Cursor.HAND_CURSOR else Cursor.DEFAULT_CURSOR)
    }

    override fun toString(): String = "Mascot ($id,$imageSet)"

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
        private val lastId = AtomicInteger()
    }
}
