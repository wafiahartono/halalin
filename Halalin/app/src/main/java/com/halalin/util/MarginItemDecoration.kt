package com.halalin.util

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class MarginItemDecoration(
    private val orientation: Orientation,
    private val column: Int,
    private val spacing: Int
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        outRect.left = 0
        outRect.top = 0
        outRect.right = 0
        outRect.bottom = 0
        when (orientation) {
            Orientation.HORIZONTAL -> {
                outRect.left = if (position > 0) spacing else 0
            }
            Orientation.VERTICAL -> {
                outRect.top = if (position > column - 1) spacing else 0
                outRect.right = if (position % column != column - 1) spacing else 0
            }
        }
    }

    enum class Orientation {
        HORIZONTAL, VERTICAL
    }
}
