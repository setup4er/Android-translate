package com.example.kursovaya.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.kursovaya.data.models.TranslationHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {

    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<TranslationHistory>>

    @Query("SELECT * FROM translation_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<TranslationHistory>>

    @Query("SELECT * FROM translation_history WHERE originalText = :text AND sourceLanguage = :sourceLang AND targetLanguage = :targetLang")
    suspend fun findTranslation(text: String, sourceLang: String, targetLang: String): TranslationHistory?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertTranslation(translation: TranslationHistory)

    @Update
    suspend fun updateTranslation(translation: TranslationHistory)

    @Query("DELETE FROM translation_history WHERE id = :id")
    suspend fun deleteTranslation(id: Long)

    @Query("SELECT * FROM translation_history ORDER BY usageCount DESC")
    fun getHistoryByUsage(): Flow<List<TranslationHistory>>
}