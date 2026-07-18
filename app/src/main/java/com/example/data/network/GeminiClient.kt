package com.example.data.network

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiPart(val text: String? = null)

data class GeminiContent(val role: String? = null, val parts: List<GeminiPart>)

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

data class GeminiCandidate(val content: GeminiContent?)

data class GeminiResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun askAi(history: List<GeminiContent>, systemInstruction: String? = null): String {
        val configKey = BuildConfig.GEMINI_API_KEY.trim().removeSurrounding("\"").trim()
        val hasConfigKey = configKey.isNotEmpty() && configKey != "dummy"
        
        val primaryKey = if (hasConfigKey) configKey else "AQ.Ab8RN6Ki2yfGCaMvXtAbDfX9MJkhISa9dqRxchy0NLEBjL_t1A"
        
        return try {
            executeRequest(primaryKey, history, systemInstruction)
        } catch (e: retrofit2.HttpException) {
            e.printStackTrace()
            // If primaryKey was the configKey, let's auto-retry with fallback key to guarantee it works!
            if (primaryKey == configKey && configKey != "AQ.Ab8RN6Ki2yfGCaMvXtAbDfX9MJkhISa9dqRxchy0NLEBjL_t1A") {
                try {
                    executeRequest("AQ.Ab8RN6Ki2yfGCaMvXtAbDfX9MJkhISa9dqRxchy0NLEBjL_t1A", history, systemInstruction)
                } catch (retryEx: retrofit2.HttpException) {
                    val errorBody = retryEx.response()?.errorBody()?.string()
                    "Error calling AI: HTTP ${retryEx.code()}\nDetails: ${errorBody ?: retryEx.message()}"
                } catch (retryEx: Exception) {
                    "Error calling AI: Fallback key failed with: ${retryEx.localizedMessage ?: "Unknown error"}"
                }
            } else {
                val errorBody = e.response()?.errorBody()?.string()
                "Error calling AI: HTTP ${e.code()}\nDetails: ${errorBody ?: e.message()}"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // General capture, let's try fallback if the primary wasn't fallback
            if (primaryKey == configKey && configKey != "AQ.Ab8RN6Ki2yfGCaMvXtAbDfX9MJkhISa9dqRxchy0NLEBjL_t1A") {
                try {
                    executeRequest("AQ.Ab8RN6Ki2yfGCaMvXtAbDfX9MJkhISa9dqRxchy0NLEBjL_t1A", history, systemInstruction)
                } catch (retryEx: Exception) {
                    "Error calling AI: ${e.localizedMessage}\nFallback key also failed: ${retryEx.localizedMessage}"
                }
            } else {
                "Error calling AI: ${e.localizedMessage ?: "Unknown error"}"
            }
        }
    }

    private suspend fun executeRequest(apiKey: String, history: List<GeminiContent>, systemInstruction: String? = null): String {
        val request = GeminiRequest(
            contents = history,
            systemInstruction = systemInstruction?.let {
                GeminiContent(parts = listOf(GeminiPart(text = it)))
            }
        )
        val response = apiService.generateContent(apiKey, request)
        return response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
            ?: "No answer received from AI."
    }
}
