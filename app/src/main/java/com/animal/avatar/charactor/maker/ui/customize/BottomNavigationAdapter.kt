package com.animal.avatar.charactor.maker.ui.customize

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.animal.avatar.charactor.maker.R
import com.animal.avatar.charactor.maker.core.base.BaseAdapter
import com.animal.avatar.charactor.maker.core.extensions.gone
import com.animal.avatar.charactor.maker.core.extensions.invisible
import com.animal.avatar.charactor.maker.core.extensions.onSingleClick
import com.animal.avatar.charactor.maker.core.utils.DataLocal
import com.animal.avatar.charactor.maker.data.custom.NavigationModel
import com.animal.avatar.charactor.maker.databinding.ItemNaviBinding
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