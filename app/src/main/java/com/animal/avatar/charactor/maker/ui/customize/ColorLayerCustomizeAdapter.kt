package com.animal.avatar.charactor.maker.ui.customize

import android.content.Context
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import com.animal.avatar.charactor.maker.core.base.BaseAdapter
import com.animal.avatar.charactor.maker.core.extensions.tap
import com.animal.avatar.charactor.maker.data.model.custom.ItemColorModel
import com.animal.avatar.charactor.maker.databinding.ItemColorBinding

class ColorLayerCustomizeAdapter(val context: Context) :
    BaseAdapter<ItemColorModel, ItemColorBinding>(ItemColorBinding::inflate) {
    var onItemClick: ((Int) -> Unit) = {}
    override fun onBind(binding: ItemColorBinding, item: ItemColorModel, position: Int) {
        binding.apply {
            imvImage.setBackgroundColor(item.color.toColorInt())
            imvFocus.isVisible = item.isSelected
            root.tap { onItemClick.invoke(position) }
        }
    }
}