package com.sampreeth.kaushalyakarnataka

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ClaudeHelper {

    private const val API_KEY = "AIzaSyC2G9K69NylXVS61kiw6RWxe8L--eJ6Eko"
    private const val API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$API_KEY"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun ask(userMessage: String): Result<String> = withContext(Dispatchers.IO) {
        var attempts = 0
        while (attempts < 3) {
            try {
                val safeMessage = userMessage
                    .replace("\\", "\\\\")
                    .replace("\"", "'")
                    .replace("\n", " ")
                    .replace("\r", " ")

                val requestBody = """
                    {
                        "contents": [
                            {
                                "parts": [
                                    {
                                        "text": "$safeMessage"
                                    }
                                ]
                            }
                        ],
                        "generationConfig": {
                            "temperature": 0.7,
                            "maxOutputTokens": 500
                        }
                    }
                """.trimIndent()

                val request = Request.Builder()
                    .url(API_URL)
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.code == 429) {
                    attempts++
                    Thread.sleep(10000)
                    continue
                }

                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        Exception("HTTP ${response.code}: $responseBody")
                    )
                }

                val json = JSONObject(responseBody)

                if (json.has("error")) {
                    val errorMsg = json.getJSONObject("error").getString("message")
                    return@withContext Result.failure(Exception("API: $errorMsg"))
                }

                if (!json.has("candidates")) {
                    return@withContext Result.failure(
                        Exception("No response from AI. Try again!")
                    )
                }

                val reply = json
                    .getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                return@withContext Result.success(reply)

            } catch (e: Exception) {
                return@withContext Result.failure(Exception("Error: ${e.message}"))
            }
        }
        return@withContext Result.failure(
            Exception("Too many requests. Please wait a moment and try again.")
        )
    }
}