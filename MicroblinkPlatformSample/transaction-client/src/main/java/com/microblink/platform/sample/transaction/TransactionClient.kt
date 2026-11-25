package com.microblink.platform.sample.transaction

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

// FOR TESTING ONLY. DO NOT SHIP THIS IN YOUR APP.
//
// In production, your backend creates the transaction with your service account
// and sends the app only the sdkConfiguration from the response. This client lets
// you try the SDK without a backend, so it carries the service account's client
// secret: anyone who unzips an APK built with real values can read it and create
// transactions on your account. Use a test service account, and keep real
// credentials out of version control.
//
// Fill in the values Microblink gave you:
private const val TRANSACTION_URL = "https://YOUR_API_HOST/agent/api/v2/transaction"
private const val CLIENT_ID = "YOUR_CLIENT_ID"
private const val CLIENT_SECRET = "YOUR_CLIENT_SECRET"
private const val WORKFLOW_ID = "YOUR_WORKFLOW_ID"

/**
 * Creates transactions from the app, so you can try the SDK without a backend. In production your
 * backend creates them, and the app only receives the `sdkConfiguration`.
 */
object TransactionClient {

    /** Creates a transaction for [WORKFLOW_ID] and returns its `sdkConfiguration`, which starts the SDK. */
    suspend fun createTransaction(): String = withContext(Dispatchers.IO) {
        check(listOf(TRANSACTION_URL, CLIENT_ID, CLIENT_SECRET, WORKFLOW_ID).none { "YOUR_" in it }) {
            "Fill in the placeholders at the top of TransactionClient.kt first."
        }
        val body = JSONObject()
            .put("workflowId", WORKFLOW_ID)
            .put("platform", "Android")
            .put(
                // A real app sends the consent its user actually gave.
                "consent",
                JSONObject()
                    .put("userId", "sample-user")
                    .put("isProcessingStoringAllowed", true)
                    .put("isTrainingAllowed", false),
            )
        val credentials = Base64.encodeToString("$CLIENT_ID:$CLIENT_SECRET".toByteArray(), Base64.NO_WRAP)

        val connection = URL(TRANSACTION_URL).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.connectTimeout = 30_000
            connection.readTimeout = 30_000
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Basic $credentials")
            connection.outputStream.use { it.write(body.toString().toByteArray()) }

            val code = connection.responseCode
            val answer = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) throw IOException("Creating the transaction failed with HTTP $code: $answer")
            JSONObject(answer).getString("sdkConfiguration")
        } finally {
            connection.disconnect()
        }
    }
}
