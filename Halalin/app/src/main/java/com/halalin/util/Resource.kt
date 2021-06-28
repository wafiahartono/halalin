package com.halalin.util

sealed class Resource<T> {
    class Failure<T> : Resource<T>()
    class Loading<T> : Resource<T>()
    class Success<T>(val data: T) : Resource<T>()
}
