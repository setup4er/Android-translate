package com.example.kursovaya.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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

class PopularFragment : Fragment() {

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
        loadPopular()
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
