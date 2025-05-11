package com.example.cocktailapp

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CocktailAiViewModel : ViewModel() {
    val result = mutableStateOf<String>("")
    val error = mutableStateOf<String?>(null)
    val loading = mutableStateOf(false)

    private val geminiApi = ApiClient.geminiApi

    fun generateCocktail(input: String) {
        loading.value = true
        error.value = null

        val prompt = """
        Na podstawie składników: $input

        Wygeneruj nazwę koktajlu i przepis w następującym **dokładnym** formacie:
        
        Nazwa koktajlu
        
        **Czas przygotowania:** ... minut
        
        **Składniki:**
        * składnik 1
        * składnik 2
        
        **Przygotowanie:**
        Opis krok po kroku.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            )
        )

        geminiApi.generateContent(apiKey = "REMOVED", request = request).enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                loading.value = false
                if (response.isSuccessful) {
                    val resultText = response.body()?.candidates?.firstOrNull()?.content?.parts?.joinToString("\n") {
                        it.text ?: ""
                    } ?: "Brak wyniku"
                    result.value = resultText
                } else {
                    error.value = "Błąd połączenia: ${response.errorBody()}"
                }
            }

            override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                loading.value = false
                error.value = t.message
            }
        })
    }

}
