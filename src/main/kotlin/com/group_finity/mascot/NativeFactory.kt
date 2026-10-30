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

package com.group_finity.mascot

import com.group_finity.mascot.environment.Environment
import com.group_finity.mascot.image.NativeImage
import com.group_finity.mascot.image.TranslucentWindow
import java.awt.image.BufferedImage

import io.github.bujjuisabee.shimelinux.generic.NativeFactoryImpl as GenericNativeFactory
import io.github.bujjuisabee.shimelinux.kde.NativeFactoryImpl as KdeNativeFactory
import io.github.bujjuisabee.shimelinux.wayland.NativeFactoryImpl as WaylandNativeFactory
import io.github.bujjuisabee.shimelinux.virtual.NativeFactoryImpl as VirtualNativeFactory

/**
 * The name of the environment chosen automatically based on [Platform.currentDesktop].
 */
val defaultEnvironment: String = when {
    Platform.currentDesktop.contains("kde") -> "kde"
    Platform.isTilingWindowManager && Platform.supportsWaylandLayerSurfaces -> "wayland"
    else -> "generic"
}

/**
 * The name of the current environment.
 */
val currentEnvironment: String
    get() = when (getProperty("Environment", "")) {
        "kde" if (Platform.currentDesktop.contains("kde")) -> "kde"
        "wayland" if (Platform.supportsWaylandLayerSurfaces) -> "wayland"
        "generic" -> "generic"
        "virtual" -> "virtual"
        else -> defaultEnvironment
    }

/**
 * Whether mascots and popup menus are being displayed on Wayland layer surfaces.
 */
val usingWaylandLayerSurfaces: Boolean
    get() = currentEnvironment == "wayland" ||
            currentEnvironment == "kde" &&
            Platform.supportsWaylandLayerSurfaces &&
            getProperty("UseWaylandLayersKde", true)

/**
 * A factory for platform-specific objects.
 *
 * @author Bujju
 */
abstract class NativeFactory {
    /**
     * Gets a platform-specific [Environment].
     */
    abstract val environment: Environment

    /**
     * Gets a platform-specific [TranslucentWindow].
     */
    abstract fun newTranslucentWindow(): TranslucentWindow

    /**
     * Gets a platform-specific [NativeImage] from the given [src] image.
     */
    abstract fun newNativeImage(src: BufferedImage): NativeImage

    companion object {
        /**
         * The shared native factory instance.
         */
        @JvmStatic
        lateinit var instance: NativeFactory

        init {
            resetInstance()
        }

        /**
         * Updates [instance] to match the [currentEnvironment] environment.
         */
        @JvmStatic
        fun resetInstance() {
            instance = when (currentEnvironment) {
                "kde" -> KdeNativeFactory()
                "wayland" -> WaylandNativeFactory()
                "virtual" -> VirtualNativeFactory()
                else -> GenericNativeFactory()
            }
        }
    }
}
