/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package io.github.bujjuisabee.shimelinux

import com.group_finity.mascot.Main
import com.group_finity.mascot.environment.Area
import com.group_finity.mascot.environment.Environment
import org.freedesktop.dbus.annotations.DBusInterfaceName
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.DBusInterface
import org.freedesktop.dbus.types.Variant
import java.awt.Point
import java.awt.Rectangle
import java.io.File

class KdeEnvironment : Environment() {
    override val workArea
        get() = screen

    override val activeIE = Area()
    override var activeIETitle = ""

    private val dbus = DBusConnectionBuilder.forSessionBus().build()
    private val client = KWinClientImpl()
    private var scripting: KWinScripting? = null
    private var script: KWinScript? = null

    private val windowCache = mutableMapOf<String, Boolean>()

    init {
        runCatching {
            dbus.requestBusName("io.github.bujjuisabee.shimelinux")
            dbus.exportObject(client)

            val scriptFile = File.createTempFile("shimelinux-kwin-script", ".js")
            scriptFile.deleteOnExit()
            this::class.java.getResourceAsStream("/shimelinux-kwin-script.js")?.use {
                it.copyTo(scriptFile.outputStream())
            }

            scripting = dbus.getRemoteObject(
                "org.kde.KWin",
                "/Scripting",
                KWinScripting::class.java
            )

            val scriptId = checkNotNull(scripting).loadScript(scriptFile.absolutePath, "shimelinux-kwin-script")
            script = dbus.getRemoteObject(
                "org.kde.KWin",
                "/Scripting/Script$scriptId",
                KWinScript::class.java
            )

            script?.run()

            Runtime.getRuntime().addShutdownHook(Thread {
                dispose()
            })
        }
    }

    override fun tick() {
        super.tick()

        val activeWindow = client.activeWindow
        if (activeWindow != null && isIE(activeWindow)) {
            activeIE.isVisible = true
            activeIE.set(activeWindow.bounds)
            activeIETitle = activeWindow.title
        } else {
            activeIE.isVisible = false
            activeIE.set(Rectangle(0, 0, 0, 0))
            activeIETitle = ""
        }
    }

    override fun moveActiveIE(point: Point) {
        client.windowPosition = point
    }

    override fun restoreIE() {
        val window = client.activeWindow
        if (window != null) {
            client.windowPosition = Point(
                (workArea.width / 2) - (window.bounds.width / 2),
                (workArea.height / 2) - (window.bounds.height / 2)
            )
        }
    }

    override fun refreshCache() {
        windowCache.clear()
    }

    override fun dispose() {
        runCatching {
            script?.stop()
            scripting?.unloadScript("shimelinux-kwin-script")
            dbus.disconnect()
        }
    }

    private fun isIE(window: Window): Boolean {
        val cachedResult = windowCache[window.title]
        if (cachedResult != null) {
            return cachedResult
        }

        var blacklistInUse = false
        val blacklist = Main.instance.properties.getProperty("InteractiveWindowsBlacklist", "").split('/')
        for (title in blacklist) {
            if (title.isNotBlank()) {
                blacklistInUse = true
                if (window.title.contains(title, true)) {
                    windowCache[window.title] = false
                    return false
                }
            }
        }

        var whitelistInUse = false
        val whitelist = Main.instance.properties.getProperty("InteractiveWindows", "").split('/')
        for (title in whitelist) {
            if (title.isNotBlank()) {
                whitelistInUse = true
                if (window.title.contains(title, true)) {
                    windowCache[window.title] = true
                    return true
                }
            }
        }

        return blacklistInUse || !whitelistInUse
    }

    @DBusInterfaceName("org.kde.kwin.Scripting")
    interface KWinScripting : DBusInterface {
        fun loadScript(path: String, name: String): Int
        fun unloadScript(name: String)
    }

    @DBusInterfaceName("org.kde.kwin.Script")
    interface KWinScript : DBusInterface {
        fun run()
        fun stop()
    }

    @DBusInterfaceName("io.github.bujjuisabee.shimelinux.KWinClient")
    interface KWinClient : DBusInterface {
        fun setActiveWindow(
            internalId: String,
            caption: String,
            x: Int,
            y: Int,
            width: Int,
            height: Int
        )

        fun resetActiveWindow()

        fun getWindowPosition(): Map<String, Variant<*>>?
    }

    class KWinClientImpl : KWinClient {
        var activeWindow: Window? = null
        var windowPosition: Point? = null

        override fun setActiveWindow(
            internalId: String,
            caption: String,
            x: Int,
            y: Int,
            width: Int,
            height: Int
        ) {
            activeWindow = Window(
                internalId,
                caption,
                Rectangle(x, y, width, height)
            )
        }

        override fun resetActiveWindow() {
            activeWindow = null
        }

        override fun getWindowPosition(): Map<String, Variant<*>>? {
            val activeWindow = activeWindow
            val windowPosition = windowPosition
            if (activeWindow == null || windowPosition == null) {
                return null
            }

            val result = mapOf(
                "windowId" to Variant(activeWindow.id),
                "x" to Variant(windowPosition.x),
                "y" to Variant(windowPosition.y)
            )

            this.windowPosition = null
            return result
        }

        override fun getObjectPath() = "/KWinClient"
    }

    data class Window(
        val id: String,
        val title: String,
        val bounds: Rectangle
    )
}
