/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.image

import hqx.Hqx_2x
import hqx.Hqx_3x
import hqx.Hqx_4x
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
        var filter = filter
        var width = source.width
        var height = source.height
        var workingImage: BufferedImage? = null

        var effectiveScaling = scaling
        if (filter == Filter.HQX && scaling > 1.0) {
            val buffer: IntArray
            var rgbValues = source.getRGB(0, 0, width, height, null, 0, width)

            when (scaling) {
                4.0, 8.0 -> {
                    width *= 4
                    height *= 4
                    buffer = IntArray(width * height)
                    Hqx_4x.hq4x_32_rb(rgbValues, buffer, width / 4, height / 4)
                    rgbValues = buffer
                    effectiveScaling = if (scaling > 4.0) 2.0 else 1.0
                }

                3.0, 6.0 -> {
                    width *= 3
                    height *= 3
                    buffer = IntArray(width * height)
                    Hqx_3x.hq3x_32_rb(rgbValues, buffer, width / 3, height / 3)
                    rgbValues = buffer
                    effectiveScaling = if (scaling > 4.0) 2.0 else 1.0
                }

                2.0 -> {
                    width *= 2
                    height *= 2
                    buffer = IntArray(width * height)
                    Hqx_2x.hq2x_32_rb(rgbValues, buffer, width / 2, height / 2)
                    rgbValues = buffer
                    effectiveScaling = 1.0
                }

                else -> {
                    filter = Filter.NEAREST_NEIGHBOR
                }
            }

            // Apply the changes if hqx is still on
            if (filter == Filter.HQX) {
                workingImage = BufferedImage(
                    (width * effectiveScaling).roundToInt(),
                    (height * effectiveScaling).roundToInt(),
                    BufferedImage.TYPE_INT_ARGB_PRE
                )
                var srcColIndex = 0
                var srcRowIndex = 0

                for (y in 0 until workingImage.height) {
                    for (x in 0 until workingImage.width) {
                        workingImage.setRGB(x, y, rgbValues[srcColIndex / effectiveScaling.toInt()])
                        srcColIndex++
                    }

                    // Reset the srcColIndex to re-use the same indexes and stretch horizontally
                    srcRowIndex++
                    if (srcRowIndex.toDouble() != effectiveScaling) {
                        srcColIndex -= workingImage.width
                    } else {
                        srcRowIndex = 0
                    }
                }
            }
        }

        width = (width * effectiveScaling).roundToInt()
        height = (height * effectiveScaling).roundToInt()

        val copy = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE)

        val g2d = copy.createGraphics()
        val renderHint = if (filter == Filter.BICUBIC) {
            RenderingHints.VALUE_INTERPOLATION_BICUBIC
        } else {
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        }

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, renderHint)
        g2d.drawImage(workingImage ?: source, 0, 0, width, height, null)
        g2d.dispose()

        return copy
    }

    enum class Filter { NEAREST_NEIGHBOR, BICUBIC, HQX }
}
