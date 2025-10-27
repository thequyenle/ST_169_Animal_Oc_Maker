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
    override fun onBind(
        binding: ItemNaviBinding, item: NavigationModel, position: Int
    ) {
        val shimmerDrawable = ShimmerDrawable().apply {
            setShimmer(DataLocal.shimmer)
        }
        binding.apply {

            if (item.isSelected) {
                // Khi selected: hiện stroke màu pink và background decoration
                cvFocus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.pink))
                imvSelected.isVisible = true
            } else {
                // Khi không selected: stroke màu white và ẩn background decoration
                cvFocus.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white))
                imvSelected.gone()
            }
            Glide.with(root).load(item.imageNavigation).placeholder(shimmerDrawable).into(imvImage)
            root.onSingleClick { onItemClick.invoke(position) }
        }
    }
}