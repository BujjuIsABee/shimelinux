/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.script

class Constant(private val value: Any?) : Variable() {
    override fun init() {}

    override fun initFrame() {}

    override fun get(variables: VariableMap): Any? = value

    override fun toString(): String = value?.toString() ?: "null"
}
