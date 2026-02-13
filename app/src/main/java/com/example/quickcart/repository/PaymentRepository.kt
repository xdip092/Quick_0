package com.example.quickcart.repository

import com.example.quickcart.BuildConfig
import com.example.quickcart.data.PaymentSession
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class PaymentRepository(
    private val baseUrl: String = BuildConfig.PAYMENT_BASE_URL
) {
    fun createSession(
        userId: String,
        amount: Int,
        gateway: String,
        currency: String = "INR"
    ): PaymentSession {
        require(baseUrl.isNotBlank()) { "PAYMENT_BASE_URL is empty. Set it in Gradle/local.properties." }
        val payload = JSONObject()
            .put("userId", userId)
            .put("amount", amount)
            .put("currency", currency)
            .put("gateway", gateway)

        val response = postJson("$baseUrl/payments/create-session", payload)
        return PaymentSession(
            sessionId = response.getString("sessionId"),
            gateway = response.getString("gateway"),
            checkoutUrl = response.getString("checkoutUrl")
        )
    }

    fun verifySession(sessionId: String): Boolean {
        require(baseUrl.isNotBlank()) { "PAYMENT_BASE_URL is empty. Set it in Gradle/local.properties." }
        val payload = JSONObject().put("sessionId", sessionId)
        val response = postJson("$baseUrl/payments/verify-session", payload)
        return response.optString("status").equals("PAID", ignoreCase = true)
    }

    private fun postJson(url: String, payload: JSONObject): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doInput = true
            doOutput = true
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }

        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(payload.toString())
        }

        val statusCode = connection.responseCode
        val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
        val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (statusCode !in 200..299) {
            throw IllegalStateException("Payment API error ($statusCode): $body")
        }

        return if (body.isBlank()) JSONObject() else JSONObject(body)
    }
}
