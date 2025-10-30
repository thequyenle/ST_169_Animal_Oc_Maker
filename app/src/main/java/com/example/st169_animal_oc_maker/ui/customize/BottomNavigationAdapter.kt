package com.example.st169_animal_oc_maker.ui.customize

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.st169_animal_oc_maker.R
import com.example.st169_animal_oc_maker.core.base.BaseAdapter
import com.example.st169_animal_oc_maker.core.extensions.gone
import com.example.st169_animal_oc_maker.core.extensions.invisible
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.core.utils.DataLocal
import com.example.st169_animal_oc_maker.data.custom.NavigationModel
import com.example.st169_animal_oc_maker.databinding.ItemNaviBinding
import com.facebook.shimmer.ShimmerDrawable


class BottomNavigationAdapter(val context: Context) :
    BaseAdapter<NavigationModel, ItemNaviBinding>(ItemNaviBinding::inflate) {
    var onItemClick: ((Int) -> Unit) = {}

    // ✅ PERFORMANCE: Cache ShimmerDrawable to avoid creating new instance on every bind
    private val shimmerDrawable: ShimmerDrawable by lazy {
        ShimmerDrawable().apply {
            setShimmer(DataLocal.shimmer)
        }
    }

    // ✅ PERFORMANCE: Cache colors to avoid repeated resource lookups
    private val pinkColor: Int by lazy {
        ContextCompat.getColor(context, R.color.pink)
    }

    private val whiteColor: Int by lazy {
        ContextCompat.getColor(context, R.color.white)
    }

    override fun onBind(
        binding: ItemNaviBinding, item: NavigationModel, position: Int
    ) {
        binding.apply {
            if (item.isSelected) {
                // Khi selected: hiện stroke màu pink và background decoration
                cvFocus.setCardBackgroundColor(pinkColor)
                imvSelected.isVisible = true
            } else {
                // Khi không selected: stroke màu white và ẩn background decoration
                cvFocus.setCardBackgroundColor(whiteColor)
                imvSelected.gone()
            }

            // ✅ PERFORMANCE: Add Glide optimizations for faster loading
            Glide.with(root)
                .load(item.imageNavigation)
                .placeholder(shimmerDrawable)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .into(imvImage)

            root.onSingleClick { onItemClick.invoke(position) }
        }
    }
}