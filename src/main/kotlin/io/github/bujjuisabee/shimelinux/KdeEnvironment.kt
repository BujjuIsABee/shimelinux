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
import java.util.logging.Level
import java.util.logging.Logger

class KdeEnvironment : Environment() {
    override val workArea
        get() = screen

    override val activeIE = Area()
    override val activeIETitle
        get() = activeWindow?.name ?: ""

    private val client = KWinClientImpl()
    private var dbus = DBusConnectionBuilder.forSessionBus().build()
    private var scriptingObj: KWinScripting? = null
    private var scriptObj: KWinScript? = null

    init {
        activeIE.isVisible = false

        try {
            dbus.use {
                it.requestBusName("io.github.bujjuisabee.shimelinux")
                it.exportObject(client)

                val script = """
                    function setWindow(window) {
                        let bounds = window.frameGeometry;
                        if (window.normalWindow) {
                            callDBus(
                                "io.github.bujjuisabee.shimelinux",
                                "/KWinClient",
                                "io.github.bujjuisabee.shimelinux",
                                "setWindow",
                                window.pid,
                                window.caption,
                                bounds.x,
                                bounds.y,
                                bounds.width,
                                bounds.height,
                                () => {}
                            );
                        }
                    }
                    
                    let windows = workspace.windowList();
                    for (i in windows) {
                        let window = windows[i];
                        setWindow(window);
                    }
                    
                    workspace.windowAdded.connect(setWindow);
                    workspace.windowMoved.connect(setWindow);
                    workspace.windowRemoved.connect(function(window) {
                        callDBus(
                            "io.github.bujjuisabee.shimelinux",
                            "/KWinClient",
                            "io.github.bujjuisabee.shimelinux",
                            "removeWindow",
                            window.pid,
                            () => {}
                        );
                    });
                """.trimIndent()

                val scriptFile = File.createTempFile("shimelinux-kwin", ".js")
                scriptFile.writeText(script)
                scriptFile.deleteOnExit()

                scriptingObj = dbus.getRemoteObject(
                    "org.kde.KWin",
                    "/Scripting",
                    KWinScripting::class.java
                )

                val scriptId = scriptingObj!!.loadScript(scriptFile.absolutePath, "shimelinux-kwin")
                scriptObj = dbus.getRemoteObject(
                    "org.kde.KWin",
                    "/Scripting/Script$scriptId",
                    KWinScript::class.java
                )

                scriptObj!!.run()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun tick() {
        super.tick()

        activeWindowId = windows.keys.firstOrNull()
        if (activeWindow != null) {
            activeIE.set(activeWindow!!.bounds)
        }
    }

    override fun moveActiveIE(point: Point) {}

    override fun restoreIE() {}

    override fun refreshCache() {}

    override fun dispose() {
        scriptObj?.stop()
        scriptingObj?.unloadScript("shimelinux-kwin")
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
            pid: Int,
            caption: String,
            x: Int,
            y: Int,
            width: Int,
            height: Int
        )

        fun removeWindow(pid: Int)
    }

    class KWinClientImpl : KWinClient {
        override fun setWindow(pid: Int, caption: String, x: Int, y: Int, width: Int, height: Int) {
            Logger.getLogger(this::class.java.name).log(Level.INFO, "SET WINDOW")

            val blacklist = Main.instance.properties.getProperty("InteractiveWindowsBlacklist", "").split('/')
            for (title in blacklist) {
                if (title.isNotBlank() && caption.contains(title)) {
                    return
                }
            }

            windows[pid] = Window(
                caption,
                Rectangle(x, y, width, height)
            )

            return
        }

        override fun removeWindow(pid: Int) {
            Logger.getLogger(this::class.java.name).log(Level.INFO, "REMOVE")

            windows.remove(pid)
            if (activeWindowId == pid) {
                activeWindowId = null
            }
        }

        override fun getObjectPath() = "/KWinClient"
    }

    data class Window(
        val name: String,
        val bounds: Rectangle,
    )

    companion object {
        private val windows = mutableMapOf<Int, Window>()
        private var activeWindowId: Int? = null
        private val activeWindow
            get() = windows[activeWindowId]
    }
}
