package com.group_finity.mascot.exception

class ActionInstantiationException : Exception {
    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}