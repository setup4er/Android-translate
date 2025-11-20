package com.example.kursovaya.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kursovaya.data.models.TranslationHistory
import com.example.kursovaya.data.repository.TranslationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TranslationViewModel(private val repository: TranslationRepository) : ViewModel() {

    private val _translationResult = MutableStateFlow<Result<String>?>(null)
    val translationResult: StateFlow<Result<String>?> = _translationResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Используем Flow из репозитория
    val history = repository.getHistory()
    val favorites = repository.getFavorites()  // Этот метод теперь будет работать
    val historyByUsage = repository.getHistoryByUsage()

    fun translateText(text: String, sourceLang: String, targetLang: String) {
        if (text.isBlank()) return

        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.translateText(text, sourceLang, targetLang)
            _translationResult.value = result
            _isLoading.value = false
        }
    }

    fun toggleFavorite(translation: TranslationHistory) {
        viewModelScope.launch {
            repository.toggleFavorite(translation)
        }
    }

    fun deleteTranslation(translation: TranslationHistory) {
        viewModelScope.launch {
            repository.deleteTranslation(translation)
        }
    }

    fun clearTranslationResult() {
        _translationResult.value = null
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }
}