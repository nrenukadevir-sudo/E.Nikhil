package com.example.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

// --- Moshi Data Classes for Gemini REST API ---

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Double? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content? = null
)

// --- Local representation of parsed Quiz Questions ---

@JsonClass(generateAdapter = true)
data class GeminiQuizQuestion(
    @Json(name = "question") val question: String,
    @Json(name = "optionA") val optionA: String,
    @Json(name = "optionB") val optionB: String,
    @Json(name = "optionC") val optionC: String,
    @Json(name = "optionD") val optionD: String,
    @Json(name = "correctOption") val correctOption: String, // "A", "B", "C", or "D"
    @Json(name = "explanation") val explanation: String? = ""
)

// --- Retrofit Interface ---

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

// --- API Client ---

object GeminiClient {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    private val moshi = Moshi.Builder()
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val apiService: GeminiApi by lazy {
        retrofit.create(GeminiApi::class.java)
    }

    suspend fun generateQuizQuestions(
        topicPrompt: String,
        count: Int = 5,
        language: String = "English"
    ): List<GeminiQuizQuestion> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return emptyList()
        }

        val systemPrompt = """
            You are a master quiz creator for modern social media (YouTube Shorts/TikTok) and mobile quiz games.
            Create exactly $count interesting multiple-choice questions on the user's requested topic in $language.
            
            Strictly return a valid JSON array of objects. Do not wrap the JSON output in markdown ```json or other text.
            Each object in the array must follow this exact schema:
            {
              "question": "The quiz question text here",
              "optionA": "First option text",
              "optionB": "Second option text",
              "optionC": "Third option text",
              "optionD": "Fourth option text",
              "correctOption": "A", // must be exactly "A", "B", "C", or "D"
              "explanation": "A tiny interesting 1-sentence explanation of why it is correct"
            }
            
            Make sure questions are highly engaging, clean, educational/funny, and suitable for rapid fire, neon mobile game styling, or Telugu media.
        """.trimIndent()

        val requestBody = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = topicPrompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.85
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        val response = apiService.generateContent(apiKey, requestBody)
        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw Exception("No response received from Gemini")

        // Parse text with Moshi
        val listType = Types.newParameterizedType(List::class.java, GeminiQuizQuestion::class.java)
        val adapter = moshi.adapter<List<GeminiQuizQuestion>>(listType)
        
        // Clean text if some markdown is returned by mistake
        var cleanedText = text.trim()
        if (cleanedText.startsWith("```json")) {
            cleanedText = cleanedText.removePrefix("```json")
        }
        if (cleanedText.endsWith("```")) {
            cleanedText = cleanedText.removeSuffix("```")
        }
        cleanedText = cleanedText.trim()

        return adapter.fromJson(cleanedText) ?: emptyList()
    }
}
