package com.group_finity.mascot.exception

class AnimationInstantiationException : Exception {
    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}