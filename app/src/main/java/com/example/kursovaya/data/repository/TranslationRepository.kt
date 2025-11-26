package com.example.kursovaya.data.repository

import com.example.kursovaya.data.api.ApiClient
import com.example.kursovaya.data.database.TranslationDao
import com.example.kursovaya.data.models.TranslationHistory
import kotlinx.coroutines.flow.Flow
import java.text.Normalizer
import kotlin.math.min

class TranslationRepository(private val translationDao: TranslationDao) {

    suspend fun translateText(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Result<String> {
        return try {
            val cleanedText = preprocessText(text)

            // Пробуем разные API по очереди до первого успешного
            val result = tryTranslateWithMultipleApis(cleanedText, sourceLang, targetLang)

            result.onSuccess { translatedText ->
                // Пост-обработка перевода
                val finalText = postprocessTranslation(translatedText, cleanedText)
                saveToHistory(cleanedText, finalText, sourceLang, targetLang)
            }

            result
        } catch (e: Exception) {
            Result.failure(Exception("Translation error: ${e.message}"))
        }
    }

    private suspend fun tryTranslateWithMultipleApis(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Result<String> {
        // Список методов перевода в порядке приоритета (объявлены как suspend)
        val translationMethods = listOf<suspend () -> Result<String>>(
            { translateWithLibreTranslate(text, sourceLang, targetLang) },
            { translateWithMyMemory(text, sourceLang, targetLang) }
        )

        // Пробуем каждый метод до первого успешного
        for (method in translationMethods) {
            try {
                val result = method()
                if (result.isSuccess) {
                    val translatedText = result.getOrNull()
                    if (isValidTranslation(translatedText, text)) {
                        return result
                    }
                }
            } catch (e: Exception) {
                // Продолжаем пробовать следующий метод
                continue
            }
        }

        return Result.failure(Exception("All translation services failed"))
    }

    private suspend fun translateWithMyMemory(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Result<String> {
        return try {
            val langPair = "$sourceLang|$targetLang"
            val response = ApiClient.myMemoryService.translateMyMemory(text, langPair)

            if (response.isSuccessful) {
                response.body()?.let { translationResponse ->
                    if (translationResponse.responseStatus == 200) {
                        val translatedText = translationResponse.responseData.translatedText
                        Result.success(translatedText)
                    } else {
                        Result.failure(Exception("MyMemory API error: ${translationResponse.responseStatus}"))
                    }
                } ?: Result.failure(Exception("Empty response from MyMemory"))
            } else {
                Result.failure(Exception("HTTP error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("MyMemory network error: ${e.message}"))
        }
    }

    private suspend fun translateWithLibreTranslate(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Result<String> {
        return try {
            val response = ApiClient.libreTranslateService.translateLibreTranslate(
                text, sourceLang, targetLang
            )

            if (response.isSuccessful) {
                response.body()?.let { translationResponse ->
                    Result.success(translationResponse.translatedText)
                } ?: Result.failure(Exception("Empty response from LibreTranslate"))
            } else {
                Result.failure(Exception("LibreTranslate HTTP error: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("LibreTranslate network error: ${e.message}"))
        }
    }

    private fun preprocessText(text: String): String {
        return text.trim()
            .replace(Regex("\\s+"), " ") // Убираем лишние пробелы
            .replace(Regex("[\\r\\n]+"), " ") // Заменяем переносы строк на пробелы
    }

    private fun postprocessTranslation(translatedText: String, originalText: String): String {
        var result = translatedText.trim()

        // Убираем лишние кавычки, которые иногда добавляют API
        result = result.removeSurrounding("\"").removeSurrounding("'")

        // Восстанавливаем пунктуацию, если она потерялась
        if (originalText.endsWith('.') && !result.endsWith('.')) {
            result += "."
        } else if (originalText.endsWith('?') && !result.endsWith('?')) {
            result += "?"
        } else if (originalText.endsWith('!') && !result.endsWith('!')) {
            result += "!"
        }

        return result
    }

    private fun isValidTranslation(translatedText: String?, originalText: String): Boolean {
        if (translatedText.isNullOrBlank()) return false

        // Проверяем, что перевод не совпадает с оригиналом (кроме случаев, когда языки одинаковые)
        if (translatedText.equals(originalText, ignoreCase = true)) {
            return false
        }

        // Проверяем, что перевод не содержит очевидных ошибок
        val commonErrors = listOf("QUOTA EXCEEDED", "ERROR", "FAILED", "NOT FOUND")
        if (commonErrors.any { translatedText.contains(it, ignoreCase = true) }) {
            return false
        }

        return true
    }

    private suspend fun saveToHistory(
        originalText: String,
        translatedText: String,
        sourceLang: String,
        targetLang: String
    ) {
        // Ограничиваем длину текста для базы данных
        val maxLength = 1000
        val trimmedOriginal = originalText.take(maxLength)
        val trimmedTranslated = translatedText.take(maxLength)

        val existingTranslation = translationDao.findTranslation(trimmedOriginal, sourceLang, targetLang)

        if (existingTranslation != null) {
            val updated = existingTranslation.copy(
                usageCount = existingTranslation.usageCount + 1,
                timestamp = System.currentTimeMillis(),
                translatedText = trimmedTranslated
            )
            translationDao.updateTranslation(updated)
        } else {
            val newTranslation = TranslationHistory(
                originalText = trimmedOriginal,
                translatedText = trimmedTranslated,
                sourceLanguage = sourceLang,
                targetLanguage = targetLang
            )
            translationDao.insertTranslation(newTranslation)
        }
    }

    fun getHistory(): Flow<List<TranslationHistory>> {
        return translationDao.getAllHistory()
    }

    fun getFavorites(): Flow<List<TranslationHistory>> {
        return translationDao.getFavorites()
    }

    fun getHistoryByUsage(): Flow<List<TranslationHistory>> {
        return translationDao.getHistoryByUsage()
    }

    suspend fun toggleFavorite(translation: TranslationHistory) {
        val updated = translation.copy(isFavorite = !translation.isFavorite)
        translationDao.updateTranslation(updated)
    }

    suspend fun deleteTranslation(translation: TranslationHistory) {
        translationDao.deleteTranslation(translation)
    }

    suspend fun clearAllData() {
        translationDao.deleteAll()
    }
}
