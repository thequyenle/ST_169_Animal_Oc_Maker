package com.example.st181_halloween_maker.ui.mycreation

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.st181_halloween_maker.R

import com.example.st181_halloween_maker.core.extensions.gone
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.extensions.show
import com.girlmaker.create.avatar.creator.model.MyCreationModel
import com.example.st181_halloween_maker.core.utils.SystemUtils.shimmerDrawable
import com.example.st181_halloween_maker.databinding.ItemMyCreationBinding

class MyCreationAdapter(val context: Context) : RecyclerView.Adapter<MyCreationAdapter.MyLibraryViewHolder>() {
    private var listMyLibrary: ArrayList<MyCreationModel> = arrayListOf()
    var onItemClick: ((String) -> Unit)? = null
    var onMoreClick: ((String, Int, View) -> Unit)? = null
    var onLongClick: ((Int) -> Unit)? = null
    var onItemTick: ((Int) -> Unit)? = null

    inner class MyLibraryViewHolder(val binding: ItemMyCreationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MyCreationModel, position: Int) {
            Glide.with(binding.root).load(item.path).placeholder(shimmerDrawable).error(shimmerDrawable).into(binding.imvImage)

            if (item.isShowSelection) {
                binding.btnSelect.show()
                binding.btnMore.gone()
            } else {
                binding.btnSelect.gone()
                binding.btnMore.show()
            }

            if (item.isSelected) {
                binding.btnSelect.setImageResource(R.drawable.ic_tick_item)
            } else {
                binding.btnSelect.setImageResource(R.drawable.ic_not_tick_item)
            }

            binding.root.onSingleClick {
                onItemClick?.invoke(item.path)
            }
            binding.btnMore.onSingleClick {
//                onMoreClick?.invoke(item.path, position, it)
            }
            binding.root.setOnLongClickListener {
                onLongClick?.invoke(position)
                return@setOnLongClickListener true
            }
            binding.btnSelect.onSingleClick {
                onItemTick?.invoke(position)
            }
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