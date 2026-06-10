/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.animation

import com.group_finity.mascot.Mascot
import com.group_finity.mascot.image.ImagePairs
import java.awt.Point
import java.nio.file.Path

class Pose(
    private val leftImage: Path?,
    private val rightImage: Path?,
    val dx: Int,
    val dy: Int,
    val duration: Int,
    val soundName: String?,
) {
    val imageName
        get() = "${leftImage ?: ""}${rightImage ?: ""}"
    val image
        get() = ImagePairs.getImagePair(imageName)

    fun next(mascot: Mascot) {
        mascot.anchor = Point(mascot.anchor.x + (if (mascot.isLookRight) -dx else dx), mascot.anchor.y + dy)
        mascot.image = ImagePairs.getImage(imageName, mascot.isLookRight)
        mascot.sound = soundName
    }

    override fun toString() = "Pose ($imageName,$dx,$dy,$duration,$soundName)"
}
