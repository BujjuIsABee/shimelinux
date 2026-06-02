/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.image

import com.group_finity.mascot.NativeFactory
import java.awt.Dimension
import java.awt.Point
import java.awt.image.BufferedImage

class MascotImage(val image: NativeImage, val center: Point, val size: Dimension) {
    constructor(image: BufferedImage, center: Point) :
        this(NativeFactory.instance.newNativeImage(image), center, Dimension(image.width, image.height))
}
