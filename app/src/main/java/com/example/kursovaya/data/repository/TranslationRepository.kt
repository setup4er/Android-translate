package com.example.kursovaya.data.repository

import com.example.kursovaya.data.api.ApiClient
import com.example.kursovaya.data.database.TranslationDao
import com.example.kursovaya.data.models.TranslationHistory
import kotlinx.coroutines.flow.Flow

class TranslationRepository(private val translationDao: TranslationDao) {

    suspend fun translateText(
        text: String,
        sourceLang: String,
        targetLang: String
    ): Result<String> {
        return try {
            // Используем MyMemory API
            val langPair = "$sourceLang|$targetLang"
            val response = ApiClient.translationService.translate(text, langPair)

            if (response.isSuccessful) {
                response.body()?.let { translationResponse ->
                    if (translationResponse.responseStatus == 200) {
                        val translatedText = translationResponse.responseData.translatedText

                        // Сохраняем в историю
                        saveToHistory(text, translatedText, sourceLang, targetLang)
                        Result.success(translatedText)
                    } else {
                        Result.failure(Exception("Translation API error: ${translationResponse.responseStatus}"))
                    }
                } ?: Result.failure(Exception("Empty response from server"))
            } else {
                Result.failure(Exception("HTTP error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message}"))
        }
    }

    private suspend fun saveToHistory(
        originalText: String,
        translatedText: String,
        sourceLang: String,
        targetLang: String
    ) {
        val existingTranslation = translationDao.findTranslation(originalText, sourceLang, targetLang)

        if (existingTranslation != null) {
            // Увеличиваем счетчик использований
            val updated = existingTranslation.copy(
                usageCount = existingTranslation.usageCount + 1,
                timestamp = System.currentTimeMillis()
            )
            translationDao.updateTranslation(updated)
        } else {
            // Создаем новую запись
            val newTranslation = TranslationHistory(
                originalText = originalText,
                translatedText = translatedText,
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