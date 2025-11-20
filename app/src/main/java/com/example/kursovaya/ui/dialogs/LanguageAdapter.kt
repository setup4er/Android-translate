package com.example.kursovaya.ui.dialogs

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kursovaya.R
import com.google.android.material.card.MaterialCardView

class LanguageAdapter(
    private val languages: List<Pair<String, String>>,
    private val onLanguageSelected: (String, String) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language, parent, false)
        return LanguageViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val (languageName, languageCode) = languages[position]
        holder.bind(languageName, languageCode)
    }

    override fun getItemCount(): Int = languages.size

    inner class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val languageName: TextView = itemView.findViewById(R.id.languageName)
        private val languageCard: MaterialCardView = itemView.findViewById(R.id.languageCard)

        fun bind(name: String, code: String) {
            languageName.text = name
            languageCard.setOnClickListener {
                onLanguageSelected(name, code)
            }
        }
    }
}