package com.example.kursovaya.data.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.Headers

interface TranslationApiService {

    @GET("get")
    suspend fun translateMyMemory(
        @Query("q") text: String,
        @Query("langpair") langPair: String
    ): Response<MyMemoryResponse>

    @GET("translate")
    suspend fun translateLibreTranslate(
        @Query("q") text: String,
        @Query("source") source: String,
        @Query("target") target: String,
        @Query("format") format: String = "text"
    ): Response<LibreTranslateResponse>

    // Резервный API - FunTranslations (только для популярных фраз)
    @GET("yoda.json")
    suspend fun translateFunTranslations(
        @Query("text") text: String
    ): Response<FunTranslationResponse>
}

// MyMemory API Response
data class MyMemoryResponse(
    val responseData: ResponseData,
    val responseStatus: Int
)

data class ResponseData(
    val translatedText: String
)

// LibreTranslate API Response
data class LibreTranslateResponse(
    val translatedText: String
)

// FunTranslations API Response
data class FunTranslationResponse(
    val contents: FunTranslationContents
)

data class FunTranslationContents(
    val translated: String,
    val text: String,
    val translation: String
)

// Конфигурация Retrofit для разных API
object ApiClient {
    private const val MY_MEMORY_BASE_URL = "https://api.mymemory.translated.net/"
    private const val LIBRE_TRANSLATE_BASE_URL = "https://libretranslate.de/"
    private const val FUN_TRANSLATIONS_BASE_URL = "https://api.funtranslations.com/"

    val myMemoryService: TranslationApiService by lazy {
        createService(MY_MEMORY_BASE_URL)
    }

    val libreTranslateService: TranslationApiService by lazy {
        createService(LIBRE_TRANSLATE_BASE_URL)
    }

    val funTranslationsService: TranslationApiService by lazy {
        createService(FUN_TRANSLATIONS_BASE_URL)
    }

    private fun createService(baseUrl: String): TranslationApiService {
        val logging = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }

        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        return retrofit2.Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
            .create(TranslationApiService::class.java)
    }
}
