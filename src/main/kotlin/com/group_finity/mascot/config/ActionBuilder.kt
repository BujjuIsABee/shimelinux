/*
 * Copyright (c) 2026, Bujju
 * All rights reserved.
 * License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 * Original License: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE-ORIGINAL
 */

package com.group_finity.mascot.config

import com.group_finity.mascot.Main
import com.group_finity.mascot.action.Action
import com.group_finity.mascot.action.Animate
import com.group_finity.mascot.action.Move
import com.group_finity.mascot.action.Select
import com.group_finity.mascot.action.Sequence
import com.group_finity.mascot.action.Stay
import com.group_finity.mascot.animation.Animation
import com.group_finity.mascot.exception.ActionInstantiationException
import com.group_finity.mascot.exception.AnimationInstantiationException
import com.group_finity.mascot.exception.ConfigurationException
import com.group_finity.mascot.exception.VariableException
import com.group_finity.mascot.script.Variable
import com.group_finity.mascot.script.VariableMap
import java.util.ResourceBundle
import java.util.logging.Level
import java.util.logging.Logger

class ActionBuilder(configuration: Configuration, actionNode: Entry, imageSet: String) : IActionBuilder {
    private val schema = configuration.schema
    val type = checkNotNull(actionNode.getAttribute(schema.getString("Type")))
    val name = actionNode.getAttribute(schema.getString("Name"))
    private val className = actionNode.getAttribute(schema.getString("Class"))
    private val params = LinkedHashMap<String, String>()
    private val animationBuilders = ArrayList<AnimationBuilder>()
    private val actionRefs = ArrayList<IActionBuilder>()

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
            val errorMessage = Main.instance.languageBundle.getString("FailedLoadActionErrorMessage")
            val forShimeji = Main.instance.languageBundle.getString("ForShimeji")
            throw ConfigurationException("$errorMessage \"$name\" $forShimeji \"$imageSet.\"", e)
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
                        try {
                            try {
                                return cls.getConstructor(
                                    ResourceBundle::class.java,
                                    ArrayList::class.java,
                                    VariableMap::class.java
                                ).newInstance(schema, animations, variables)
                            } catch (_: Exception) {
                            }
                            return cls.getConstructor(ResourceBundle::class.java, VariableMap::class.java)
                                .newInstance(schema, variables)
                        } catch (_: Exception) {
                        }
                        return cls.getConstructor().newInstance()
                    } catch (e: InstantiationException) {
                        throw ActionInstantiationException(Main.instance.languageBundle.getString("FailedClassActionInitialiseErrorMessage") + " ($this)", e)
                    } catch (e: IllegalAccessException) {
                        throw ActionInstantiationException(Main.instance.languageBundle.getString("CannotAccessClassActionErrorMessage") + " ($this)", e)
                    } catch (e: ClassNotFoundException) {
                        throw ActionInstantiationException(Main.instance.languageBundle.getString("ClassNotFoundErrorMessage") + " ($this)", e)
                    }
                }
                "Move" -> Move(schema, animations, variables)
                "Stay" -> Stay(schema, animations, variables)
                "Animate" -> Animate(schema, animations, variables)
                "Sequence" -> Sequence(schema, variables, *actions.toTypedArray())
                "Select" -> Select(schema, variables, *actions.toTypedArray())
                else -> throw ActionInstantiationException(Main.instance.languageBundle.getString("UnknownActionTypeErrorMessage") + " ($this)")
            }
        } catch (e: AnimationInstantiationException) {
            throw ActionInstantiationException(Main.instance.languageBundle.getString("FailedCreateAnimationErrorMessage") + ": $this", e)
        } catch (e: VariableException) {
            throw ActionInstantiationException(Main.instance.languageBundle.getString("FailedParameterEvaluationErrorMessage") + ": $this", e)
        }
    }

    override fun validate() {
        for (ref in actionRefs) {
            ref.validate()
        }
    }

    private fun createActions(): List<Action> {
        val result = ArrayList<Action>()
        for (ref in actionRefs) {
            result.add(ref.buildAction(HashMap()))
        }
        return result
    }

    private fun createAnimations(): ArrayList<Animation> {
        val result = ArrayList<Animation>()
        for (animationFactory in animationBuilders) {
            result.add(animationFactory.buildAnimation())
        }
        return result
    }

    private fun createVariables(params: Map<String, String>): VariableMap {
        val result = VariableMap()
        for (param in this.params) {
            result[param.key] = Variable.parse(param.value)
        }
        for (param in params) {
            result[param.key] = Variable.parse(param.value)
        }
        return result
    }

    override fun toString(): String = "Action ($name,$type,$className)"

    companion object {
        private val log = Logger.getLogger(this::class.java.name)
    }
}
