package com.animal.avatar.charactor.maker.ui.intro

import android.content.Context
import com.animal.avatar.charactor.maker.core.base.BaseAdapter
import com.animal.avatar.charactor.maker.core.extensions.loadImage
import com.animal.avatar.charactor.maker.core.extensions.select
import com.animal.avatar.charactor.maker.core.extensions.strings
import com.animal.avatar.charactor.maker.data.model.IntroModel
import com.animal.avatar.charactor.maker.databinding.ItemIntroBinding

class IntroAdapter(val context: Context) : BaseAdapter<IntroModel, ItemIntroBinding>(
    ItemIntroBinding::inflate
) {
    override fun onBind(binding: ItemIntroBinding, item: IntroModel, position: Int) {
        binding.apply {
            loadImage(root, item.image, imvImage, false)
            tvContent.text = context.strings(item.content)
            tvContent.select()
        }
    }
}