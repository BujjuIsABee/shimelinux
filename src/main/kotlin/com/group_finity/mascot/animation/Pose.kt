/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.animation

import com.group_finity.mascot.Mascot
import com.group_finity.mascot.image.ImagePair
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
    val imageName: String
        get() = "${leftImage ?: ""}${rightImage ?: ""}"
    val image: ImagePair?
        get() = ImagePairs.getImagePair(imageName)

    fun next(mascot: Mascot) {
        mascot.anchor = Point(mascot.anchor.x + (if (mascot.isLookRight) -dx else dx), mascot.anchor.y + dy)
        mascot.image = ImagePairs.getImage(imageName, mascot.isLookRight)
        mascot.sound = soundName
    }

    override fun toString(): String = "Pose ($imageName,$dx,$dy,$duration,$soundName)"
}
