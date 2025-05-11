package com.example.cocktailapp

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// Model requestu
data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String?
)

// Model odpowiedzi
data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content
)

// Interfejs Retrofit
interface GeminiApi {
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Call<GeminiResponse>
}

// Klient API
object ApiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val geminiApi: GeminiApi = retrofit2.Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
        .client(okhttp3.OkHttpClient.Builder().addInterceptor(
            okhttp3.logging.HttpLoggingInterceptor().apply {
                level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
            }
        ).build())
        .build()
        .create(GeminiApi::class.java)
}
