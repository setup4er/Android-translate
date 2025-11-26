package com.example.kursovaya.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.kursovaya.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        binding.appName.text = "Мой Переводчик"
        binding.versionInfo.text = "Версия 1.0"
        binding.developerInfo.text = "Разработано с ❤️ для удобного перевода"

        binding.appDescription.text = """
            "Мой Переводчик" - это современное приложение для мгновенного перевода текста между различными языками. 
            
            Основные возможности:
            • Перевод текста на 10+ языков
            • История всех переводов
            • Избранные переводы
            • Статистика популярных слов
            • Темная и светлая темы
            
            Приложение использует передовые технологии машинного перевода для обеспечения точных и качественных результатов.
        """.trimIndent()

    }

    private fun sendFeedback() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@mytranslator.app")
            putExtra(Intent.EXTRA_SUBJECT, "Обратная связь: Мой Переводчик")
            putExtra(Intent.EXTRA_TEXT, "Здравствуйте! Хочу поделиться мнением о приложении...")
        }
        try {
            startActivity(Intent.createChooser(emailIntent, "Отправить отзыв"))
        } catch (e: Exception) {
            com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                "Приложение почты не найдено",
                com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun rateApp() {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "Благодарим за оценку!",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showPrivacyPolicy() {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "Политика конфиденциальности",
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).setAction("Подробнее") {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mytranslator.app/privacy"))
            startActivity(intent)
        }.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
