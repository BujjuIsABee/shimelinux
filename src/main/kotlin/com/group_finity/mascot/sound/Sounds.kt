/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.sound

import com.group_finity.mascot.Main
import java.util.concurrent.ConcurrentHashMap
import javax.sound.sampled.Clip

object Sounds {
    private val sounds = ConcurrentHashMap<String, Clip>()

    var isMuted: Boolean
        get() = !Main.instance.properties.getProperty("Sounds", "true").toBoolean()
        set(value) {
            if (value) {
                for (key in sounds.keys) {
                    sounds[key]!!.stop()
                }
            }
        }

    fun load(fileName: String, clip: Clip) {
        if (!sounds.containsKey(fileName)) {
            sounds[fileName] = clip
        }
    }

    fun contains(fileName: String): Boolean {
        return sounds.containsKey(fileName)
    }

    fun getSound(fileName: String): Clip? {
        return sounds[fileName]
    }

    fun getSoundsIgnoringVolume(fileName: String): ArrayList<Clip> {
        val result = ArrayList<Clip>(5)

        for (key in sounds.keys) {
            if (key.startsWith(fileName)) {
                result.add(sounds[key]!!)
            }
        }

        return result
    }
}
