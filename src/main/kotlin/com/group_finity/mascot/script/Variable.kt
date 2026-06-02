/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.script

abstract class Variable {
    abstract fun init()

    abstract fun initFrame()

    abstract fun get(variables: VariableMap): Any?

    companion object {
        fun parse(source: String?): Variable? = if (source == null) {
            null
        } else if (source.startsWith($$"${") && source.endsWith("}")) {
            Script(source.substring(2, source.length - 1), false)
        } else if (source.startsWith("#{") && source.endsWith("}")) {
            Script(source.substring(2, source.length - 1), true)
        } else {
            Constant(parseConstant(source))
        }

        private fun parseConstant(source: String?): Any? = when (source) {
            null, "null" -> null
            "true" -> true
            "false" -> false
            else -> source.toDoubleOrNull() ?: source
        }
    }
}
