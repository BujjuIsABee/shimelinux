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
        } catch (e: Throwable) {
            Main.showError(localize("WaylandLibraryErrorMessage"), e)
            exitProcess(0)
        }
    }

    /**
     * Creates a Wayland layer surface with the given [id]. Mouse events will be sent to [mouseEventReceiver]. If
     * [useInputRegion] is true, mouse events on fully transparent pixels will be ignored.
     *
     * @param mascotLayer Options include:
     * - Background (`0`)
     * - Bottom (`1`)
     * - Top (`2`)
     * - Overlay (`3`)
     */
    external fun createLayer(id: Int, mascotLayer: Int, mouseEventReceiver: MouseEventReceiver, useInputRegion: Boolean)

    /**
     * Sets the bounds of the layer surface with the given [id] to the specified [x], [y], [width], and [height].
     */
    external fun setBounds(id: Int, x: Int, y: Int, width: Int, height: Int)

    /**
     * Displays the [rgb] pixel data on the layer surface with the given [id].
     */
    external fun setImage(id: Int, rgb: IntArray)

    /**
     * Sets the cursor to a hand if [useHand] is `true`, or the regular cursor if it is `false`.
     */
    external fun setCursor(useHand: Boolean)

    /**
     * Gets an [IntArray] with four elements corresponding to the logical position and size of the screen that layer
     * surfaces are displayed on.
     */
    external fun getScreenRect(): IntArray

    /**
     * Destroys the layer surface with the given [id].
     */
    external fun dispose(id: Int)
}
