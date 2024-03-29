package com.holahmeds.ledger

sealed class Error {
    open class Some(private val errorMessage: String) : Error() {
        fun errorMessage(): String = errorMessage
    }

    open class InvalidProperties(errorMessage: String) : Some(errorMessage)
    object InvalidServerURL : InvalidProperties("Invalid Server URL")
    object UsernameNotSet : InvalidProperties("Username not set")
    object PasswordNotSet : InvalidProperties("Password not set")

    class AuthorizationError(errorMessage: String) : Some(errorMessage)

    object ConnectionError : Some("ConnectionError")

    object UserAlreadyExists : Some("User already exists")
    object SignupDisabled : Some("Signup disable")

    object TransactionNotFoundError : Some("Transaction not Found")

    object None : Error()
}