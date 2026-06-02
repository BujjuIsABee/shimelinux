package com.group_finity.mascot.config

import com.group_finity.mascot.action.Action

interface IActionBuilder {
    fun validate()

    fun buildAction(params: Map<String, String>): Action
}