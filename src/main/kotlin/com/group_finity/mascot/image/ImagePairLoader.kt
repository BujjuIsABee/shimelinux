/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.image

import java.awt.Color
import java.awt.Point
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import kotlin.math.roundToInt

object ImagePairLoader {
    enum class Filter { NEAREST_NEIGHBOR, HQX, BICUBIC }

    fun load(leftPath: String, rightPath: String?, center: Point, scaling: Double, filter: Filter, opacity: Double) {
        if (ImagePairs.contains(leftPath + (rightPath ?: ""))) return

        // Load left image
        val leftStream = this::class.java.getResourceAsStream(leftPath)
        val leftImage = scale(premultiply(ImageIO.read(leftStream), opacity), scaling, filter)

        // Load right image
        val rightStream = if (rightPath != null) this::class.java.getResourceAsStream(leftPath) else null
        val rightImage = if (rightPath != null) {
            scale(premultiply(ImageIO.read(rightStream), opacity), scaling, filter)
        } else {
            flip(leftImage)
        }

        val leftCenter = Point((center.x * scaling).roundToInt(), (center.y * scaling).roundToInt())
        val rightCenter = Point(rightImage.width - (center.x * scaling).roundToInt(), (center.y * scaling).roundToInt())
        val imagePair = ImagePair(MascotImage(leftImage, leftCenter), MascotImage(rightImage, rightCenter))
        ImagePairs.load(leftPath + (rightPath ?: ""), imagePair)
    }

    private fun flip(source: BufferedImage): BufferedImage {
        val result = BufferedImage(
            source.width, source.height,
            if (source.type == BufferedImage.TYPE_CUSTOM) {
                BufferedImage.TYPE_INT_ARGB
            } else {
                source.type
            }
        )

        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                result.setRGB(result.width - x - 1, y, source.getRGB(x, y))
            }
        }

        return result
    }

    private fun premultiply(source: BufferedImage, opacity: Double): BufferedImage {
        val result = BufferedImage(
            source.width, source.height,
            if (source.type == BufferedImage.TYPE_CUSTOM) {
                BufferedImage.TYPE_INT_ARGB_PRE
            } else {
                source.type
            }
        )

        for (y in 0 until source.height) {
            for (x in 0 until source.width) {
                var color = Color(source.getRGB(x, y), true)
                val components = color.getComponents(null)

                components[3] *= opacity.toFloat()
                components[0] *= components[3]
                components[1] *= components[3]
                components[2] *= components[3]

                color = Color(components[0], components[1], components[2], components[3])
                result.setRGB(x, y, color.rgb)
            }
        }

        return result
    }

    private fun scale(source: BufferedImage, scaling: Double, filter: Filter): BufferedImage {
        // idk how to do this
        return source
    }
}
