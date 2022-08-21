package com.holahmeds.ledger.server

import android.content.SharedPreferences
import android.util.Log
import com.holahmeds.ledger.Error
import com.holahmeds.ledger.Result
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.URL

/**
 * On success, returns the API token
 *
 * On failure, returns
 * * [Error.ConnectionError]
 * * [Error.AuthorizationError]
 */
suspend fun getAuthToken(
    serverURL: URL,
    credentials: Credentials
): Result<String> = withContext(Dispatchers.IO) {
    val authClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            jackson()
        }
    }
    val response: HttpResponse
    try {
        response = authClient.post(serverURL) {
            url {
                appendPathSegments("auth", "get_token")
            }
            setBody(credentials)
            contentType(ContentType.Application.Json)
        }
    } catch (e: ConnectException) {
        return@withContext Result.Failure(Error.ConnectionError)
    }
    if (response.status == HttpStatusCode.NotFound || response.status == HttpStatusCode.Unauthorized) {
        return@withContext Result.Failure(Error.AuthorizationError("Unauthorized"))
    }
    val token: String = response.body()
    return@withContext Result.Success(token)
}

/**
 * On failure, returns
 * * [Error.ConnectionError]
 * * [Error.UserAlreadyExists]
 * * [Error.SignupDisabled]
 * * [Error.Some] if the server returns something other then 200, 409 or 404
 */
suspend fun signup(serverURL: URL, credentials: Credentials): Result<Unit> =
    withContext(Dispatchers.IO) {
        val authClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                jackson()
            }
        }
        val response: HttpResponse
        try {
            response = authClient.post(serverURL) {
                url {
                    appendPathSegments("auth", "signup")
                }
                setBody(credentials)
                contentType(ContentType.Application.Json)
            }
        } catch (e: ConnectException) {
            return@withContext Result.Failure(Error.ConnectionError)
        }

        when (response.status) {
            HttpStatusCode.Conflict -> Result.Failure(Error.UserAlreadyExists)
            HttpStatusCode.NotFound -> Result.Failure(Error.SignupDisabled)
            HttpStatusCode.OK -> Result.Success(Unit)
            else -> {
                Log.e("Signup", "Server returned ${response.status}")
                Result.Failure(Error.Some("Unknown error"))
            }
        }
    }

fun getServerUrl(sharedPreferences: SharedPreferences): Result<URL> {
    val serverURLStr = sharedPreferences.getString(PREFERENCE_SERVER_URL, null)
        ?: return Result.Failure(Error.InvalidServerURL)
    val serverURL: URL
    try {
        serverURL = URL(serverURLStr)
    } catch (e: MalformedURLException) {
        return Result.Failure(Error.InvalidServerURL)
    }

    return Result.Success(serverURL)
}
