package com.example.st181_halloween_maker.ui.background

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.example.st181_halloween_maker.core.extensions.gone
import com.example.st181_halloween_maker.core.extensions.show
import com.girlmaker.create.avatar.creator.model.BackGroundModel
import com.example.st181_halloween_maker.core.utils.KeyApp.BODY
import com.example.st181_halloween_maker.core.utils.SystemUtils.shimmerDrawable
import com.example.st181_halloween_maker.databinding.ItemBackgroundBinding
import kotlin.apply
import kotlin.text.contains

class BackgroundAdapter(private val context: Context) : RecyclerView.Adapter<BackgroundAdapter.BackgroundViewHolder>() {
    private val itemList: ArrayList<BackGroundModel> = arrayListOf()
    var onItemClick: ((BackGroundModel, Int) -> Unit)? = null
    var onNoneClick: ((Int) -> Unit)? = null

    inner class BackgroundViewHolder(private val binding: ItemBackgroundBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BackGroundModel, position: Int) {
            val isBody = item.path.contains("/$BODY/")
            when (position) {
                0 -> {
                    if (isBody) {
                        binding.btnNone.gone()
                        binding.imvImage.gone()
                    } else {
                        binding.btnNone.show()
                        binding.imvImage.gone()
                    }
                }

                else -> {
                    binding.btnNone.gone()
                    binding.imvImage.show()
                    Glide.with(binding.root).load(item.path).placeholder(shimmerDrawable).error(shimmerDrawable).into(binding.imvImage)
                }
            }

            if (item.isSelected) {
               binding.layoutFocus1.show()
            } else {
                binding.layoutFocus1.gone()
            }

            binding.imvImage.setOnClickListener {
                onItemClick?.invoke(item, position)
            }

            binding.btnNone.setOnClickListener {
                onNoneClick?.invoke(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackgroundViewHolder {
        return BackgroundViewHolder(ItemBackgroundBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BackgroundViewHolder, position: Int) {
        holder.bind(itemList[position], position)
    }

    override fun getItemCount(): Int = itemList.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: ArrayList<BackGroundModel>) {
        itemList.clear()
        itemList.addAll(list)
        notifyDataSetChanged()
    }

    val currentList: ArrayList<BackGroundModel>
        get() = itemList
    fun getCurrentList(): List<BackGroundModel> = itemList

}

