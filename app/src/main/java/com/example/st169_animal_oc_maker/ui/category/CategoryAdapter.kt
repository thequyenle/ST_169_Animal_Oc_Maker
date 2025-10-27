package com.example.st169_animal_oc_maker.ui.category

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.st169_animal_oc_maker.R
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.core.utils.DataLocal.shimmer
import com.example.st169_animal_oc_maker.data.custom.CustomizeModel
import com.example.st169_animal_oc_maker.databinding.ItemCategoryBinding
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

            // Set frame based on position
            val frameRes = when(position) {
                0 -> R.drawable.img_tommy_make_character
                1 -> R.drawable.img_miley_make_character
                2 -> R.drawable.img_dammy_make_character
                else -> R.drawable.img_tommy_make_character // Default
            }
            binding.imvFrame.setImageResource(frameRes)

            // Load content image
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