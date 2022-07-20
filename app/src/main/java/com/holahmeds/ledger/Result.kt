package com.holahmeds.ledger

sealed class Result<out T> {
    class Success<out T>(val result: T) : Result<T>()
    class Failure(val error: Error) : Result<Nothing>()
}