/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.sound

import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent

object SoundLoader {
    fun load(name: String, volume: Float) {
        if (Sounds.contains(name + volume)) return

        // Load clip
        val clip = AudioSystem.getClip()
        AudioSystem.getAudioInputStream(File(name)).use { clip.open(it) }

        // Set volume
        (clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl).value = volume

        // Handle stop event
        clip.addLineListener {
            if (it.type == LineEvent.Type.STOP) {
                (it.line as Clip).stop()
            }
        }

        Sounds.load(name + volume, clip)
    }
}
