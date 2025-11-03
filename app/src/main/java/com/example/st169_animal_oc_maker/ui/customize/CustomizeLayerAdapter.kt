package com.animal.avatar.charactor.maker.ui.customize

import android.content.Context
import com.bumptech.glide.Glide
import com.animal.avatar.charactor.maker.core.base.BaseAdapter
import com.animal.avatar.charactor.maker.core.extensions.gone
import com.animal.avatar.charactor.maker.core.extensions.onSingleClick
import com.animal.avatar.charactor.maker.core.extensions.visible
import com.animal.avatar.charactor.maker.core.utils.DataLocal
import com.animal.avatar.charactor.maker.core.utils.key.AssetsKey
import com.animal.avatar.charactor.maker.data.custom.ItemNavCustomModel
import com.animal.avatar.charactor.maker.databinding.ItemCusBinding
import com.facebook.shimmer.ShimmerDrawable


class CustomizeLayerAdapter(val context: Context) :
    BaseAdapter<ItemNavCustomModel, ItemCusBinding>(ItemCusBinding::inflate) {

    var onItemClick: ((ItemNavCustomModel, Int) -> Unit) = { _, _ -> }
    var onNoneClick: ((Int) -> Unit) = {}
    var onRandomClick: (() -> Unit) = {}

    private val shimmerDrawable: ShimmerDrawable by lazy {
        ShimmerDrawable().apply {
            setShimmer(DataLocal.shimmer)
        }
    }

    override fun onBind(
        binding: ItemCusBinding,
        item: ItemNavCustomModel,
        position: Int
    ) {
        binding.apply {
            // ✅ PERFORMANCE: Set selection state first (cheaper operation)
            vFocus.isSelected = item.isSelected

            when (item.path) {
                AssetsKey.NONE_LAYER -> {
                    btnNone.visible()
                    btnRandom.gone()
                    imvImage.gone()
                }
                AssetsKey.RANDOM_LAYER -> {
                    btnNone.gone()
                    btnRandom.visible()
                    imvImage.gone()
                }
                else -> {
                    btnNone.gone()
                    imvImage.visible()
                    btnRandom.gone()
                    // ✅ PERFORMANCE: Add Glide optimizations for faster loading
                    Glide.with(root)
                        .load(item.path)
                        .placeholder(shimmerDrawable)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                        .skipMemoryCache(false)
                        .into(imvImage)
                }
            }

            // ✅ PERFORMANCE: Set click listeners (these are cheap, no need to optimize)
            imvImage.onSingleClick { onItemClick.invoke(item, position) }
            btnRandom.onSingleClick { onRandomClick.invoke() }
            btnNone.onSingleClick { onNoneClick.invoke(position) }
        }
    }
}