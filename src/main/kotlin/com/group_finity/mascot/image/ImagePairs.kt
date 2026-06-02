package com.group_finity.mascot.image

import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

object ImagePairs {
    private val imagePairs = ConcurrentHashMap<String, ImagePair>()

    fun load(fileName: String, imagePair: ImagePair) {
        if (!imagePairs.containsKey(fileName)) {
            imagePairs[fileName] = imagePair
        }
    }

    fun getImagePair(fileName: String): ImagePair? {
        return if (imagePairs.containsKey(fileName)) imagePairs[fileName] else null
    }

    fun contains(fileName: String): Boolean {
        return imagePairs.containsKey(fileName)
    }

    fun clear() {
        imagePairs.clear()
    }

    fun removeAll(searchTerm: String) {
        if (imagePairs.isEmpty()) {
            return
        }

        val iterator = imagePairs.keys.iterator()
        while (iterator.hasNext()) {
            val fileName = iterator.next()
            if (searchTerm == Paths.get(fileName).getName(2).toString()) {
                imagePairs.remove(fileName)
            }
        }
    }

    fun getImage(fileName: String, isLookRight: Boolean): MascotImage? {
        return if (imagePairs.containsKey(fileName)) imagePairs[fileName]!!.getImage(isLookRight) else null
    }
}