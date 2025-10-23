package com.example.st181_halloween_maker.ui.category

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.utils.DataLocal.shimmer
import com.example.st181_halloween_maker.data.custom.CustomizeModel
import com.example.st181_halloween_maker.databinding.ItemCategoryBinding
import com.facebook.shimmer.ShimmerDrawable
import kotlin.collections.ArrayList


class CategoryAdapter(val context: Context): RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    private val avatarList: ArrayList<CustomizeModel> = arrayListOf()
    var onItemClick: ((String, Int) -> Unit)? = null

    inner class CategoryViewHolder(private val binding : ItemCategoryBinding): RecyclerView.ViewHolder(binding.root){
        fun bind(item: CustomizeModel, position: Int){
            val shimmerDrawable = ShimmerDrawable().apply {
                setShimmer(shimmer)
            }
            Glide.with(binding.root).load(item.avatar).placeholder(shimmerDrawable).error(shimmerDrawable).into(binding.imvImage)

            binding.root.onSingleClick {
                onItemClick?.invoke(item.avatar, position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = avatarList[position]
        holder.bind(item,position)
    }

    override fun getItemCount(): Int {
        return avatarList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: ArrayList<CustomizeModel>) {
        avatarList.clear()
        avatarList.addAll(list)
        notifyDataSetChanged()
    }


}