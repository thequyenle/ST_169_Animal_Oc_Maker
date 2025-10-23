package com.example.st181_halloween_maker.ui.lissticker

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
import kotlin.collections.ArrayList


class ListStickerAdapter(val context: Context): RecyclerView.Adapter<ListStickerAdapter.ListStickerViewHolder>() {
    private val avatarList: ArrayList<String> = arrayListOf()
    var onItemClick: ((String,Int) -> Unit)? = null

    inner class ListStickerViewHolder(private val binding : ItemListStickerBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(path:String, position: Int){
                Glide.with(binding.root).load(path).placeholder(shimmerDrawable).error(shimmerDrawable).into(binding.imvImage)

            binding.root.onSingleClick {
                onItemClick?.invoke(path,position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListStickerViewHolder {
        val binding = ItemListStickerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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