package com.example.kursovaya.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kursovaya.R
import com.example.kursovaya.data.models.TranslationHistory
import com.example.kursovaya.databinding.ItemHistoryBinding

class HistoryAdapter(
    private val onFavoriteClick: (TranslationHistory) -> Unit,
    private val onDeleteClick: (TranslationHistory) -> Unit
) : ListAdapter<TranslationHistory, HistoryAdapter.HistoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class HistoryViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(translation: TranslationHistory) {
            binding.apply {
                originalText.text = translation.originalText
                translatedText.text = translation.translatedText
                languagesText.text = "${translation.sourceLanguage} → ${translation.targetLanguage}"
                usageCount.text = "Использовано: ${translation.usageCount}"

                // Красное сердечко для избранного
                favoriteButton.setImageResource(
                    if (translation.isFavorite) R.drawable.ic_favorite
                    else R.drawable.ic_favorite_border
                )

                // Устанавливаем цвет для избранного
                favoriteButton.setColorFilter(
                    if (translation.isFavorite) root.context.getColor(R.color.favorite_red)
                    else root.context.getColor(R.color.on_surface_light)
                )

                // Желтая мусорка
                deleteButton.setColorFilter(root.context.getColor(R.color.delete_yellow))

                favoriteButton.setOnClickListener {
                    onFavoriteClick(translation)
                }

                deleteButton.setOnClickListener {
                    onDeleteClick(translation)
                }

                // Копирование перевода
                root.setOnLongClickListener {
                    val clipboard = root.context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("Перевод", translation.translatedText)
                    clipboard.setPrimaryClip(clip)

                    com.google.android.material.snackbar.Snackbar.make(
                        root,
                        "Перевод скопирован",
                        com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                    ).show()
                    true
                }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<TranslationHistory>() {
            override fun areItemsTheSame(oldItem: TranslationHistory, newItem: TranslationHistory): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: TranslationHistory, newItem: TranslationHistory): Boolean {
                return oldItem == newItem
            }
        }
    }
}