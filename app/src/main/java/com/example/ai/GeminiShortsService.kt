package com.example.ai

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

object GeminiShortsService {

    private const val TAG = "GeminiShortsService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun isKeyConfigured(): Boolean {
        val key = runCatching { BuildConfig.GEMINI_API_KEY }.getOrNull()
        return !key.isNullOrBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun generateShortsScript(
        topic: String,
        tone: String,
        targetDurationSec: Int
    ): ScriptGenerationResult? = withContext(Dispatchers.IO) {
        val apiKey = runCatching { BuildConfig.GEMINI_API_KEY }.getOrNull() ?: ""
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "No active Gemini API key configured, using built-in creative engine")
            return@withContext null
        }

        try {
            val prompt = """
                You are a master viral YouTube Shorts & Instagram Reels scriptwriter with 10M+ subscribers.
                Create a high-retention vertical short script on the topic: "$topic".
                
                Tone: $tone
                Target Spoken Duration: $targetDurationSec seconds (approx ${ (targetDurationSec * 2.5).toInt() } words total).
                
                Requirements:
                1. Hook (first 2-3 seconds): Must grab attention immediately with a psychological question, shocking fact, or curiosity gap. No greetings like 'Hey guys'.
                2. Body: Direct, high-energy storytelling. Use simple conversational words. Every sentence must move the narrative forward.
                3. CTA: Punchy call to action (e.g. Subscribe for daily mind-blowing facts, or drop your thoughts in the comments).
                4. Title: Catchy 40-character viral title in Title Case with 1 emoji.
                5. Tags: 4-5 relevant viral hashtags.
                
                Respond ONLY with valid JSON in this exact structure:
                {
                  "title": "Title here",
                  "hook": "Hook sentence here.",
                  "body": "Body story text here with 3-4 punchy sentences.",
                  "cta": "Call to action here.",
                  "tags": ["#Shorts", "#Viral", "#$topic"]
                }
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArr = JSONArray()
                val contentObj = JSONObject().apply {
                    val partsArr = JSONArray()
                    val partObj = JSONObject().apply {
                        put("text", prompt)
                    }
                    partsArr.put(partObj)
                    put("parts", partsArr)
                }
                contentsArr.put(contentObj)
                put("contents", contentsArr)

                val genConfig = JSONObject().apply {
                    put("temperature", 0.85)
                    put("responseMimeType", "application/json")
                }
                put("generationConfig", genConfig)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val bodyString = response.body?.string() ?: return@withContext null

            if (!response.isSuccessful) {
                Log.e(TAG, "Gemini API error code: ${response.code}, body: $bodyString")
                return@withContext null
            }

            val responseObj = JSONObject(bodyString)
            val candidates = responseObj.optJSONArray("candidates") ?: return@withContext null
            if (candidates.length() == 0) return@withContext null

            val candidate = candidates.getJSONObject(0)
            val content = candidate.getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text")

            val scriptJson = JSONObject(text.trim())
            val title = scriptJson.optString("title", topic)
            val hook = scriptJson.optString("hook", "")
            val body = scriptJson.optString("body", "")
            val cta = scriptJson.optString("cta", "")
            val tagsArr = scriptJson.optJSONArray("tags")
            val tagsList = mutableListOf<String>()
            if (tagsArr != null) {
                for (i in 0 until tagsArr.length()) {
                    tagsList.add(tagsArr.getString(i))
                }
            } else {
                tagsList.addAll(listOf("#Shorts", "#Viral", "#Trending"))
            }

            ScriptGenerationResult(
                title = title,
                hook = hook,
                body = body,
                cta = cta,
                tags = tagsList
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed calling Gemini API", e)
            null
        }
    }
}

data class ScriptGenerationResult(
    val title: String,
    val hook: String,
    val body: String,
    val cta: String,
    val tags: List<String>
)
