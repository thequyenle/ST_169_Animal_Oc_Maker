package com.animal.avatar.charactor.maker.ui.my_creation.adapter

import com.animal.avatar.charactor.maker.R
import com.animal.avatar.charactor.maker.core.base.BaseAdapter
import com.animal.avatar.charactor.maker.core.extensions.gone
import com.animal.avatar.charactor.maker.core.extensions.loadImage
import com.animal.avatar.charactor.maker.core.extensions.tap
import com.animal.avatar.charactor.maker.core.extensions.visible
import com.animal.avatar.charactor.maker.data.model.MyAlbumModel
import com.animal.avatar.charactor.maker.databinding.ItemMyDesignBinding

class MyDesignAdapter() : BaseAdapter<MyAlbumModel, ItemMyDesignBinding>(ItemMyDesignBinding::inflate) {
    var onItemClick: ((String) -> Unit) = {}
    var onLongClick: ((Int) -> Unit) = {}
    var onItemTick: ((Int) -> Unit) = {}

    var onDeleteClick: ((String) -> Unit) = {}

    override fun onBind(binding: ItemMyDesignBinding, item: MyAlbumModel, position: Int) {
        binding.apply {
            android.util.Log.d("MyDesignAdapter", "🖼️ onBind() position=$position")
            android.util.Log.d("MyDesignAdapter", "  Image path: ${item.path}")
            // Check if file exists
            val file = java.io.File(item.path)
            val exists = file.exists()
            val size = if (exists) file.length() else 0
            val lastModified = if (exists) java.util.Date(file.lastModified()) else "N/A"
            android.util.Log.d("MyDesignAdapter", "  File exists: $exists, Size: $size bytes")
            android.util.Log.d("MyDesignAdapter", "  Last modified: $lastModified")

            loadImage(root, item.path, imvImage)

            if (item.isShowSelection) {
                btnSelect.visible()
                btnDelete.gone()
            } else {
                btnSelect.gone()
                btnDelete.visible()
            }

            if (item.isSelected) {
                btnSelect.setImageResource(R.drawable.ic_selected)
            } else {
                btnSelect.setImageResource(R.drawable.ic_not_select)
            }

            root.tap { onItemClick.invoke(item.path) }

            root.setOnLongClickListener {
                if (items.any { album -> album.isShowSelection }) {
                    return@setOnLongClickListener false
                } else {
                    onLongClick.invoke(position)
                    return@setOnLongClickListener true

                }
            }
            btnDelete.tap { onDeleteClick.invoke(item.path) }
            btnSelect.tap { onItemTick.invoke(position) }
        }
    }
}