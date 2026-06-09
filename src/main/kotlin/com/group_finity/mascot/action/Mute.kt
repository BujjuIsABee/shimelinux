/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.action

import com.group_finity.mascot.Main
import com.group_finity.mascot.script.VariableMap
import com.group_finity.mascot.sound.Sounds
import java.util.ResourceBundle

class Mute(
    schema: ResourceBundle,
    params: VariableMap
) : InstantAction(schema, params) {
    private val sound: String?
        get() = eval(schema.getString(PARAMETER_SOUND), String::class, DEFAULT_SOUND).takeUnless { it == DEFAULT_SOUND }

    override fun apply() {
        if (sound != null) {
            var clips = Sounds.getSoundsIgnoringVolume(Main.getPath("sound", sound!!).toString())
            if (clips.isNotEmpty()) {
                for (clip in clips) {
                    if (clip.isRunning) {
                        clip.stop()
                    }
                }
            } else {
                clips = Sounds.getSoundsIgnoringVolume(Main.getPath("sound", mascot.imageSet, sound!!).toString())
                if (clips.isNotEmpty()) {
                    for (clip in clips) {
                        if (clip.isRunning) {
                            clip.stop()
                        }
                    }
                } else {
                    clips = Sounds.getSoundsIgnoringVolume(Main.getPath("img", mascot.imageSet, "sound", sound!!).toString())
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
        private const val DEFAULT_SOUND = "null"
    }
}
