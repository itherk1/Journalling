package com.example.api

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

suspend fun fetchDailyPrompts(focusArea: String = "", userGoals: String = ""): List<String> = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
        return@withContext listOf(
            "What are you grateful for today?",
            "What is one thing you learned today?",
            "What brought you joy today?"
        )
    }
    
    val basePrompt = "Provide 3 highly unique, profound, and varied self-reflection journal prompts for personal growth. Do not repeat standard questions. Separate each prompt with a newline character. Do not number them. Only write the prompts themselves."
    val specificPrompt = if (focusArea.isNotBlank() || userGoals.isNotBlank()) {
        "The user has a focus area of '$focusArea' and goals: '$userGoals'. Ensure 60-70% of the prompts are related to these areas, and the rest are general life reflections. $basePrompt"
    } else {
        basePrompt
    }
    
    val request = GenerateContentRequest(
        contents = listOf(Content(
            parts = listOf(Part(text = specificPrompt))
        ))
    )
    try {
        val response = RetrofitClient.service.generateContent(apiKey, request)
        val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
        val prompts = text.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        if (prompts.size >= 3) prompts.take(3) else listOf("How are you feeling right now?", "What's on your mind today?", "What are you grateful for today?")
    } catch (e: Exception) {
        listOf("How are you feeling right now?", "What's on your mind today?", "What are you grateful for today?")
    }
}
