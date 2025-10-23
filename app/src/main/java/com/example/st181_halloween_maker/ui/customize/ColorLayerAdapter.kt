package com.example.st181_halloween_maker.ui.customize

import android.content.Context
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import com.example.st181_halloween_maker.core.base.BaseAdapter
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.data.custom.ItemColorModel
import com.example.st181_halloween_maker.databinding.ItemColorBinding


class ColorLayerAdapter(val context: Context) :
    BaseAdapter<ItemColorModel, ItemColorBinding>(ItemColorBinding::inflate) {
    var onItemClick: ((Int) -> Unit) = {}
    override fun onBind(binding: ItemColorBinding, item: ItemColorModel, position: Int) {
        binding.apply {
            imvImage.setBackgroundColor(item.color.toColorInt())
            layoutFocus.isVisible = item.isSelected
            root.onSingleClick { onItemClick.invoke(position) }
        }
    }
}