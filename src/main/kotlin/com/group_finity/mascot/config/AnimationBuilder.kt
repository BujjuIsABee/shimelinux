/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.config

import com.group_finity.mascot.Main
import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.animation.Pose
import com.group_finity.mascot.exception.AnimationInstantiationException
import com.group_finity.mascot.exception.ConfigurationException
import com.group_finity.mascot.exception.VariableException
import com.group_finity.mascot.hotspot.Hotspot
import com.group_finity.mascot.image.ImagePairLoader
import com.group_finity.mascot.script.Variable
import com.group_finity.mascot.sound.SoundLoader
import java.awt.Dimension
import java.awt.Point
import java.awt.Rectangle
import java.awt.geom.Ellipse2D
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.math.abs
import kotlin.math.roundToInt

class AnimationBuilder(private val schema: ResourceBundle, animationNode: Entry, private val imageSet: String) {
    private val condition = animationNode.getAttribute(schema.getString("Condition")) ?: "true"
    private val poses = ArrayList<Pose>()
    private val hotspots = ArrayList<Hotspot>()
    private val turn = animationNode.getAttribute(schema.getString("IsTurn")) ?: "false"

    init {
        log.log(Level.INFO, "Loading animation")

        for (frameNode in animationNode.selectChildren(schema.getString("Pose"))) {
            try {
                poses.add(loadPose(frameNode))
            } catch (e: Exception) {
                log.log(Level.SEVERE, "Failed to load pose: ${frameNode.attributes}", e)
                throw ConfigurationException(Main.instance.languageBundle.getString("FailedLoadPoseErrorMessage") + ": ${frameNode.attributes}", e)
            }
        }

        for (frameNode in animationNode.selectChildren(schema.getString("Hotspot"))) {
            try {
                hotspots.add(loadHotspot(frameNode))
            } catch (e: Exception) {
                log.log(Level.SEVERE, "Failed to load hotspot: ${frameNode.attributes}", e)
                throw ConfigurationException(Main.instance.languageBundle.getString("FailedLoadHotspotErrorMessage") + ": ${frameNode.attributes}", e)
            }
        }

        log.log(Level.INFO, "Finished loading animation")
    }

