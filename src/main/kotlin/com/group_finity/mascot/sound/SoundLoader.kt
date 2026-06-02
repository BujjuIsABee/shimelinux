package com.group_finity.mascot.sound

import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import javax.sound.sampled.LineEvent

object SoundLoader {
    fun load(name: String, volume: Float) {
        if (Sounds.contains(name + volume)) {
            return
        }

        // Load clip
        val stream = AudioSystem.getAudioInputStream(File(name))
        val clip = AudioSystem.getClip()
        clip.open(stream)

        // Set volume
        (clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl).value = volume

        // Handle stop event
        clip.addLineListener { event ->
            if (event.type == LineEvent.Type.STOP) {
                (event.line as Clip).stop()
            }
        }

        Sounds.load(name + volume, clip)
    }
}