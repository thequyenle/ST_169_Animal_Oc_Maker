package com.example.st169_animal_oc_maker.ui.suggestion

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.st169_animal_oc_maker.data.suggestion.SuggestionModel
import com.example.st169_animal_oc_maker.databinding.ItemSuggestionBinding

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
                binding.imvSuggestion.setImageBitmap(thumbnail)
            } else {
                Glide.with(binding.root.context)
                    .load(suggestion.characterData)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.imvSuggestion)
            }

            binding.cardSuggestion.setOnClickListener {
                onItemClick(suggestion)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SuggestionModel>() {
        override fun areItemsTheSame(oldItem: SuggestionModel, newItem: SuggestionModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SuggestionModel, newItem: SuggestionModel): Boolean {
            return oldItem == newItem
        }
    }
}