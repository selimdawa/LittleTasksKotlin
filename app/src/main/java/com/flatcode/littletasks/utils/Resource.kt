package com.flatcode.littletasks.utils

sealed class Resource<out T>(
    val data: T? = null,
    val message: String? = null
) {
    data object Idle : Resource<Nothing>()
    class Loading<T>(val isLoading: Boolean = true, data: T? = null) : Resource<T>(data)
    class Success<T>(data: T?) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}