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
    override var activeIETitle = ""

    private val dbus = DBusConnectionBuilder.forSessionBus().build()
    private val client = KWinClientImpl()
    private val scripting: KWinScripting
    private val script: KWinScript

    private val cache = mutableMapOf<String, Boolean>()

    init {
        dbus.requestBusName("io.github.bujjuisabee.shimelinux")
        dbus.exportObject(client)

        val scriptFile = File.createTempFile("shimelinux-kwin", ".js")
        scriptFile.writeText("""
            let activeWindow = null;
            let frameGeometryChangedHandler = null;
            let windowClosedOrMinimizedHandler = null;
            let windowMaximizedHandler = null;
            let width = null;
            let height = null;
            
            function setActiveWindow(window) {
                const bounds = window.frameGeometry;
                callDBus(
                    "io.github.bujjuisabee.shimelinux",
                    "/KWinClient",
                    "io.github.bujjuisabee.shimelinux",
                    "setActiveWindow",
                    window.resourceClass,
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height,
                    () => {}
                );
            }
            
            function resetActiveWindow() {
                callDBus(
                    "io.github.bujjuisabee.shimelinux",
                    "/KWinClient",
                    "io.github.bujjuisabee.shimelinux",
                    "resetActiveWindow",
                    () => {}
                );
            }
            
            function onWindowActivated(window) {
                if (!window || !window.normalWindow || window.minimized) {
                    onWindowDeactivated();
                    return;
                }
                
                setActiveWindow(window);
                
                if (activeWindow != window) {
                    activeWindow = window;
                    frameGeometryChangedHandler = onFrameGeometryChanged.bind(null, window);
                    windowClosedOrMinimizedHandler = resetActiveWindow.bind(null);
                    windowMaximizedHandler = onWindowMaximized.bind(null, window);
                    window.frameGeometryChanged.connect(frameGeometryChangedHandler);
                    window.closed.connect(windowClosedOrMinimizedHandler);
                    window.minimizedChanged.connect(windowClosedOrMinimizedHandler);
                    window.maximizedChanged.connect(windowMaximizedHandler);
                }
            }
            
            function onWindowDeactivated() {
                resetActiveWindow();
                activeWindow.frameGeometryChanged.disconnect(frameGeometryChangedHandler);
                activeWindow.closed.disconnect(windowClosedOrMinimizedHandler);
                activeWindow.minimizedChanged.disconnect(windowClosedOrMinimizedHandler);
                activeWindow.maximizedChanged.disconnect(windowMaximizedHandler);
                frameGeometryChangedHandler = null;
                windowClosedOrMinimizedHandler = null;
                windowMaximizedHandler = null;
            }
            
            function onFrameGeometryChanged(window) {
                const bounds = window.frameGeometry;
                if (bounds.width == width && bounds.height == height) {
                    resetActiveWindow();
                    width = null;
                    height = null;
                } else {
                    setActiveWindow(window);
                    width = bounds.width;
                    height = bounds.height;
                }
            }
            
            function onWindowMaximized(window) {
                if (window.active) {
                    setActiveWindow(window);
                }
            }
            
            workspace.windowActivated.connect(onWindowActivated);
            onWindowActivated(workspace.activeWindow);
        """.trimIndent())
        scriptFile.deleteOnExit()

        scripting = dbus.getRemoteObject(
            "org.kde.KWin",
            "/Scripting",
            KWinScripting::class.java
        )

        val scriptId = scripting.loadScript(scriptFile.absolutePath, "shimelinux-kwin")
        script = dbus.getRemoteObject(
            "org.kde.KWin",
            "/Scripting/Script$scriptId",
            KWinScript::class.java
        )

        script.run()

        Runtime.getRuntime().addShutdownHook(Thread {
            dispose()
        })
    }

    override fun tick() {
        super.tick()

        val activeWindow = client.activeWindow
        if (activeWindow == null || !isViableIE(activeWindow)) {
            activeIE.isVisible = false
            activeIE.set(Rectangle(0, 0, 0, 0))
            activeIETitle = ""
        } else {
            activeIE.isVisible = true
            activeIE.set(activeWindow.bounds)
            activeIETitle = activeWindow.title
        }
    }

    override fun moveActiveIE(point: Point) {}

    override fun restoreIE() {}

    override fun refreshCache() {
        cache.clear()
    }

    override fun dispose() {
        script.stop()
        scripting.unloadScript("shimelinux-kwin")
        dbus.disconnect()
    }

    private fun isViableIE(window: Window): Boolean {
        if (cache.containsKey(window.title)) {
            return cache[window.title]!!
        }

        var blacklistInUse = false
        val blacklist = Main.instance.properties.getProperty("InteractiveWindowsBlacklist", "").split('/')
        for (title in blacklist) {
            if (title.isNotBlank()) {
                blacklistInUse = true
                if (window.title == title) {
                    cache[window.title] = false
                    return false
                }
            }
        }

        var whitelistInUse = false
        val whitelist = Main.instance.properties.getProperty("InteractiveWindows", "").split('/')
        for (title in whitelist) {
            if (title.isNotBlank()) {
                whitelistInUse = true
                if (window.title == title) {
                    cache[window.title] = true
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
            resourceClass: String,
            x: Int,
            y: Int,
            width: Int,
            height: Int
        )

        fun resetActiveWindow()
    }

    class KWinClientImpl : KWinClient {
        var activeWindow: Window? = null

        override fun setActiveWindow(
            resourceClass: String,
            x: Int,
            y: Int,
            width: Int,
            height: Int
        ) {
            activeWindow = Window(
                resourceClass,
                Rectangle(x, y, width, height)
            )
        }

        override fun resetActiveWindow() {
            activeWindow = null
        }

        override fun getObjectPath() = "/KWinClient"
    }

    data class Window(
        val title: String,
        val bounds: Rectangle
    )
}
