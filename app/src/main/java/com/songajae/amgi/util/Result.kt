package com.songajae.amgi.util
sealed class Result<out T> {
    data object Idle : Result<Nothing>()
    data object Loading : Result<Nothing>()
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
}
