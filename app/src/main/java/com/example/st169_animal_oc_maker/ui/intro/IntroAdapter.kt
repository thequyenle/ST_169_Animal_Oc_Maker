package com.example.st169_animal_oc_maker.ui.intro

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginTop
import androidx.recyclerview.widget.RecyclerView
import com.example.st169_animal_oc_maker.R
import com.example.st169_animal_oc_maker.core.extensions.select
import com.example.st169_animal_oc_maker.databinding.ItemIntroBinding
import com.girlmaker.create.avatar.creator.model.IntroModel

import kotlin.apply

class IntroAdapter(val context: Context, private val items: List<IntroModel>) :
    RecyclerView.Adapter<IntroAdapter.ItemIntroViewHolder>() {

    inner class ItemIntroViewHolder(private val binding: ItemIntroBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: IntroModel) {
            binding.apply {
                imvImage.setImageResource(item.image)

                // Chỉ giảm chiều cao cho intro1
                if (item.image == R.drawable.intro1) {
                    val params = imvImage.layoutParams as ConstraintLayout.LayoutParams
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT
                    params.height = (context.resources.displayMetrics.heightPixels * 0.7).toInt()
                    params.topMargin = (40 * context.resources.displayMetrics.density).toInt()
                    imvImage.layoutParams = params
                } else {
                    val params = imvImage.layoutParams as ConstraintLayout.LayoutParams
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                    params.topMargin = 0
                    imvImage.layoutParams = params
                }

                txtContent.text = ContextCompat.getString(context, item.content)
                txtContent.typeface = ResourcesCompat.getFont(context, R.font.coiny_regular)
                txtContent.select()

            }
        }
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): ItemIntroViewHolder {
        val binding = ItemIntroBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemIntroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemIntroViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}