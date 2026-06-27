package com.example.util

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiHelper {

    private const val TAG = "GeminiHelper"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Helper to check if the API key is configured
     */
    fun isApiKeyAvailable(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrEmpty() && key != "MY_GEMINI_API_KEY"
    }

    /**
     * Generic function to send a prompt to Gemini and receive a text response
     */
    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (!isApiKeyAvailable()) {
            return@withContext "የጌሚኒ ኤፒአይ ቁልፍ አልተዋቀረም (Gemini API key not configured)."
        }

        try {
            val url = "$BASE_URL?key=$apiKey"
            
            val jsonBody = JSONObject().apply {
                // Contents
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                
                // System Instruction if provided
                if (systemInstruction != null) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", systemInstruction)
                            })
                        })
                    })
                }

                // Configuration for optimal output
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.4)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonBody.toString().toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: ""
                    Log.e(TAG, "API call failed: $errorBody")
                    return@withContext "እባክዎን እንደገና ይሞክሩ (API Error: ${response.code})"
                }

                val responseBody = response.body?.string() ?: return@withContext "ባዶ ምላሽ (Empty response)"
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text")
                    }
                }
                "ምላሽ ማግኘት አልተቻለም (Could not extract response text)"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call", e)
            "ስህተት ተከስቷል: ${e.message}"
        }
    }

    /**
     * Ask Gemini to generate a clean, suitable title and a short summary (in Amharic) for a note.
     * Returns a pair of (Title, Summary).
     */
    suspend fun suggestTitleAndSummary(content: String): Pair<String, String> {
        val systemPrompt = "You are an Amharic writing assistant. Analyze the given text and provide a suitable, highly descriptive Title (maximum 4 words) and a short Summary (maximum 1 sentence). Respond ONLY in Amharic in this exact JSON format: {\"title\": \"your_title\", \"summary\": \"your_summary\"}. Do not add markdown or backticks."
        val responseText = generateContent(content, systemPrompt)
        
        return try {
            // Clean markdown blocks if returned
            val cleanJson = responseText.replace("```json", "").replace("```", "").trim()
            val json = JSONObject(cleanJson)
            val title = json.optString("title", "የተጠቆመ ርዕስ").trim()
            val summary = json.optString("summary", "ማጠቃለያ").trim()
            Pair(title, summary)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse title/summary JSON from: $responseText", e)
            // Fallback: generate simply
            val titleText = generateContent("ለዚህ ማስታወሻ አጭር ርዕስ (ቢበዛ 4 ቃላት) ብቻ ስጠኝ፡\n$content")
            Pair(titleText.take(40).trim(), "የጌሚኒ ረዳት ማጠቃለያ")
        }
    }

    /**
     * Ask Gemini to refine or improve the Amharic writing style.
     */
    suspend fun improveAmharicWriting(content: String): String {
        val systemPrompt = "You are a professional Amharic editor. Refine the grammar, spelling, and phrasing of the given Amharic text to make it more elegant, poetic, and professional. Keep the original meaning fully intact. Return ONLY the refined Amharic text, without comments."
        return generateContent(content, systemPrompt)
    }

    /**
     * Translate any foreign text (e.g. English) into refined Amharic.
     */
    suspend fun translateToAmharic(content: String): String {
        val systemPrompt = "You are an expert English-to-Amharic translator. Translate the given text accurately and naturally into Amharic. Maintain a warm, respectful tone. Return ONLY the translation, without notes."
        return generateContent(content, systemPrompt)
    }
}
