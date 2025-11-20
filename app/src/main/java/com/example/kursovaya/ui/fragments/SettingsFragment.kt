package com.example.kursovaya.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.kursovaya.databinding.FragmentSettingsBinding
import com.example.kursovaya.ui.viewmodels.TranslationViewModel
import com.example.kursovaya.ui.viewmodels.TranslationViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TranslationViewModel by viewModels {
        TranslationViewModelFactory((requireActivity().application as com.example.kursovaya.TranslationApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        loadCurrentTheme()
    }

    private fun setupUI() {
        // Переключатель темы
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Включена темная тема
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                saveThemePreference(true)
            } else {
                // Включена светлая тема
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                saveThemePreference(false)
            }
        }

        // Кнопка очистки данных
        binding.clearDataButton.setOnClickListener {
            showClearDataConfirmation()
        }

        // Информация о версии
        binding.versionInfo.text = "Версия 1.0"
    }

    private fun loadCurrentTheme() {
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val isDarkTheme = sharedPreferences.getBoolean("dark_theme", false)
        binding.themeSwitch.isChecked = isDarkTheme
    }

    private fun saveThemePreference(isDarkTheme: Boolean) {
        val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putBoolean("dark_theme", isDarkTheme)
            apply()
        }
    }

    private fun showClearDataConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Очистка данных")
            .setMessage("Вы уверены, что хотите удалить всю историю переводов, избранное и статистику? Это действие нельзя отменить.")
            .setPositiveButton("Удалить") { dialog, which ->
                clearAllData()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun clearAllData() {
        viewModel.clearAllData()
        com.google.android.material.snackbar.Snackbar.make(
            binding.root,
            "Все данные успешно удалены",
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).show()

        // Обновляем UI после очистки
        binding.root.postDelayed({
            // Можно обновить какие-то элементы UI если нужно
        }, 500)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}