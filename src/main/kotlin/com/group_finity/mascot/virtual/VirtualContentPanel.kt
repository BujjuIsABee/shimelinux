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

package com.group_finity.mascot.virtual

import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Image
import java.awt.event.ComponentEvent
import java.awt.event.ComponentListener
import javax.swing.JPanel

class VirtualContentPanel(
    preferredSize: Dimension,
    background: Color,
    image: Image?,
    private val mode: Mode
) : JPanel() {
    private var resizedImage: Image? = null

    init {
        this.layout = null
        this.preferredSize = preferredSize
        this.background = background
        this.resizedImage = image

        addComponentListener(object : ComponentListener {
            override fun componentResized(e: ComponentEvent) {
                if (image != null) {
                    if (mode == Mode.STRETCH) {
                        resizedImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH)
                    } else if (mode != Mode.CENTER) {
                        val scaledWidth = width / image.getWidth(null).toDouble()
                        val scaledHeight = height / image.getHeight(null).toDouble()
                        val factor = if (mode == Mode.FIT) {
                            scaledWidth.coerceAtMost(scaledHeight)
                        } else {
                            scaledWidth.coerceAtLeast(scaledHeight)
                        }

                        resizedImage = image.getScaledInstance(
                            (factor * image.getWidth(null)).toInt(),
                            (factor * image.getHeight(null)).toInt(),
                            Image.SCALE_SMOOTH
                        )
                    }
                }
            }

            override fun componentMoved(e: ComponentEvent) {}

            override fun componentShown(e: ComponentEvent) {}

            override fun componentHidden(e: ComponentEvent) {}
        })
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val resizedImage = resizedImage
        if (resizedImage != null) {
            val (width, height) = when (mode) {
                Mode.STRETCH -> 0 to 0
                Mode.CENTER -> (if (resizedImage.getWidth(null) > getWidth()) {
                    (resizedImage.getWidth(null) - getWidth()) / -2
                } else {
                    (getWidth() - resizedImage.getWidth(null)) / 2
                }) to (if (resizedImage.getHeight(null) > getHeight()) {
                    (resizedImage.getHeight(null) - getHeight()) / -2
                } else {
                    (getHeight() - resizedImage.getHeight(null)) / 2
                })

                else -> (width - resizedImage.getWidth(null)) / 2 to (height - resizedImage.getHeight(null)) / 2
            }

            g.drawImage(resizedImage, width, height, null)
        }
    }

    enum class Mode { CENTER, FIT, STRETCH, FILL }
}
