package com.example.st169_animal_oc_maker.ui.mycreation

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.st169_animal_oc_maker.R
import com.example.st169_animal_oc_maker.core.dialog.ItemOptionsPopup
import com.example.st169_animal_oc_maker.core.extensions.gone
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.core.extensions.show
import com.girlmaker.create.avatar.creator.model.MyCreationModel
import com.example.st169_animal_oc_maker.core.utils.SystemUtils.shimmerDrawable
import com.example.st169_animal_oc_maker.databinding.ItemMyCreationBinding

class MyCreationAdapter(val context: Context) : RecyclerView.Adapter<MyCreationAdapter.MyLibraryViewHolder>() {
    private var listMyLibrary: ArrayList<MyCreationModel> = arrayListOf()
    var onItemClick: ((String) -> Unit)? = null
    var onDeleteClick: ((String, Int) -> Unit)? = null
    var onShareClick: ((String) -> Unit)? = null
    var onDownloadClick: ((String) -> Unit)? = null
    var onLongClick: ((Int) -> Unit)? = null
    var onItemTick: ((Int) -> Unit)? = null

    inner class MyLibraryViewHolder(val binding: ItemMyCreationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MyCreationModel, position: Int) {
            Glide.with(binding.root).load(item.path).placeholder(shimmerDrawable).error(shimmerDrawable).into(binding.imvImage)

            // Toggle between btnMore and imgCheck based on selection mode
            if (item.isShowSelection) {
                binding.btnMore.gone()
                binding.imgCheck.show()
                binding.btnSelect.gone()

                // Update imgCheck icon based on selection state
                if (item.isSelected) {
                    binding.imgCheck.setImageResource(R.drawable.ic_check)
                } else {
                    binding.imgCheck.setImageResource(R.drawable.ic_uncheck)
                }
            } else {
                binding.btnMore.show()
                binding.imgCheck.gone()
                binding.btnSelect.gone()
            }

            // Handle root click
            binding.root.onSingleClick {
                if (item.isShowSelection) {
                    // In selection mode, clicking item toggles selection
                    onItemTick?.invoke(position)
                } else {
                    // Normal mode, open item
                    onItemClick?.invoke(item.path)
                }
            }

            // Handle btnMore click (show popup menu)
            binding.btnMore.onSingleClick {
                showOptionsPopup(binding.root, item.path, position)
            }

            // Handle long click to enter selection mode
            binding.root.setOnLongClickListener {
                onLongClick?.invoke(position)
                return@setOnLongClickListener true
            }

            // Handle imgCheck click to toggle selection
            binding.imgCheck.onSingleClick {
                onItemTick?.invoke(position)
            }
        }

        private fun showOptionsPopup(itemRootView: View, imagePath: String, position: Int) {
            val popup = ItemOptionsPopup(context)

            popup.onDeleteClick = {
                onDeleteClick?.invoke(imagePath, position)
            }

            popup.onShareClick = {
                onShareClick?.invoke(imagePath)
            }

            popup.onDownloadClick = {
                onDownloadClick?.invoke(imagePath)
            }

            popup.showAtLocation(itemRootView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyLibraryViewHolder {
        return MyLibraryViewHolder(ItemMyCreationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        )
    }

    override fun getItemCount(): Int {
        return listMyLibrary.size
    }

    override fun onBindViewHolder(holder: MyLibraryViewHolder, position: Int) {
        val item = listMyLibrary[position]
        holder.bind(item, position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: ArrayList<MyCreationModel>) {
        listMyLibrary.clear()
        listMyLibrary.addAll(list)
        notifyDataSetChanged()
    }

    fun submitItem(position: Int, isSelect: Boolean) {
        listMyLibrary[position].isSelected = isSelect
        notifyItemChanged(position)
    }
}