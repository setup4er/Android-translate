package com.example.kursovaya.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.kursovaya.R
import com.example.kursovaya.databinding.FragmentTranslationBinding
import com.example.kursovaya.ui.dialogs.LanguageSelectionDialog
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
        binding.sourceLanguageText.text = "Русский"
        binding.targetLanguageText.text = "Английский"

        binding.sourceLanguageCard.setOnClickListener {
            showLanguageSelectionDialog(true)
        }

        binding.targetLanguageCard.setOnClickListener {
            showLanguageSelectionDialog(false)
        }

        binding.translateButton.setOnClickListener {
            val text = binding.inputText.text.toString().trim()
            val sourceLang = languages.find { it.first == binding.sourceLanguageText.text.toString() }?.second ?: "ru"
            val targetLang = languages.find { it.first == binding.targetLanguageText.text.toString() }?.second ?: "en"

            if (text.isNotEmpty()) {
                if (text.length > 500) {
                    binding.inputText.error = "Текст слишком длинный (максимум 500 символов)"
                } else {
                    viewModel.translateText(text, sourceLang, targetLang)
                }
            } else {
                binding.inputText.error = "Введите текст"
            }
        }

        binding.swapLanguagesButton.setOnClickListener {
            val currentSource = binding.sourceLanguageText.text.toString()
            val currentTarget = binding.targetLanguageText.text.toString()

            binding.sourceLanguageText.text = currentTarget
            binding.targetLanguageText.text = currentSource
        }

        // Очистка ошибки при вводе
        binding.inputText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                binding.inputText.error = null
            }
        })
    }

    private fun showLanguageSelectionDialog(isSourceLanguage: Boolean) {
        val dialog = LanguageSelectionDialog(languages) { languageName, languageCode ->
            if (isSourceLanguage) {
                binding.sourceLanguageText.text = languageName
            } else {
                binding.targetLanguageText.text = languageName
            }
        }
        dialog.show(parentFragmentManager, "LanguageSelectionDialog")
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.translationResult.collectLatest { result ->
                result?.fold(
                    onSuccess = { translatedText ->
                        binding.translatedText.text = translatedText
                        binding.resultCard.visibility = View.VISIBLE
                        setTranslateButtonActive()
                        showTranslationSuccess()
                    },
                    onFailure = { exception ->
                        val errorMessage = when {
                            exception.message?.contains("network", ignoreCase = true) == true ->
                                "Проблемы с интернет-соединением"
                            exception.message?.contains("quota", ignoreCase = true) == true ->
                                "Лимит переводов исчерпан. Попробуйте позже"
                            exception.message?.contains("All translation services failed") == true ->
                                "Сервисы перевода временно недоступны"
                            else -> "Ошибка перевода: ${exception.message}"
                        }
                        binding.translatedText.text = errorMessage
                        binding.resultCard.visibility = View.VISIBLE
                        setTranslateButtonActive()
                        showTranslationError(errorMessage)
                    }
                )
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                if (isLoading) {
                    setTranslateButtonLoading()
                } else {
                    setTranslateButtonActive()
                }
            }
        }
    }

    private fun setTranslateButtonLoading() {
        binding.translateButton.apply {
            text = "Перевод..."
            isEnabled = false
            background = ContextCompat.getDrawable(requireContext(), R.drawable.button_gradient_disabled)
        }
    }

    private fun setTranslateButtonActive() {
        binding.translateButton.apply {
            text = "Перевести"
            isEnabled = true
            background = ContextCompat.getDrawable(requireContext(), R.drawable.button_gradient)
        }
    }

    private fun showTranslationSuccess() {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "Перевод выполнен успешно",
            com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showTranslationError(message: String) {
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            message,
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()  // ИСПРАВЛЕНО: было super.onViewCreated(view, savedInstanceState)
        _binding = null
    }
}
