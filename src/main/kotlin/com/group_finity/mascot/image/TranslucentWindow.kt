package com.group_finity.mascot.image

import java.awt.Component

interface TranslucentWindow {
    fun asComponent(): Component

    fun setImage(image: NativeImage)

    fun updateImage()

    fun setAlwaysOnTop(onTop: Boolean)

    fun dispose()
}