/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.image

import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap

object ImagePairs {
    private val imagePairs = ConcurrentHashMap<String, ImagePair>()

    fun load(fileName: String, imagePair: ImagePair) {
        imagePairs.putIfAbsent(fileName, imagePair)
    }

    fun getImagePair(fileName: String) = imagePairs[fileName]

    fun contains(fileName: String) = imagePairs.containsKey(fileName)

    fun getImage(fileName: String, isLookRight: Boolean) = imagePairs[fileName]?.getImage(isLookRight)

    fun clear() {
        imagePairs.clear()
    }

    fun removeAll(searchTerm: String) {
        imagePairs.entries.removeIf { searchTerm == Paths.get(it.key).getName(2).toString() }
    }
}
