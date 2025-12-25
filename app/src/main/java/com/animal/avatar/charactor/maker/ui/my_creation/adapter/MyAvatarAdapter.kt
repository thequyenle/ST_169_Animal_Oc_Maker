package com.animal.avatar.charactor.maker.ui.my_creation.adapter

import android.content.Context
import com.animal.avatar.charactor.maker.R
import com.animal.avatar.charactor.maker.core.base.BaseAdapter
import com.animal.avatar.charactor.maker.core.extensions.gone
import com.animal.avatar.charactor.maker.core.extensions.loadImage
import com.animal.avatar.charactor.maker.core.extensions.loadImageFromFile
import com.animal.avatar.charactor.maker.core.extensions.tap
import com.animal.avatar.charactor.maker.core.extensions.visible
import com.animal.avatar.charactor.maker.data.model.MyAlbumModel
import com.animal.avatar.charactor.maker.databinding.ItemMyAlbumBinding

class MyAvatarAdapter(val context: Context) :
    BaseAdapter<MyAlbumModel, ItemMyAlbumBinding>(ItemMyAlbumBinding::inflate) {
    var onItemClick: ((String) -> Unit) = {}
    var onLongClick: ((Int) -> Unit) = {}
    var onItemTick: ((Int) -> Unit) = {}

    var onEditClick: ((String) -> Unit) = {}
    var onDeleteClick: ((String) -> Unit) = {}

    var isSelectMode: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onBind(binding: ItemMyAlbumBinding, item: MyAlbumModel, position: Int) {
        binding.apply {
            android.util.Log.d("MyAvatarAdapter", "🖼️ onBind() position=$position")
            android.util.Log.d("MyAvatarAdapter", "  Image path: ${item.path}")
            // Check if file exists
            val file = java.io.File(item.path)
            val exists = file.exists()
            val size = if (exists) file.length() else 0
            val lastModified = if (exists) java.util.Date(file.lastModified()) else "N/A"
            android.util.Log.d("MyAvatarAdapter", "  File exists: $exists, Size: $size bytes")
            android.util.Log.d("MyAvatarAdapter", "  Last modified: $lastModified")
            android.util.Log.d("MyAvatarAdapter", "  Using loadImageFromFile() with cache invalidation")

            imvImage.loadImageFromFile(item.path)

            if (item.isShowSelection) {
                btnSelect.visible()
                btnEdit.gone()
                btnDelete.gone()
            } else {
                btnSelect.gone()
                btnEdit.visible()
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
            btnEdit.tap { onEditClick.invoke(item.path) }
            btnDelete.tap { onDeleteClick.invoke(item.path) }
            btnSelect.tap { onItemTick.invoke(position) }
        }
    }
}