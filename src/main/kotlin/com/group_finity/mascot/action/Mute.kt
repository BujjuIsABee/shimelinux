/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.script.VariableMap
import com.group_finity.mascot.sound.Sounds
import java.util.ResourceBundle

class Mute(
    schema: ResourceBundle,
    params: VariableMap
) : InstantAction(schema, params) {
    @Suppress("UNCHECKED_CAST")
    private val sound: String?
        get() = eval(schema.getString(PARAMETER_SOUND), String::class.java as Class<String?>, DEFAULT_SOUND)

    override fun apply() {
        if (sound != null) {
            var clips = Sounds.getSoundsIgnoringVolume("/sound/$sound")
            if (clips.isNotEmpty()) {
                for (clip in clips) {
                    if (clip.isRunning) {
                        clip.stop()
                    }
                }
            } else {
                clips = Sounds.getSoundsIgnoringVolume("/sound/${mascot.imageSet}/$sound")
                if (clips.isNotEmpty()) {
                    for (clip in clips) {
                        if (clip.isRunning) {
                            clip.stop()
                        }
                    }
                } else {
                    clips = Sounds.getSoundsIgnoringVolume("/img/${mascot.imageSet}/sound/$sound")
                    for (clip in clips) {
                        if (clip.isRunning) {
                            clip.stop()
                        }
                    }
                }
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
        private val DEFAULT_SOUND: String? = null
    }
}
