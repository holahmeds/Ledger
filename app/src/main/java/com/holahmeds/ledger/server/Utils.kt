package com.holahmeds.ledger.server

import android.content.SharedPreferences
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
import java.net.ConnectException
import java.net.MalformedURLException
import java.net.URL

suspend fun getAuthToken(
    serverURL: URL,
    credentials: Credentials
): Result<String> {
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
        return Result.Failure(Error.ConnectionError)
    }
    if (response.status == HttpStatusCode.NotFound || response.status == HttpStatusCode.Unauthorized) {
        return Result.Failure(Error.AuthorizationError("Unauthorized"))
    }
    val token: String = response.body()
    return Result.Success(token)
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
