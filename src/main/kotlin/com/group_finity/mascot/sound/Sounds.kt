/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.sound

import com.group_finity.mascot.Main
import java.util.concurrent.ConcurrentHashMap
import javax.sound.sampled.Clip

object Sounds {
    private val sounds = ConcurrentHashMap<String, Clip>()

    var isMuted
        get() = !Main.instance.properties.getProperty("Sounds", "true").toBoolean()
        set(value) {
            if (value) {
                for (sound in sounds.values) {
                    sound.stop()
                }
            }
        }

    fun load(fileName: String, clip: Clip) {
        sounds.putIfAbsent(fileName, clip)
    }

    fun contains(fileName: String) = sounds.containsKey(fileName)

    fun getSound(fileName: String) = sounds[fileName]

    fun getSoundsIgnoringVolume(fileName: String) = sounds.filter { it.key.startsWith(fileName) }.values.toMutableList()
}
