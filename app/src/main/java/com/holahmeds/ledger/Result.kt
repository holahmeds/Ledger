package com.holahmeds.ledger

sealed class Result<out T> {
    data class Success<T>(val result: T) : Result<T>()
    data class Failure(val error: Error) : Result<Nothing>()
}

inline fun <R, T : R> Result<T>.getResultOr(block: (Error) -> R): R {
    return when (this) {
        is Result.Success -> this.result
        is Result.Failure -> {
            block(this.error)
        }
    }
}
