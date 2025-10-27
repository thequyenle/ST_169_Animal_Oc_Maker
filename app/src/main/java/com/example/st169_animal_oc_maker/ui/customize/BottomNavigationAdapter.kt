package com.example.st169_animal_oc_maker.ui.customize

import android.content.Context
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.st169_animal_oc_maker.core.base.BaseAdapter
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.core.utils.DataLocal
import com.example.st169_animal_oc_maker.data.custom.NavigationModel
import com.example.st169_animal_oc_maker.databinding.ItemNaviBinding
import com.facebook.shimmer.ShimmerDrawable


class BottomNavigationAdapter(val context: Context) :
    BaseAdapter<NavigationModel, ItemNaviBinding>(ItemNaviBinding::inflate) {
    var onItemClick: ((Int) -> Unit) = {}
    override fun onBind(
        binding: ItemNaviBinding, item: NavigationModel, position: Int
    ) {
        val shimmerDrawable = ShimmerDrawable().apply {
            setShimmer(DataLocal.shimmer)
        }
        binding.apply {
            vFocus.isVisible = item.isSelected
            Glide.with(root).load(item.imageNavigation).placeholder(shimmerDrawable).into(imvImage)
            root.onSingleClick { onItemClick.invoke(position) }
        }
    }
}