package com.group_finity.mascot.script

class Constant(private val value: Any?) : Variable() {
    override fun init() {
    }

    override fun initFrame() {
    }

    override fun get(variables: VariableMap): Any? {
        return value
    }

    override fun toString(): String {
        return value?.toString() ?: "null"
    }
}