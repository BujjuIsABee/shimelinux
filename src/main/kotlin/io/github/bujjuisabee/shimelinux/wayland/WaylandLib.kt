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

import com.group_finity.mascot.Main
import com.group_finity.mascot.loadResource
import com.group_finity.mascot.localize
import java.io.File
import kotlin.io.outputStream
import kotlin.system.exitProcess

/**
 * A foreign function interface used to create and manage Wayland layer surfaces
 *
 * @author Bujju
 */
object WaylandLib {
    init {
        val libFile = File.createTempFile("libshimelinux_wayland", ".so")
        libFile.deleteOnExit()
        loadResource("lib/libshimelinux_wayland.so")?.use { input ->
            libFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        try {
            System.load(libFile.absolutePath)
        } catch (e: Exception) {
            Main.showError(localize("SevereShimejiErrorErrorMessage"), e)
            exitProcess(0)
        }
    }

    /**
     * Creates a Wayland layer, returning a pointer to the event sender.
     */
    external fun createLayer(receiver: MouseEventReceiver): Long

    /**
     * Uses the [senderPtr] to send a SetBounds event to a layer
     */
    external fun setBounds(senderPtr: Long, x: Int, y: Int, width: Int, height: Int)

    /**
     * Uses the [senderPtr] to send a SetImage event to a layer
     */
    external fun setImage(senderPtr: Long, rgb: IntArray, updateMask: Boolean)

    /**
     * Uses the [senderPtr] to send a SetCursor event to a layer
     */
    external fun setCursor(senderPtr: Long, useHand: Boolean)

    /**
     * Uses the [senderPtr] to send a Dispose event to a layer
     */
    external fun dispose(senderPtr: Long)

    /**
     * Gets the bounds of the primary monitor
     */
    external fun getScreenRect(): IntArray

    interface MouseEventReceiver {
        fun updateCursor(
            leftPressed: Boolean,
            rightPressed: Boolean,
            leftReleased: Boolean,
            rightReleased: Boolean,
            positionX: Int,
            positionY: Int
        )
    }
}
