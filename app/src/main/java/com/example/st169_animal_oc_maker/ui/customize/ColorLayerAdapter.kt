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

    fun submitListWithLog(list: List<ItemColorModel>) {
        android.util.Log.d("ColorLayerAdapter", "📊 submitList called: size=${list.size}")
        list.forEachIndexed { index, item ->
            android.util.Log.d("ColorLayerAdapter", "  [$index] color=${item.color}, isSelected=${item.isSelected}")
        }
        submitList(list)
        android.util.Log.d("ColorLayerAdapter", "✅ submitList completed")
    }

    override fun onBind(binding: ItemColorBinding, item: ItemColorModel, position: Int) {
        binding.apply {
            // ✅ FIX: Ensure proper layout params for Android 8
            root.layoutParams = root.layoutParams?.apply {
                width = context.resources.getDimensionPixelSize(R.dimen.color_item_size)
                height = context.resources.getDimensionPixelSize(R.dimen.color_item_size)
            } ?: android.view.ViewGroup.LayoutParams(
                context.resources.getDimensionPixelSize(R.dimen.color_item_size),
                context.resources.getDimensionPixelSize(R.dimen.color_item_size)
            )

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

                android.util.Log.d("ColorLayerAdapter", "✅ Color parsed: $colorString at position $position")
            } catch (e: Exception) {
                // Fallback to white if color parsing fails
                android.util.Log.e("ColorLayerAdapter", "❌ Failed to parse color: ${item.color} at position $position", e)
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