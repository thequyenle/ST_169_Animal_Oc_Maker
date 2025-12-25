package com.animal.avatar.charactor.maker.ui.add_character.adapter

import com.animal.avatar.charactor.maker.core.base.BaseAdapter
import com.animal.avatar.charactor.maker.core.extensions.loadImage
import com.animal.avatar.charactor.maker.core.extensions.tap
import com.animal.avatar.charactor.maker.data.model.SelectedModel
import com.animal.avatar.charactor.maker.databinding.ItemStickerBinding

class StickerAdapter : BaseAdapter<SelectedModel, ItemStickerBinding>(ItemStickerBinding::inflate) {
    var onItemClick : ((String) -> Unit) = {}
    override fun onBind(binding: ItemStickerBinding, item: SelectedModel, position: Int) {
        binding.apply {
            loadImage(root, item.path, imvSticker)
            root.tap { onItemClick.invoke(item.path) }
        }
    }
}