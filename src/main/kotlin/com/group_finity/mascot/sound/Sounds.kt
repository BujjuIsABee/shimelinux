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

package com.group_finity.mascot.sound

import com.group_finity.mascot.getProperty
import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent

object Sounds {
    private val sounds = mutableListOf<Sound>()

    var isMuted: Boolean
        get() = !getProperty("Sounds", true)
        set(value) {
            if (value) {
                for (clip in sounds.mapNotNull { it.clip }) {
                    clip.stop()
                }
            }
        }

    fun load(name: String, volume: Float) {
        sounds.add(Sound(name, volume, null))
    }

    fun contains(name: String) = sounds.any { name == it.name + it.volume }

    fun getSound(name: String) = sounds.find { name == it.name + it.volume }?.let { getClip(it) }

    fun getSoundsIgnoringVolume(name: String) = sounds.filter { it.name == name }.mapNotNull { it.clip }

    private fun getClip(sound: Sound): Clip {
        sound.clip?.let { return it }

        val clip = AudioSystem.getClip()
        AudioSystem.getAudioInputStream(File(sound.name)).use { clip.open(it) }

        (clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl).value = sound.volume

        clip.addLineListener {
            if (it.type == LineEvent.Type.STOP) {
                clip.stop()
                clip.close()
                sounds.remove(sound)
            }
        }

        return clip.also { sound.clip = it }
    }

    data class Sound(val name: String, val volume: Float, var clip: Clip?)
}