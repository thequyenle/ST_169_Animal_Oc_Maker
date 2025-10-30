package com.example.st169_animal_oc_maker.ui.customize

import android.content.Context
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

    // Cache dimension size to avoid repeated lookups
    private val colorItemSize: Int by lazy {
        context.resources.getDimensionPixelSize(R.dimen.color_item_size)
    }

    fun submitListWithLog(list: List<ItemColorModel>) {
        // Logging disabled for performance
        submitList(list)
    }

    override fun onBind(binding: ItemColorBinding, item: ItemColorModel, position: Int) {
        binding.apply {
            // ✅ FIX: Ensure proper layout params for Android 8 (using cached size)
            root.layoutParams = root.layoutParams?.apply {
                width = colorItemSize
                height = colorItemSize
            } ?: android.view.ViewGroup.LayoutParams(colorItemSize, colorItemSize)

            // ✅ FIX: Add try-catch for Android 8 compatibility
            try {
                // Parse color string - ensure it has # prefix
                val colorString = if (item.color.startsWith("#")) {
                    item.color
                } else {
                    "#${item.color}"
                }

                val colorInt = android.graphics.Color.parseColor(colorString)
                imvImage.setBackgroundColor(colorInt)
            } catch (e: Exception) {
                // Fallback to white if color parsing fails (logging disabled for performance)
                imvImage.setBackgroundColor(android.graphics.Color.WHITE)
            }

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