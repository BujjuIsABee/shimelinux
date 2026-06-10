/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.image

class ImagePair(val leftImage: MascotImage, val rightImage: MascotImage) {
    fun getImage(isLookRight: Boolean) = if (isLookRight) rightImage else leftImage
}
