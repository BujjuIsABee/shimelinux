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

package com.group_finity.mascot.action

import com.group_finity.mascot.getPath
import com.group_finity.mascot.script.VariableMap
import com.group_finity.mascot.sound.Sounds
import java.util.ResourceBundle

class Mute(
    schema: ResourceBundle,
    params: VariableMap
) : InstantAction(schema, params) {
    private val sound: String?
        get() = eval(schema.getString(PARAMETER_SOUND), DEFAULT_SOUND)

    override fun apply() {
        val sound = sound
        if (sound != null) {
            val clips = Sounds.getSoundsIgnoringVolume(getPath("sound", sound).toString())
                .ifEmpty { Sounds.getSoundsIgnoringVolume(getPath("sound", mascot.imageSet, sound).toString()) }
                .ifEmpty { Sounds.getSoundsIgnoringVolume(getPath("img", mascot.imageSet, "sound", sound).toString()) }

            if (clips.isNotEmpty()) {
                clips.filter { it.isRunning }.forEach { it.stop() }
            }
        } else {
            if (!Sounds.isMuted) {
                Sounds.isMuted = true
                Sounds.isMuted = false
            }
        }
    }

    companion object {
        const val PARAMETER_SOUND = "Sound"
        private val DEFAULT_SOUND = null
    }
}
