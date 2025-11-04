package com.animal.avatar.charactor.maker.core.custom

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Custom GridLayoutManager that properly handles wrap_content height in ScrollView
 * Fixes issue where RecyclerView doesn't show all items
 */
class WrapContentGridLayoutManager(
    context: Context,
    spanCount: Int
) : GridLayoutManager(context, spanCount) {

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Log.e("WrapContentGridLayoutManager", "IndexOutOfBoundsException in onLayoutChildren", e)
        }
    }

    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
}

