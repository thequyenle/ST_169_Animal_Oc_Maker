package com.animal.avatar.charactor.maker.ui.suggestion

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.animal.avatar.charactor.maker.data.suggestion.SuggestionModel
import com.animal.avatar.charactor.maker.databinding.ItemSuggestionBinding
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable

class SuggestionAdapter(
    private val onItemClick: (SuggestionModel) -> Unit
) : ListAdapter<SuggestionModel, SuggestionAdapter.ViewHolder>(DiffCallback()) {

    private var thumbnails: Map<String, Bitmap> = emptyMap()

    fun updateThumbnails(newThumbnails: Map<String, Bitmap>) {
        thumbnails = newThumbnails
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSuggestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemSuggestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(suggestion: SuggestionModel) {
            val thumbnail = thumbnails[suggestion.id]

            if (thumbnail != null) {
                binding.sm.visibility = View.GONE
                binding.imvSuggestion.setImageBitmap(thumbnail)
            } else {
                binding.sm.visibility = View.VISIBLE
//                Glide.with(binding.root.context)
//                    .load(suggestion.characterData)
//                    .placeholder(getShimmerPlaceholder())
//                    .into(binding.imvSuggestion)
            }

            binding.cardSuggestion.setOnClickListener {
                onItemClick(suggestion)
            }
        }
    }

    private fun getShimmerPlaceholder(): Drawable {
        val shimmer = Shimmer.ColorHighlightBuilder()
            .setBaseColor(Color.parseColor("#E0E0E0"))
            .setHighlightColor(Color.parseColor("#F5F5F5"))
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .setAutoStart(true)
            .build()

        return ShimmerDrawable().apply {
            setShimmer(shimmer)
        }

    }
        private class DiffCallback : DiffUtil.ItemCallback<SuggestionModel>() {
            override fun areItemsTheSame(
                oldItem: SuggestionModel,
                newItem: SuggestionModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: SuggestionModel,
                newItem: SuggestionModel
            ): Boolean {
                return oldItem == newItem
            }
        }

}