package com.example.kursovaya.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TranslationApiService {

    @GET("get")
    suspend fun translate(
        @Query("q") text: String,
        @Query("langpair") langPair: String
    ): Response<MyMemoryResponse>
}

data class MyMemoryResponse(
    val responseData: ResponseData,
    val responseStatus: Int
)

data class ResponseData(
    val translatedText: String
)

// Конфигурация Retrofit для MyMemory API
object ApiClient {
    private const val BASE_URL = "https://api.mymemory.translated.net/"

    val translationService: TranslationApiService by lazy {
        val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }

        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        retrofit2.Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(TranslationApiService::class.java)
    }
}