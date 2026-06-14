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

        val scriptFile = File.createTempFile("shimelinux-kwin-script", ".js")
        scriptFile.writeText("""
            let activeWindow = null;
            let frameGeometryChangedHandler = null;
            let windowClosedOrMinimizedHandler = null;
            let width = null;
            let height = null;
            
            function setActiveWindow(window) {
                if (activeWindow != window && !isWindowOnscreen(activeWindow)) return;
            
                const bounds = window.frameGeometry;
                callDBus(
                    "io.github.bujjuisabee.shimelinux",
                    "/KWinClient",
                    "io.github.bujjuisabee.shimelinux",
                    "setActiveWindow",
                    window.internalId.toString(),
                    window.resourceClass,
                    bounds.x,
                    bounds.y,
                    bounds.width,
                    bounds.height,
                    () => {}
                );
            }
            
            function resetActiveWindow() {
                if (!isWindowOnscreen(activeWindow)) return;
                
                callDBus(
                    "io.github.bujjuisabee.shimelinux",
                    "/KWinClient",
                    "io.github.bujjuisabee.shimelinux",
                    "resetActiveWindow",
                    () => {}
                );
            }
            
            function onWindowActivated(window) {
                if (!isWindowOnscreen(activeWindow)) return;
                
                if (!window || !window.normalWindow || window.minimized) {
                    onWindowDeactivated();
                    return;
                }
                
                setActiveWindow(window);
                
                if (activeWindow != window) {
                    activeWindow = window;
                    frameGeometryChangedHandler = setActiveWindow.bind(null, window);
                    windowClosedOrMinimizedHandler = resetActiveWindow.bind(null);
                    window.frameGeometryChanged.connect(frameGeometryChangedHandler);
                    window.closed.connect(windowClosedOrMinimizedHandler);
                    window.minimizedChanged.connect(windowClosedOrMinimizedHandler);
                }
            }
            
            function onWindowDeactivated() {
                if (!isWindowOnscreen(activeWindow)) return;
            
                resetActiveWindow();
                activeWindow.frameGeometryChanged.disconnect(frameGeometryChangedHandler);
                activeWindow.closed.disconnect(windowClosedOrMinimizedHandler);
                activeWindow.minimizedChanged.disconnect(windowClosedOrMinimizedHandler);
                frameGeometryChangedHandler = null;
                windowClosedOrMinimizedHandler = null;
            }
            
            function move() {
                callDBus(
                    "io.github.bujjuisabee.shimelinux",
                    "/KWinClient",
                    "io.github.bujjuisabee.shimelinux",
                    "getWindowPosition",
                    (array) => {
                        if (array[0] != "null" && array[1] != "null" && array[2] != "null") {
                            const windowId = array[0];
                            const x = parseInt(array[1], 10);
                            const y = parseInt(array[2], 10);
                            
                            const windows = workspace.windowList();
                            
                            for (let i = 0; i < windows.length; i++) {
                                const window = windows[i];
                                if (window.internalId.toString() == windowId) {
                                    const bounds = window.frameGeometry;
                                    window.frameGeometry = {
                                        x: x,
                                        y: y,
                                        width: bounds.width,
                                        height: bounds.height
                                    };
                                    break;
                                }
                            }
                        }
                    }
                );
            }
            
            function isWindowOnscreen(window) {
                if (!window || !window.normalWindow) return true;
                
                const windowBounds = window.frameGeometry;
                const screenBounds = workspace.clientArea(KWin.MaximizeArea, window);
                
                return windowBounds.x >= screenBounds.x &&
                       windowBounds.y >= screenBounds.y &&
                       (windowBounds.x + windowBounds.width) <= (screenBounds.x + screenBounds.width) &&
                       (windowBounds.y + windowBounds.height) <= (screenBounds.y + screenBounds.height);
            }
            
            workspace.windowActivated.connect(onWindowActivated);
            if (workspace.activeWindow != null) {
                onWindowActivated(workspace.activeWindow);
            }
            
            const timer = new QTimer();
            timer.interval = 40;
            timer.timeout.connect(move);
            timer.start();
        """.trimIndent())
        scriptFile.deleteOnExit()

        scripting = dbus.getRemoteObject(
            "org.kde.KWin",
            "/Scripting",
            KWinScripting::class.java
        )

        val scriptId = scripting.loadScript(scriptFile.absolutePath, "shimelinux-kwin-script")
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
        cache.clear()
    }

    override fun dispose() {
        script.stop()
        scripting.unloadScript("shimelinux-kwin-script")
        dbus.disconnect()
    }

    private fun isIE(window: Window): Boolean {
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
            internalId: String,
            resourceClass: String,
            x: Int,
            y: Int,
            width: Int,
            height: Int
        )

        fun resetActiveWindow()

        fun getWindowPosition(): Array<String>
    }

    class KWinClientImpl : KWinClient {
        var activeWindow: Window? = null
        var windowPosition: Point? = null

        override fun setActiveWindow(
            internalId: String,
            resourceClass: String,
            x: Int,
            y: Int,
            width: Int,
            height: Int
        ) {
            activeWindow = Window(
                internalId,
                resourceClass,
                Rectangle(x, y, width, height)
            )
        }

        override fun resetActiveWindow() {
            activeWindow = null
        }

        override fun getWindowPosition(): Array<String> {
            val result = arrayOf(
                activeWindow?.id ?: "null",
                windowPosition?.x?.toString() ?: "null",
                windowPosition?.y?.toString() ?: "null"
            )
            windowPosition = null
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
