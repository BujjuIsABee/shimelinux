package com.group_finity.mascot.script

import org.openjdk.nashorn.api.scripting.ClassFilter

class ScriptFilter : ClassFilter {
    override fun exposeToScripts(className: String): Boolean {
        return className.startsWith("com.group_finity.mascot")
    }
}