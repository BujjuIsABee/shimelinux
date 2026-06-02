package com.group_finity.mascot.exception

class ConfigurationException : Exception {
    constructor(message: String) : super(message)

    constructor(cause: Throwable) : super(cause)

    constructor(message: String, cause: Throwable) : super(message, cause)
}