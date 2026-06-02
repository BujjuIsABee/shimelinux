/*
 * ShimeLinux is an unofficial Linux version/Kotlin rewrite of Shimeji-ee by Kilkakon (https://kilkakon.com/shimeji)
 * View the full license here: https://github.com/BujjuIsABee/shimelinux/blob/master/LICENSE
 */

package com.group_finity.mascot.config

import com.group_finity.mascot.action.Action

interface IActionBuilder {
    fun validate()

    fun buildAction(params: Map<String, String>): Action
}
