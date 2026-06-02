/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.image

import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

object ImagePairs {
    private val imagePairs = ConcurrentHashMap<String, ImagePair>()

    fun load(fileName: String, imagePair: ImagePair) {
        imagePairs.putIfAbsent(fileName, imagePair)
    }

    fun getImagePair(fileName: String): ImagePair? = imagePairs[fileName]

    fun contains(fileName: String): Boolean = imagePairs.containsKey(fileName)

    fun getImage(fileName: String, isLookRight: Boolean): MascotImage? = imagePairs[fileName]?.getImage(isLookRight)

    fun clear() {
        imagePairs.clear()
    }

    fun removeAll(searchTerm: String) {
        imagePairs.entries.removeIf { entry ->
            searchTerm == Paths.get(entry.key).getName(2).toString()
        }
    }
}
