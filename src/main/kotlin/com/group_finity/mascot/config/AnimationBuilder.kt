/*
 * Copyright (c) 2026, Bujju
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *        following disclaimer.
 *     2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 *        following disclaimer in the documentation and/or other materials provided with the distribution.
 *     3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote
 *        products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.group_finity.mascot.config

import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.animation.Pose
import com.group_finity.mascot.exception.AnimationInstantiationException
import com.group_finity.mascot.exception.ConfigurationException
import com.group_finity.mascot.exception.VariableException
import com.group_finity.mascot.getPath
import com.group_finity.mascot.getProperty
import com.group_finity.mascot.hotspot.Hotspot
import com.group_finity.mascot.image.ImagePairLoader
import com.group_finity.mascot.localize
import com.group_finity.mascot.script.Variable
import com.group_finity.mascot.sound.SoundLoader
import java.awt.Dimension
import java.awt.Point
import java.awt.Rectangle
import java.awt.geom.Ellipse2D
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.io.path.exists
import kotlin.math.abs
import kotlin.math.roundToInt

class AnimationBuilder(
    private val schema: ResourceBundle,
    animationNode: Entry,
    private val imageSet: String
) {
    private val condition = animationNode.getAttribute(schema.getString("Condition")) ?: "true"
    private val poses = mutableListOf<Pose>()
    private val hotspots = mutableListOf<Hotspot>()
    private val turn = animationNode.getAttribute(schema.getString("IsTurn")) ?: "false"

    init {
        log.log(Level.INFO, "Loading animation")

        for (frameNode in animationNode.selectChildren(schema.getString("Pose"))) {
            try {
                poses.add(loadPose(frameNode))
            } catch (e: Exception) {
                log.log(Level.SEVERE, "Failed to load pose: ${frameNode.attributes}", e)
                throw ConfigurationException("FailedLoadPoseErrorMessage".localize() + ": ${frameNode.attributes}", e)
            }
        }

        for (frameNode in animationNode.selectChildren(schema.getString("Hotspot"))) {
            try {
                hotspots.add(loadHotspot(frameNode))
            } catch (e: Exception) {
                log.log(Level.SEVERE, "Failed to load hotspot: ${frameNode.attributes}", e)
                throw ConfigurationException("FailedLoadHotspotErrorMessage".localize() + ": ${frameNode.attributes}", e)
            }
        }

        log.log(Level.INFO, "Finished loading animation")
    }

    private fun loadPose(frameNode: Entry): Pose {
        val leftImageText = frameNode.getAttribute(schema.getString("Image"))
        val leftImagePath = if (!leftImageText.isNullOrEmpty()) getPath("img", imageSet, leftImageText) else null
        val rightImageText = frameNode.getAttribute(schema.getString("ImageRight"))
        val rightImagePath = if (!rightImageText.isNullOrEmpty()) getPath("img", imageSet, rightImageText) else null
        val anchorText = requireNotNull(frameNode.getAttribute(schema.getString("ImageAnchor")))
        val moveText = requireNotNull(frameNode.getAttribute(schema.getString("Velocity")))
        val durationText = requireNotNull(frameNode.getAttribute(schema.getString("Duration")))
        var soundText = frameNode.getAttribute(schema.getString("Sound"))
        val volumeText = frameNode.getAttribute(schema.getString("Volume")) ?: "0"

        val opacity = getProperty("Opacity", 1.0)
        val scaling = getProperty("Scaling", 1.0)

        val filterText = getProperty("Filter", "Nearest")
        val filter = when (filterText) {
            "Nearest" -> ImagePairLoader.Filter.NEAREST_NEIGHBOR
            "Bicubic" -> ImagePairLoader.Filter.BICUBIC
            "Hqx" -> ImagePairLoader.Filter.HQX
            else -> ImagePairLoader.Filter.NEAREST_NEIGHBOR
        }

        if (leftImagePath != null) {
            val anchorCoordinates = anchorText.split(',')
            val anchor = Point(anchorCoordinates[0].toInt(), anchorCoordinates[1].toInt())

            try {
                ImagePairLoader.load(leftImagePath, rightImagePath, anchor, scaling, filter, opacity)
            } catch (e: Exception) {
                log.log(Level.SEVERE, "Failed to load image: $leftImagePath, ${rightImagePath ?: ""}", e)
                throw ConfigurationException("FailedLoadImageErrorMessage".localize() + ": $leftImagePath, ${rightImagePath ?: ""}", e)
            }
        }

        val moveCoordinates = moveText.split(',')
        var moveX = moveCoordinates[0].toInt()
        var moveY = moveCoordinates[1].toInt()

        moveX = if (abs(moveX) > 0 && abs(moveX * scaling) < 1) (if (moveX > 0) 1 else -1) else (moveX * scaling).roundToInt()
        moveY = if (abs(moveY) > 0 && abs(moveY * scaling) < 1) (if (moveY > 0) 1 else -1) else (moveY * scaling).roundToInt()

        val duration = durationText.toInt()

        if (soundText != null) {
            try {
                soundText = getPath("sound", soundText).takeIf { it.exists() }?.toString() ?:
                    getPath("sound", imageSet, soundText).takeIf { it.exists() }?.toString() ?:
                    getPath("img", imageSet, "sound", soundText).toString()

                SoundLoader.load(soundText, volumeText.toFloat())
                soundText += volumeText.toFloat()
            } catch (e: Exception) {
                log.log(Level.SEVERE, "Failed to load sound: $soundText", e)
                throw ConfigurationException("FailedLoadSoundErrorMessage".localize() + ": $soundText", e)
            }
        }

        return Pose(leftImagePath, rightImagePath, moveX, moveY, duration, soundText).also {
            log.log(Level.INFO, "Loaded pose: $it")
        }
    }

    private fun loadHotspot(frameNode: Entry): Hotspot {
        val shapeText = requireNotNull(frameNode.getAttribute(schema.getString("Shape")))
        val originText = requireNotNull(frameNode.getAttribute(schema.getString("Origin")))
        val sizeText = requireNotNull(frameNode.getAttribute(schema.getString("Size")))
        val behaviorText = frameNode.getAttribute(schema.getString("Behavior"))

        val scaling = getProperty("Scaling", 1.0)

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

        val shape = when {
            shapeText.equals("Rectangle", true) -> Rectangle(origin, size)
            shapeText.equals("Ellipse", true) -> Ellipse2D.Float(
                origin.x.toFloat(),
                origin.y.toFloat(),
                size.width.toFloat(),
                size.height.toFloat()
            )

            else -> {
                log.log(Level.SEVERE, "Failed to load hotspot shape: $shapeText")
                throw ConfigurationException("HotspotShapeNotSupportedErrorMessage".localize() + ": $shapeText")
            }
        }

        return Hotspot(behaviorText, shape).also {
            log.log(Level.INFO, "Loaded hotspot: $it")
        }
    }

    fun buildAnimation(): Animation {
        try {
            return Animation(
                checkNotNull(Variable.parse(condition)),
                poses.toTypedArray(),
                hotspots.toTypedArray(),
                turn.toBoolean()
            )
        } catch (e: VariableException) {
            throw AnimationInstantiationException("FailedConditionEvaluationErrorMessage".localize(), e)
        }
    }

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
    }
}
