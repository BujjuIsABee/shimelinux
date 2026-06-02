package com.group_finity.mascot.script

import com.group_finity.mascot.Main
import com.group_finity.mascot.exception.VariableException
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import javax.script.Compilable
import javax.script.CompiledScript
import javax.script.ScriptException

class Script(private val source: String?, private val isClearAtInitFrame: Boolean) : Variable() {
    private val compiled: CompiledScript
    private var value: Any? = null

    init {
        try {
            compiled = (engine as Compilable).compile(source)
        } catch (e: ScriptException) {
            throw VariableException(Main.instance.languageBundle.getString("ScriptCompilationErrorMessage") + ": $source", e)
        }
    }

    override fun init() {
        value = null
    }

    override fun initFrame() {
        if (isClearAtInitFrame) {
            value = null
        }
    }

    @Synchronized
    override fun get(variables: VariableMap): Any? {
        if (value != null) {
            return value
        }

        try {
            value = compiled.eval(variables)
        } catch (e: ScriptException) {
            throw VariableException(Main.instance.languageBundle.getString("ScriptEvaluationErrorMessage") + ": $source", e)
        }

        return value
    }

    override fun toString(): String {
        return if (isClearAtInitFrame) "#{$source}" else $$"${$$source}"
    }

    companion object {
        private val engine = NashornScriptEngineFactory().getScriptEngine(ScriptFilter())
    }
}