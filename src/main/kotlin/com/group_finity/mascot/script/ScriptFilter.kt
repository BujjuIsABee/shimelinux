/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.script

import org.openjdk.nashorn.api.scripting.ClassFilter

class ScriptFilter : ClassFilter {
    override fun exposeToScripts(className: String): Boolean {
        return className.startsWith("com.group_finity.mascot")
    }
}
