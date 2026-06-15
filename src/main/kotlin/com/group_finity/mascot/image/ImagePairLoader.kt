/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.image

import java.awt.Color
import java.awt.Point
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO
import kotlin.io.path.inputStream
import kotlin.math.roundToInt

object ImagePairLoader {
    fun load(leftPath: Path, rightPath: Path?, center: Point, scaling: Double, filter: Filter, opacity: Double) {
        if (ImagePairs.contains(leftPath.toString() + (rightPath?.toString() ?: ""))) return

        // Load left image
        val leftImage = leftPath.inputStream().use {
            scale(premultiply(ImageIO.read(it), opacity), scaling, filter)
        }

        // Load right image
        val rightImage = rightPath?.inputStream()?.use {
            scale(premultiply(ImageIO.read(it), opacity), scaling, filter)
        } ?: flip(leftImage)

        val leftCenter = Point((center.x * scaling).roundToInt(), (center.y * scaling).roundToInt())
        val rightCenter = Point(rightImage.width - (center.x * scaling).roundToInt(), (center.y * scaling).roundToInt())
        val imagePair = ImagePair(MascotImage(leftImage, leftCenter), MascotImage(rightImage, rightCenter))
        ImagePairs.load(leftPath.toString() + (rightPath?.toString() ?: ""), imagePair)
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
        val hints = when (filter) {
            Filter.NEAREST_NEIGHBOR -> RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
            Filter.BICUBIC -> RenderingHints.VALUE_INTERPOLATION_BICUBIC
        }

        val scaledWidth = (source.width * scaling).toInt()
        val scaledHeight = (source.height * scaling).toInt()

        val result = BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = result.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hints)
        graphics.drawImage(source, 0, 0, scaledWidth, scaledHeight, null)
        graphics.dispose()

        return result
    }

    enum class Filter { NEAREST_NEIGHBOR, BICUBIC }
}
