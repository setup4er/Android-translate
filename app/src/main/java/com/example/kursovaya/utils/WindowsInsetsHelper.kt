package com.example.kursovaya.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.view.updateMargins

object WindowInsetsHelper {

    fun addBottomPaddingToView(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                bottom = systemBars.bottom + 16.dpToPx(v.context)
            )
            insets
        }
    }

    fun addBottomMarginToView(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val layoutParams = v.layoutParams as? ViewGroup.MarginLayoutParams
            layoutParams?.let {
                it.bottomMargin = systemBars.bottom + 16.dpToPx(v.context)
                v.layoutParams = it
            }
            insets
        }
    }

    fun addBottomPaddingToRecyclerView(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                bottom = systemBars.bottom + 8.dpToPx(v.context)
            )
            insets
        }
    }

    private fun Int.dpToPx(context: android.content.Context): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}