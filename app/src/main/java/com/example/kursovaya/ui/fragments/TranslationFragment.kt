package com.example.kursovaya.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.kursovaya.databinding.FragmentTranslationBinding
import com.example.kursovaya.ui.viewmodels.TranslationViewModel
import com.example.kursovaya.ui.viewmodels.TranslationViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TranslationFragment : Fragment() {

    private var _binding: FragmentTranslationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TranslationViewModel by viewModels {
        TranslationViewModelFactory((requireActivity().application as com.example.kursovaya.TranslationApplication).repository)
    }

    private val languages = listOf(
        "Русский" to "ru",
        "Английский" to "en",
        "Испанский" to "es",
        "Французский" to "fr",
        "Немецкий" to "de",
        "Итальянский" to "it",
        "Японский" to "ja",
        "Корейский" to "ko",
        "Китайский" to "zh",
        "Арабский" to "ar"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTranslationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        // Настройка выпадающих списков языков
        val languageNames = languages.map { it.first }

        // Исходный язык (по умолчанию русский)
        binding.sourceLanguageDropdown.setSimpleItems(languageNames.toTypedArray())
        binding.sourceLanguageDropdown.setText("Русский", false)

        // Целевой язык (по умолчанию английский)
        binding.targetLanguageDropdown.setSimpleItems(languageNames.toTypedArray())
        binding.targetLanguageDropdown.setText("Английский", false)

        // Кнопка перевода
        binding.translateButton.setOnClickListener {
            val text = binding.inputText.text.toString().trim()
            val sourceLang = languages.find { it.first == binding.sourceLanguageDropdown.text.toString() }?.second ?: "ru"
            val targetLang = languages.find { it.first == binding.targetLanguageDropdown.text.toString() }?.second ?: "en"

            if (text.isNotEmpty()) {
                viewModel.translateText(text, sourceLang, targetLang)
            } else {
                binding.inputText.error = "Введите текст"
            }
        }

        // Кнопка смены языков
        binding.swapLanguagesButton.setOnClickListener {
            val currentSource = binding.sourceLanguageDropdown.text.toString()
            val currentTarget = binding.targetLanguageDropdown.text.toString()

            binding.sourceLanguageDropdown.setText(currentTarget, false)
            binding.targetLanguageDropdown.setText(currentSource, false)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.translationResult.collectLatest { result ->
                result?.fold(
                    onSuccess = { translatedText ->
                        // Теперь получаем просто строку с переведенным текстом
                        binding.translatedText.text = translatedText
                        binding.resultCard.visibility = View.VISIBLE
                    },
                    onFailure = { exception ->
                        binding.translatedText.text = "Ошибка перевода: ${exception.message}"
                        binding.resultCard.visibility = View.VISIBLE
                    }
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.translateButton.isEnabled = !isLoading
                binding.translateButton.text = if (isLoading) "Перевод..." else "Перевести"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}