    private fun loadPose(frameNode: Entry): Pose {
        var imageText = frameNode.getAttribute(schema.getString("Image"))
        imageText = if (!imageText.isNullOrEmpty()) "/img/$imageSet/$imageText" else null

        var imageRightText = frameNode.getAttribute(schema.getString("ImageRight"))
        imageRightText = if (!imageRightText.isNullOrEmpty()) "/img/$imageSet/$imageRightText" else null

        val anchorText = checkNotNull(frameNode.getAttribute(schema.getString("ImageAnchor")))
        val moveText = checkNotNull(frameNode.getAttribute(schema.getString("Velocity")))
        val durationText = checkNotNull(frameNode.getAttribute(schema.getString("Duration")))
        var soundText = frameNode.getAttribute(schema.getString("Sound"))
        val volumeText = frameNode.getAttribute(schema.getString("Volume")) ?: "0"

        val opacity = Main.instance.properties.getProperty("Opacity", "1.0").toDouble()
        val scaling = Main.instance.properties.getProperty("Scaling", "1.0").toDouble()

        val filterText = Main.instance.properties.getProperty("Filter", "false")
        val filter = if (
            filterText.equals("true", true) ||
            filterText.equals("hqx", true)
        ) {
            ImagePairLoader.Filter.HQX
        } else if (filterText.equals("bicubic", true)) {
            ImagePairLoader.Filter.BICUBIC
        } else {
            ImagePairLoader.Filter.NEAREST_NEIGHBOR
        }

        if (imageText != null) {
            val anchorCoordinates = anchorText.split(",")
            val anchor = Point(anchorCoordinates[0].toInt(), anchorCoordinates[1].toInt())

            try {
                ImagePairLoader.load(imageText, imageRightText, anchor, scaling, filter, opacity)
            } catch (e: Exception) {
                log.log(Level.SEVERE, "Failed to load image: $imageText, ${imageRightText ?: ""}", e)
                throw ConfigurationException(Main.instance.languageBundle.getString("FailedLoadImageErrorMessage") + ": $imageText, ${imageRightText ?: ""}", e)
            }
        }

        val moveCoordinates = moveText.split(",")
        var moveX = moveCoordinates[0].toInt()
        var moveY = moveCoordinates[1].toInt()

        moveX = if (abs(moveX) > 0 && abs(moveX * scaling) < 1) (if (moveX > 0) 1 else -1) else (moveX * scaling).roundToInt()
        moveY = if (abs(moveY) > 0 && abs(moveY * scaling) < 1) (if (moveY > 0) 1 else -1) else (moveY * scaling).roundToInt()

        val duration = durationText.toInt()

        if (soundText != null) {
            try {
                soundText = if (Path("/sound/$soundText").exists()) {
                    "/sound/$soundText"
                } else if (Path("/sound/$imageSet/$soundText").exists()) {
                    "/sound/$imageSet/$soundText"
                } else {
                    "/img/$imageSet/sound/$soundText"
                }

                SoundLoader.load(soundText, volumeText.toFloat())
                soundText += volumeText.toFloat()
            } catch (e: Exception) {
                log.log(Level.SEVERE, "Failed to load sound: $soundText", e)
                throw ConfigurationException(Main.instance.languageBundle.getString("FailedLoadSoundErrorMessage") + ": $soundText", e)
            }
        }

        val pose = Pose(
            if (imageText != null) Path(imageText) else null,
            if (imageRightText != null) Path(imageRightText) else null,
            moveX, moveY, duration, soundText
        )

        log.log(Level.INFO, "Loaded pose: $pose")

        return pose
    }

    private fun loadHotspot(frameNode: Entry): Hotspot {
        val shapeText = checkNotNull(frameNode.getAttribute(schema.getString("Shape")))
        val originText = checkNotNull(frameNode.getAttribute(schema.getString("Origin")))
        val sizeText = checkNotNull(frameNode.getAttribute(schema.getString("Size")))
        val behaviorText = frameNode.getAttribute(schema.getString("Behaviour"))

        val scaling = Main.instance.properties.getProperty("Scaling", "1.0").toDouble()

        val originCoordinates = originText.split(",")
        val origin = Point(
            (originCoordinates[0].toInt() * scaling).roundToInt(),
            (originCoordinates[1].toInt() * scaling).roundToInt()
        )

        val sizeCoordinates = sizeText.split(",")
        val size = Dimension(
            (sizeCoordinates[0].toInt() * scaling).roundToInt(),
            (sizeCoordinates[1].toInt() * scaling).roundToInt()
        )

        val shape = if (shapeText.equals("Rectangle", true)) {
            Rectangle(origin, size)
        } else if (shapeText.equals("Ellipse", true)) {
            Ellipse2D.Float(
                origin.x.toFloat(),
                origin.y.toFloat(),
                size.width.toFloat(),
                size.height.toFloat()
            )
        } else {
            log.log(Level.SEVERE, "Failed to load hotspot shape: $shapeText")
            throw ConfigurationException(Main.instance.languageBundle.getString("HotspotShapeNotSupportedErrorMessage") + ": $shapeText")
        }

        val hotspot = Hotspot(behaviorText, shape)

        log.log(Level.INFO, "Loaded hotspot: $hotspot")

        return hotspot
    }

    fun buildAnimation(): Animation {
        try {
            return Animation(
                Variable.parse(condition)!!,
                poses.toTypedArray(),
                hotspots.toTypedArray(),
                turn.toBoolean()
            )
        } catch (e: VariableException) {
            throw AnimationInstantiationException(Main.instance.languageBundle.getString("FailedConditionEvaluationErrorMessage"), e)
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
    }
}
