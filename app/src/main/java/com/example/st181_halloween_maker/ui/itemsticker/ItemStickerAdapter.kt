package com.example.st181_halloween_maker.ui.itemsticker

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.utils.SystemUtils.shimmerDrawable
import com.example.st181_halloween_maker.databinding.ItemCategoryBinding
import com.example.st181_halloween_maker.databinding.ItemListStickerBinding
import com.example.st181_halloween_maker.databinding.ItemStickerBinding
import kotlin.collections.ArrayList


class ItemStickerAdapter(val context: Context): RecyclerView.Adapter<ItemStickerAdapter.ListStickerViewHolder>() {
    private val avatarList: ArrayList<String> = arrayListOf()
    var onItemClick: ((String,Int) -> Unit)? = null

    inner class ListStickerViewHolder(private val binding : ItemStickerBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(path:String, position: Int){
                Glide.with(binding.root).load(path).placeholder(shimmerDrawable).error(shimmerDrawable).into(binding.imvImage)

            binding.root.onSingleClick {
                onItemClick?.invoke(path,position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListStickerViewHolder {
        val binding = ItemStickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListStickerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListStickerViewHolder, position: Int) {
        val item = avatarList[position]
        holder.bind(item,position)
    }

    override fun getItemCount(): Int {
        return avatarList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: ArrayList<String>) {
        avatarList.clear()
        avatarList.addAll(list)
        notifyDataSetChanged()
    }


}