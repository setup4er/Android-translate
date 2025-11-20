package com.example.kursovaya

import android.app.Application
import com.example.kursovaya.data.database.AppDatabase
import com.example.kursovaya.data.repository.TranslationRepository

class TranslationApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { TranslationRepository(database.translationDao()) }
}