package com.group_finity.mascot.script

abstract class Variable {
    abstract fun init()

    abstract fun initFrame()

    abstract fun get(variables: VariableMap): Any?

    companion object {
        fun parse(source: String?): Variable? {
            var result: Variable? = null

            if (source != null) {
                result = if (source.startsWith($$"${") && source.endsWith("}")) {
                    Script(source.substring(2, source.length - 1), false)
                } else if (source.startsWith("#{") && source.endsWith("}")) {
                    Script(source.substring(2, source.length - 1), true)
                } else {
                    Constant(parseConstant(source))
                }
            }

            return result
        }

        private fun parseConstant(source: String?): Any? {
            return when (source) {
                null, "null" -> null
                "true" -> true
                "false" -> false
                else -> source.toDoubleOrNull() ?: source
            }
        }
    }
}