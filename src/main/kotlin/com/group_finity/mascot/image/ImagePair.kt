package com.group_finity.mascot.image

class ImagePair(val leftImage: MascotImage, val rightImage: MascotImage) {
    fun getImage(isLookRight: Boolean): MascotImage {
        return if (isLookRight) rightImage else leftImage
    }
}