package com.halalin.util

open class Event<out T>(private val content: T) {
    private var handled = false

    fun get(): T? =
        if (handled) null
        else {
            handled = true
            content
        }

    fun peek() = content
}
