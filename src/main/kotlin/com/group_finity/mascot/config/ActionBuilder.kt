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

package com.group_finity.mascot.config

import com.group_finity.mascot.action.Action
import com.group_finity.mascot.action.Animate
import com.group_finity.mascot.action.Move
import com.group_finity.mascot.action.Select
import com.group_finity.mascot.action.Sequence
import com.group_finity.mascot.action.Stay
import com.group_finity.mascot.exception.ActionInstantiationException
import com.group_finity.mascot.exception.AnimationInstantiationException
import com.group_finity.mascot.exception.ConfigurationException
import com.group_finity.mascot.exception.VariableException
import com.group_finity.mascot.localize
import com.group_finity.mascot.script.Variable
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

class ActionBuilder(configuration: Configuration, actionNode: Entry, imageSet: String) : IActionBuilder {
    private val schema = configuration.schema
    val type = requireNotNull(actionNode.getAttribute(schema.getString("Type")))
    val name = actionNode.getAttribute(schema.getString("Name"))
    private val className = actionNode.getAttribute(schema.getString("Class"))
    private val params = linkedMapOf<String, String>()
    private val animationBuilders = mutableListOf<AnimationBuilder>()
    private val actionRefs = mutableListOf<IActionBuilder>()

    init {
        log.log(Level.INFO, "Loading action: $this")

        try {
            params.putAll(actionNode.attributes)

            for (node in actionNode.selectChildren(schema.getString("Animation"))) {
                animationBuilders.add(AnimationBuilder(schema, node, imageSet))
            }

            for (node in actionNode.children) {
                if (node.name == schema.getString("ActionReference")) {
                    actionRefs.add(ActionRef(configuration, node))
                } else if (node.name == schema.getString("Action")) {
                    actionRefs.add(ActionBuilder(configuration, node, imageSet))
                }
            }
        } catch (e: ConfigurationException) {
            throw ConfigurationException("${"FailedLoadActionErrorMessage".localize()} \"$name\" ${"ForShimeji".localize()} \"$imageSet.\"", e)
        }

        log.log(Level.INFO, "Finished loading action")
    }

    @Suppress("UNCHECKED_CAST")
    override fun buildAction(params: Map<String, String>): Action {
        return try {
            val variables = createVariables(params)
            val animations = createAnimations()
            val actions = createActions()

            when (type) {
                "Embedded" -> {
                    try {
                        val cls = Class.forName(className) as Class<out Action>

                        return runCatching {
                            cls.getConstructor(ResourceBundle::class.java, List::class.java, VariableMap::class.java)
                                .newInstance(schema, animations, variables)
                        }.recoverCatching {
                            cls.getConstructor(ResourceBundle::class.java, VariableMap::class.java)
                                .newInstance(schema, variables)
                        }.getOrElse {
                            cls.getConstructor().newInstance()
                        }
                    } catch (e: InstantiationException) {
                        throw ActionInstantiationException("FailedClassActionInitializeErrorMessage".localize() + " ($this)", e)
                    } catch (e: IllegalAccessException) {
                        throw ActionInstantiationException("CannotAccessClassActionErrorMessage".localize() + " ($this)", e)
                    } catch (e: ClassNotFoundException) {
                        throw ActionInstantiationException("ClassNotFoundErrorMessage".localize() + " ($this)", e)
                    }
                }

                "Move" -> Move(schema, animations, variables)
                "Stay" -> Stay(schema, animations, variables)
                "Animate" -> Animate(schema, animations, variables)
                "Sequence" -> Sequence(schema, variables, *actions.toTypedArray())
                "Select" -> Select(schema, variables, *actions.toTypedArray())
                else -> throw ActionInstantiationException("UnknownActionTypeErrorMessage".localize() + " ($this)")
            }
        } catch (e: AnimationInstantiationException) {
            throw ActionInstantiationException("FailedCreateAnimationErrorMessage".localize() + ": $this", e)
        } catch (e: VariableException) {
            throw ActionInstantiationException("FailedParameterEvaluationErrorMessage".localize() + ": $this", e)
        }
    }

    override fun validate() {
        for (ref in actionRefs) {
            ref.validate()
        }
    }

    private fun createActions() = actionRefs.map { it.buildAction(hashMapOf()) }

    private fun createAnimations() = animationBuilders.map { it.buildAnimation() }

    private fun createVariables(params: Map<String, String>) = VariableMap().apply {
        putAll(this@ActionBuilder.params.mapValues { Variable.parse(it.value) })
        putAll(params.mapValues { Variable.parse(it.value) })
    }

    override fun toString() = "Action ($name, $type, $className)"

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
    }
}
