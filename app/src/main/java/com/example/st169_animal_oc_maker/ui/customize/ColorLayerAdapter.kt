package com.example.st169_animal_oc_maker.ui.customize

import android.content.Context
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import com.example.st169_animal_oc_maker.R
import com.example.st169_animal_oc_maker.core.base.BaseAdapter
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.data.custom.ItemColorModel
import com.example.st169_animal_oc_maker.databinding.ItemColorBinding


class ColorLayerAdapter(val context: Context) :
    BaseAdapter<ItemColorModel, ItemColorBinding>(ItemColorBinding::inflate) {
    var onItemClick: ((Int) -> Unit) = {}
    var categoryPosition: Int = 0 // Thêm biến này
    var isEnabled: Boolean = true // Biến để kiểm soát enable/disable

    override fun onBind(binding: ItemColorBinding, item: ItemColorModel, position: Int) {
        binding.apply {
            imvImage.setBackgroundColor(item.color.toColorInt())
            layoutFocus.isVisible = item.isSelected

            // Set background dựa trên categoryPosition
            val focusBackground = when(categoryPosition) {
                0 -> R.drawable.bg_item_color_1
                1 -> R.drawable.bg_item_color_2
                2 -> R.drawable.bg_item_color_3
                else -> R.drawable.bg_item_color_1
            }
            layoutFocus.setImageResource(focusBackground)

            // Chỉ cho phép click nếu isEnabled = true
            root.onSingleClick {
                if (isEnabled) {
                    onItemClick.invoke(position)
                }
            }
        }
    }
}