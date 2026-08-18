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

package com.group_finity.mascot

import java.io.InputStream
import kotlin.io.path.Path

/**
 * Gets a path within the config directory
 *
 * @author Bujju
 */
fun getPath(vararg paths: String) =
    Path(
        System.getenv("XDG_CONFIG_HOME")?.takeUnless { it.isBlank() } ?: System.getProperty("user.home"),
        ".config",
        "shimelinux",
        *paths
    )

/**
 * Gets a property and casts it to [T], or returns [defaultValue] if the property does not exist or the cast fails
 *
 * @author Bujju
 */
inline fun <reified T> getProperty(key: String, defaultValue: T): T =
    Main.properties.getProperty(key, defaultValue.toString()).let { value ->
        when (T::class) {
            Int::class -> value.toInt()
            Double::class -> value.toDouble()
            Boolean::class -> value.toBoolean()
            else -> value
        } as? T ?: defaultValue
    }

/**
 * Translates a string to the current language
 *
 * @author Bujju
 */
fun localize(key: String): String = Main.languageBundle.getString(key)

/**
 * Loads a resource and returns an input stream, or null if the resource does not exist
 *
 * @author Bujju
 */
fun loadResource(path: String): InputStream? = Main::class.java.getResourceAsStream("/$path")
