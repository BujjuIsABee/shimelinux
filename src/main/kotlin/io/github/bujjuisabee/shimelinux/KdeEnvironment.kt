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
import java.awt.Point
import java.awt.Rectangle
import java.io.File

class KdeEnvironment : Environment() {
    override val workArea
        get() = screen

    override val activeIE = Area()
    override val activeIETitle = ""

    private val dbus = DBusConnectionBuilder.forSessionBus().build()
    private val client = KWinClientImpl()
    private var scripting: KWinScripting? = null
    private var script: KWinScript? = null

    private val windowCache = mutableMapOf<String, Boolean>()

    init {
        dbus.requestBusName("io.github.bujjuisabee.shimelinux")
        dbus.exportObject(client)

        val scriptFile = File.createTempFile("shimelinux-kwin", ".js")
        scriptFile.writeText("""
            function setWindow(window) {
                const bounds = window.frameGeometry;
                callDBus(
                    "io.github.bujjuisabee.shimelinux",
                    "/KWinClient",
                    "io.github.bujjuisabee.shimelinux",
                    "setWindow",
                    window.internalId.toString(),
                    window.resourceClass,
                    window.active,
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height,
                    () => {}
                );
            }
            
            function onWindowAdded(window) {
                if (!window || !window.normalWindow) return;
            
                setWindow(window);
                
                window.frameGeometryChanged.connect(setWindow.bind(undefined, window));
            }
            
            function onWindowRemoved(window) {
                if (!window || !window.normalWindow) return;
            
                callDBus(
                    "io.github.bujjuisabee.shimelinux",
                    "/KWinClient",
                    "io.github.bujjuisabee.shimelinux",
                    "removeWindow",
                    window.internalId.toString(),
                    () => {}
                );
            }

            workspace.windowAdded.connect(onWindowAdded);
            workspace.windowRemoved.connect(onWindowRemoved);
        """.trimIndent())
        scriptFile.deleteOnExit()

        scripting = dbus.getRemoteObject(
            "org.kde.KWin",
            "/Scripting",
            KWinScripting::class.java
        )

        val scriptId = scripting!!.loadScript(scriptFile.absolutePath, "shimelinux-kwin")
        script = dbus.getRemoteObject(
            "org.kde.KWin",
            "/Scripting/Script$scriptId",
            KWinScript::class.java
        )

        script!!.run()

        Runtime.getRuntime().addShutdownHook(Thread {
            dispose()
        })
    }

    override fun tick() {
        super.tick()

        val activeWindow = client.windows.values.firstOrNull {
            if (!windowCache.containsKey(it.id)) {
                windowCache[it.id] = isViableIE(it)
            }
            return@firstOrNull windowCache[it.id]!!
        }

        if (activeWindow != null) {
            activeIE.isVisible = true
            activeIE.set(activeWindow.frameGeometry)
        } else {
            activeIE.isVisible = false
        }
    }

    override fun moveActiveIE(point: Point) {}

    override fun restoreIE() {}

    override fun refreshCache() {
        windowCache.clear()
    }

    override fun dispose() {
        script?.stop()
        scripting?.unloadScript("shimelinux-kwin")
        dbus?.disconnect()
    }

    private fun isViableIE(window: Window): Boolean {
        if (!window.isActive) return false

        var blacklistInUse = false
        val blacklist = Main.instance.properties.getProperty("InteractiveWindowsBlacklist", "").split("/")
        for (title in blacklist) {
            if (title.isNotBlank()) {
                blacklistInUse = true
                if (window.resourceClass.contains(title)) return false
            }
        }

        var whitelistInUse = false
        val whitelist = Main.instance.properties.getProperty("InteractiveWindows", "").split("/")
        for (title in whitelist) {
            if (title.isNotBlank()) {
                whitelistInUse = true
                if (window.resourceClass.contains(title)) return true
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
        fun setWindow(
            id: String,
            resourceClass: String,
            isActive: Boolean,
            x: Int,
            y: Int,
            width: Int,
            height: Int
        )

        fun removeWindow(id: String)
    }

    class KWinClientImpl : KWinClient {
        val windows = mutableMapOf<String, Window>()

        override fun setWindow(
            id: String,
            resourceClass: String,
            isActive: Boolean,
            x: Int,
            y: Int,
            width: Int,
            height: Int
        ) {
            windows[id] = Window(
                id,
                resourceClass,
                isActive,
                Rectangle(x, y, width, height)
            )
        }

        override fun removeWindow(id: String) {
            windows.remove(id)
        }

        override fun getObjectPath() = "/KWinClient"
    }

    data class Window(
        val id: String,
        val resourceClass: String,
        val isActive: Boolean,
        val frameGeometry: Rectangle
    )
}
