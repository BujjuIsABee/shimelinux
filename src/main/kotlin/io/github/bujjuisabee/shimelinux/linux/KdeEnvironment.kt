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

package io.github.bujjuisabee.shimelinux.linux

import com.group_finity.mascot.environment.Area
import com.group_finity.mascot.environment.Environment
import com.group_finity.mascot.getProperty
import com.group_finity.mascot.loadResource
import org.freedesktop.dbus.annotations.DBusInterfaceName
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.connections.impl.DBusConnectionBuilder
import org.freedesktop.dbus.interfaces.DBusInterface
import java.awt.Point
import java.awt.Rectangle
import java.io.File

class KdeEnvironment : Environment() {
    override val workArea: Area
        get() = screen

    override val activeIE = Area()
    override var activeIETitle = ""

    private var dbus: DBusConnection? = null
    private var scripting: KWinScripting? = null
    private var script: KWinScript? = null
    private val client = KWinClientImpl()
    private var activeWindow: Window? = null
    private var windowPosition: Point? = null
    private var restoreWindows: Boolean = false
    private val windowCache = mutableMapOf<String, Boolean>()

    private val shutdownThread = Thread { handleShutdown() }
    private var isShuttingDown = false

    init {
        try {
            dbus = DBusConnectionBuilder.forSessionBus().build().also {
                it.requestBusName("io.github.bujjuisabee.shimelinux")
                it.exportObject(client)
            }

            val scriptFile = File.createTempFile("shimelinux-kwin-script", ".js")
            scriptFile.deleteOnExit()
            loadResource("/shimelinux-kwin-script.js")?.use { input ->
                scriptFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            scripting = dbus?.getRemoteObject(
                "org.kde.KWin",
                "/Scripting",
                KWinScripting::class.java
            )

            script = scripting?.loadScript(scriptFile.absolutePath, "shimelinux-kwin-script")?.let { id ->
                dbus?.getRemoteObject(
                    "org.kde.KWin",
                    "/Scripting/Script$id",
                    KWinScript::class.java
                )
            }

            script?.run()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            // Ensure the DBus connection is closed when the program shuts down
            Runtime.getRuntime().addShutdownHook(shutdownThread)
        }
    }

    override fun tick() {
        super.tick()

        val activeWindow = activeWindow
        if (activeWindow != null && isIE(activeWindow)) {
            activeIE.set(activeWindow.bounds)
            activeIETitle = activeWindow.title
            activeIE.isVisible = true
        } else {
            activeIE.isVisible = false
            activeIE.set(Rectangle(0, 0, 0, 0))
            activeIETitle = ""
        }
    }

    override fun moveActiveIE(point: Point) {
        windowPosition = point
    }

    override fun restoreIE() {
        restoreWindows = true
    }

    override fun refreshCache() {
        windowCache.clear()
    }

    override fun dispose() {
        try {
            script?.stop()
            scripting?.unloadScript("shimelinux-kwin-script")
            dbus?.disconnect()

            if (!isShuttingDown) {
                Runtime.getRuntime().removeShutdownHook(shutdownThread)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun handleShutdown() {
        isShuttingDown = true
        dispose()
    }

    private fun isIE(window: Window) = windowCache.getOrPut(window.title) {
        var blacklistInUse = false
        val blacklist = getProperty("InteractiveWindowsBlacklist", "").split("/")
        for (title in blacklist) {
            if (title.isNotBlank()) {
                blacklistInUse = true
                if (window.title.contains(title, true)) {
                    windowCache[window.title] = false
                    return@getOrPut false
                }
            }
        }

        var whitelistInUse = false
        val whitelist = getProperty("InteractiveWindows", "").split("/")
        for (title in whitelist) {
            if (title.isNotBlank()) {
                whitelistInUse = true
                if (window.title.contains(title, true)) {
                    windowCache[window.title] = true
                    return@getOrPut true
                }
            }
        }

        return@getOrPut blacklistInUse || !whitelistInUse
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
            caption: String,
            x: Int,
            y: Int,
            width: Int,
            height: Int
        )

        fun resetActiveWindow()

        fun getWindowPosition(): Map<String, Int>

        fun getRestoreWindows(): Boolean
    }

    inner class KWinClientImpl : KWinClient {
        override fun setActiveWindow(
            caption: String,
            x: Int,
            y: Int,
            width: Int,
            height: Int
        ) {
            activeWindow = Window(
                caption,
                Rectangle(x, y, width, height)
            )
        }

        override fun resetActiveWindow() {
            activeWindow = null
        }

        override fun getWindowPosition(): Map<String, Int> {
            val windowPosition = windowPosition.also {
                this@KdeEnvironment.windowPosition = null
            }

            return if (windowPosition == null) {
                mapOf(
                    "hasValue" to 0,
                    "x" to -1,
                    "y" to -1
                )
            } else {
                mapOf(
                    "hasValue" to 1,
                    "x" to windowPosition.x,
                    "y" to windowPosition.y
                )
            }
        }

        override fun getRestoreWindows(): Boolean {
            return restoreWindows.also {
                restoreWindows = false
            }
        }

        override fun getObjectPath() = "/KWinClient"
    }

    data class Window(val title: String, val bounds: Rectangle)
}
