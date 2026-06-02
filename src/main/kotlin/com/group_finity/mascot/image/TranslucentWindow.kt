/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.image

import java.awt.Component

interface TranslucentWindow {
    fun asComponent(): Component

    fun setImage(image: NativeImage)

    fun updateImage()

    fun setAlwaysOnTop(onTop: Boolean)

    fun dispose()
}
