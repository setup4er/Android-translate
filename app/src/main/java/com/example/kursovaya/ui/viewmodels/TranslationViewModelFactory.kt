package com.example.kursovaya.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kursovaya.data.repository.TranslationRepository

class TranslationViewModelFactory(
    private val repository: TranslationRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TranslationViewModel::class.java)) {
            return TranslationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}