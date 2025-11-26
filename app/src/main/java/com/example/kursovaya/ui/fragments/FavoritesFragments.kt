package com.example.kursovaya.ui.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kursovaya.R
import com.example.kursovaya.databinding.DialogSortBinding
import com.example.kursovaya.databinding.FragmentFavoritesBinding
import com.example.kursovaya.ui.adapters.HistoryAdapter
import com.example.kursovaya.ui.viewmodels.TranslationViewModel
import com.example.kursovaya.ui.viewmodels.TranslationViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TranslationViewModel by viewModels {
        TranslationViewModelFactory((requireActivity().application as com.example.kursovaya.TranslationApplication).repository)
    }

    private lateinit var historyAdapter: HistoryAdapter

    private var currentSortType = SortType.ALPHABET
    private var currentSortOrder = SortOrder.ASCENDING
    private var allFavorites = listOf<com.example.kursovaya.data.models.TranslationHistory>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.favorites_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sort -> {
                showSortDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        loadFavorites()
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_sort -> {
                    showSortDialog()
                    true
                }
                else -> false
            }
        }
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

        binding.favoritesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applySortingAndFiltering()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                applySortingAndFiltering()
                true
            } else {
                false
            }
        }
    }

    private fun loadFavorites() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favorites.collectLatest { favorites: List<com.example.kursovaya.data.models.TranslationHistory> ->
                allFavorites = favorites
                applySortingAndFiltering()
            }
        }
    }

    private fun applySortingAndFiltering() {
        val query = binding.searchEditText.text.toString().trim()

        var filteredList = if (query.isNotEmpty()) {
            allFavorites.filter {
                it.originalText.contains(query, true) ||
                        it.translatedText.contains(query, true)
            }
        } else {
            allFavorites
        }

        filteredList = when (currentSortType) {
            SortType.ALPHABET -> {
                if (currentSortOrder == SortOrder.ASCENDING) {
                    filteredList.sortedBy { it.originalText }
                } else {
                    filteredList.sortedByDescending { it.originalText }
                }
            }
            SortType.USAGE -> {
                if (currentSortOrder == SortOrder.ASCENDING) {
                    filteredList.sortedBy { it.usageCount }
                } else {
                    filteredList.sortedByDescending { it.usageCount }
                }
            }
        }

        historyAdapter.submitList(filteredList)
        updateEmptyState(filteredList.isEmpty(), query)
    }

    private fun showSortDialog() {
        val dialogBinding = DialogSortBinding.inflate(layoutInflater)

        when (currentSortType) {
            SortType.ALPHABET -> dialogBinding.radioAlphabet.isChecked = true
            SortType.USAGE -> dialogBinding.radioUsage.isChecked = true
        }

        when (currentSortOrder) {
            SortOrder.ASCENDING -> dialogBinding.radioAscending.isChecked = true
            SortOrder.DESCENDING -> dialogBinding.radioDescending.isChecked = true
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.buttonApply.setOnClickListener {
            currentSortType = when (dialogBinding.radioGroupSortBy.checkedRadioButtonId) {
                R.id.radioAlphabet -> SortType.ALPHABET
                R.id.radioUsage -> SortType.USAGE
                else -> SortType.ALPHABET
            }

            currentSortOrder = when (dialogBinding.radioGroupOrder.checkedRadioButtonId) {
                R.id.radioAscending -> SortOrder.ASCENDING
                R.id.radioDescending -> SortOrder.DESCENDING
                else -> SortOrder.ASCENDING
            }

            applySortingAndFiltering()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateEmptyState(isEmpty: Boolean, query: String) {
        if (isEmpty) {
            binding.emptyState.visibility = View.VISIBLE
            binding.favoritesRecyclerView.visibility = View.GONE

            if (query.isNotEmpty()) {
                binding.emptyStateText.text = "Ничего не найдено по запросу \"$query\""
            } else {
                binding.emptyStateText.text = "В избранном пока ничего нет"
            }
        } else {
            binding.emptyState.visibility = View.GONE
            binding.favoritesRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class SortType {
        ALPHABET, USAGE
    }

    enum class SortOrder {
        ASCENDING, DESCENDING
    }
}
