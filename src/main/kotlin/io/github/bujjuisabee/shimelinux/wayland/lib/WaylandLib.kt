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

package io.github.bujjuisabee.shimelinux.wayland.lib

import com.group_finity.mascot.Main
import com.group_finity.mascot.getProperty
import com.group_finity.mascot.loadResource
import com.group_finity.mascot.localize
import java.io.File
import kotlin.io.outputStream
import kotlin.system.exitProcess

/**
 * A foreign function interface for creating and managing Wayland layer surfaces.
 *
 * @author Bujju
 */
object WaylandLib {
    val senderPtr: Long

    init {
        try {
            // Copy the library to temp file
            val libFile = File.createTempFile("libshimelinux_wayland", ".so")
            libFile.deleteOnExit()
            checkNotNull(loadResource("lib/libshimelinux_wayland.so")).use { input ->
                libFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Load the library
            System.load(libFile.absolutePath)

            senderPtr = init(getProperty("MascotLayerWayland", 3))
        } catch (e: Throwable) {
            Main.showError(localize("WaylandLibraryErrorMessage"), e)
            exitProcess(0)
        }
    }

    external fun init(mascotLayer: Int): Long

    external fun createLayer(senderPtr: Long, id: Int, mouseEventReceiver: MouseEventReceiver, useInputRegion: Boolean)

    external fun setBounds(senderPtr: Long, id: Int, x: Int, y: Int, width: Int, height: Int)

    external fun setImage(senderPtr: Long, id: Int, rgb: IntArray)

    external fun setCursor(senderPtr: Long, useHand: Boolean)

    external fun dispose(senderPtr: Long, id: Int)
}
