package com.example.kursovaya.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history")
data class TranslationHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val originalText: String,
    val translatedText: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val timestamp: Long = System.currentTimeMillis(),
    val usageCount: Int = 1,
    val isFavorite: Boolean = false
)

data class Language(
    val code: String,
    val name: String
)

data class TranslationResponse(
    val translatedText: String,
    val sourceLanguage: String,
    val targetLanguage: String
)