package com.holahmeds.ledger

sealed class Error {
    open class Some(private val errorMessage: String) : Error() {
        fun errorMessage(): String = errorMessage
    }

    class InvalidServerURL : Some("Invalid Server URL")

    object None : Error()
}