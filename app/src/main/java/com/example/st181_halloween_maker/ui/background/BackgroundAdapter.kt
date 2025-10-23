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
import com.example.st181_halloween_maker.databinding.ItemBackgorundBinding
import kotlin.apply
import kotlin.text.contains

class BackgroundAdapter(private val context: Context) : RecyclerView.Adapter<BackgroundAdapter.BackgroundViewHolder>() {
    private val itemList: ArrayList<BackGroundModel> = arrayListOf()
    var onItemClick: ((BackGroundModel, Int) -> Unit)? = null
    var onNoneClick: ((Int) -> Unit)? = null

    inner class BackgroundViewHolder(val binding: ItemBackgorundBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BackGroundModel, position: Int) {
            binding.apply {
                val isBody = item.path.contains("/$BODY/")
                when (position) {
                    0 -> {
                        if (isBody) {
                            btnNone.gone()
                            imvImage.gone()
                        } else {
                            btnNone.show()
                            imvImage.gone()
                        }
                    }

                    else -> {
                        btnNone.gone()
                        imvImage.show()
                        Glide.with(root).load(item.path).placeholder(shimmerDrawable).error(shimmerDrawable).into(imvImage)
                    }
                }

                if (item.isSelected) {
                   binding.layoutFocus1.show()
                } else {
                    binding.layoutFocus1.gone()
                }

                imvImage.setOnClickListener {
                    onItemClick?.invoke(item, position)
                }

                btnNone.setOnClickListener {
                    onNoneClick?.invoke(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackgroundViewHolder {
        return BackgroundViewHolder(ItemBackgorundBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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

