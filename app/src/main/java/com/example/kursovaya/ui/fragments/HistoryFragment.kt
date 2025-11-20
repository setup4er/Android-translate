package com.example.kursovaya.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kursovaya.databinding.FragmentHistoryBinding
import com.example.kursovaya.ui.adapters.HistoryAdapter
import com.example.kursovaya.ui.viewmodels.TranslationViewModel
import com.example.kursovaya.ui.viewmodels.TranslationViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.widget.TextView

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TranslationViewModel by viewModels {
        TranslationViewModelFactory((requireActivity().application as com.example.kursovaya.TranslationApplication).repository)
    }

    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupTabLayout()
        // Загружаем историю по умолчанию
        loadHistory()
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            onFavoriteClick = { translation ->
                viewModel.toggleFavorite(translation)
            },
            onDeleteClick = { translation ->
                viewModel.deleteTranslation(translation)
            }
        )

        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun setupTabLayout() {
        // Устанавливаем обработчик выбора табов
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> loadHistory()
                    1 -> loadFavorites()
                    2 -> loadPopular()
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }

    private fun loadHistory() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.history.collectLatest { history ->
                historyAdapter.submitList(history)
                updateEmptyState(history.isEmpty(), "История переводов пуста")
            }
        }
    }

    private fun loadFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favorites.collectLatest { favorites ->
                historyAdapter.submitList(favorites)
                updateEmptyState(favorites.isEmpty(), "Нет избранных переводов")
            }
        }
    }

    private fun loadPopular() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.historyByUsage.collectLatest { popular ->
                historyAdapter.submitList(popular)
                updateEmptyState(popular.isEmpty(), "Нет популярных переводов")
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean, message: String) {
        if (isEmpty) {
            binding.emptyState.visibility = View.VISIBLE
            binding.historyRecyclerView.visibility = View.GONE
            // Можно обновить текст сообщения, если у вас есть TextView в emptyState
            val textView = binding.emptyState.getChildAt(1) as? TextView
            textView?.text = message
        } else {
            binding.emptyState.visibility = View.GONE
            binding.historyRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}