package com.example.st181_halloween_maker.ui.language

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.databinding.ItemLanguageBinding

import com.girlmaker.create.avatar.creator.model.LanguageModel
import kotlin.apply
import kotlin.collections.forEach

class LanguageAdapter(val context: Context): RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {
    private val languageList = kotlin.collections.ArrayList<LanguageModel>()
    private var currentActive = 0
    var onItemClick:((LanguageModel)-> Unit)? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        return LanguageViewHolder(ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val item = languageList[position]
        holder.bind(item,position)
    }

    override fun getItemCount(): Int {
        return languageList.size
    }

    inner class LanguageViewHolder(private val binding: ItemLanguageBinding): RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ResourceAsColor") fun bind(item: LanguageModel, position: Int) {
            binding.apply {
                imvFlag.setImageResource(item.flag)
                txtLang.text = item.name
                if (item.activate) {
                    rdbLang.setImageResource(R.drawable.ic_tick_lang)
                    txtLang.setTextColor(ContextCompat.getColor(context, R.color.orange))
                    itemLang.background = ContextCompat.getDrawable(context, R.drawable.bg_tick_lang)
                } else {
                    rdbLang.setImageResource(R.drawable.ic_not_tick_lang)
                    txtLang.setTextColor(ContextCompat.getColor(context, R.color.orange))
                    itemLang.background = ContextCompat.getDrawable(context, R.drawable.bg_tick_lang)

                }
                itemLang.setOnClickListener {
                    languageList[currentActive].activate = false
                    notifyItemChanged(currentActive)
                    currentActive = position
                    languageList[currentActive].activate = true
                    notifyItemChanged(currentActive)
                    onItemClick?.invoke(item)
                }
            }

        }

    }
    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: ArrayList<LanguageModel>){
        languageList.clear()
        languageList.addAll(list)
        notifyDataSetChanged()
    }

    // Duyệt qua ds các phần tử
    fun submitItem(code: String) {
        languageList.forEach {
            it.activate = it.code == code
        }
    }
}