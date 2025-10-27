package com.example.st169_animal_oc_maker.ui.language

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.st169_animal_oc_maker.R
import com.example.st169_animal_oc_maker.databinding.ItemLanguageBinding

import com.girlmaker.create.avatar.creator.model.LanguageModel
import kotlin.apply

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
                    rdbLang.setImageResource(R.drawable.ic_tick_lang_white)
                    txtLang.setTextColor(ContextCompat.getColor(context, R.color.white))
                    itemLang.background = ContextCompat.getDrawable(context, R.drawable.bg_lang_selected)
                } else {
                    rdbLang.setImageResource(R.drawable.ic_not_tick_lang_pink)
                    txtLang.setTextColor(ContextCompat.getColor(context, R.color.black))
                    itemLang.background = ContextCompat.getDrawable(context, R.drawable.bg_lang_unselected)

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
        // Tìm vị trí của item đang active để cập nhật currentActive
        for(i in languageList.indices){
            if(languageList[i].activate){
                currentActive = i
                break
            }
        }
        notifyDataSetChanged()
    }
}