package com.holahmeds.ledger.server

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CredentialManagerTest {
    private lateinit var credentialManager: CredentialManager
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setup() {
        credentialManager = CredentialManager(context)
    }

    @After
    fun cleanUp() {
        context.deleteFile(TOKEN_FILE_NAME)
    }

    @Test
    fun testTokenSaved() {
        credentialManager.saveToken("abc")
        assertEquals(
            "abc",
            CredentialManager(context).getToken()
        )
    }
}