/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.script

import org.openjdk.nashorn.api.scripting.ClassFilter

class ScriptFilter : ClassFilter {
    override fun exposeToScripts(className: String): Boolean =
        className.startsWith("com.group_finity.mascot")
}
