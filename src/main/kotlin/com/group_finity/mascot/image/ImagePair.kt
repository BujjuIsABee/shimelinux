/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.image

class ImagePair(val leftImage: MascotImage, val rightImage: MascotImage) {
    fun getImage(isLookRight: Boolean): MascotImage {
        return if (isLookRight) rightImage else leftImage
    }
}
