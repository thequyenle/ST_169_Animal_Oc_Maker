package com.example.st181_halloween_maker.ui.itemsticker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil.setContentView
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseActivity
import com.example.st181_halloween_maker.core.extensions.handleBack
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.utils.KeyApp.HALLOWEEN_KEY
import com.example.st181_halloween_maker.databinding.ActivityItemStickerBinding

class ItemStickerActivity : BaseActivity<ActivityItemStickerBinding>() {
    private val stickerList = ArrayList<String>()
    private val stickerAdapter by lazy { ItemStickerAdapter(this) }
    override fun setViewBinding(): ActivityItemStickerBinding {
        return ActivityItemStickerBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initData()
        initRcv()
    }

    override fun viewListener() {
        binding.apply {
            btnBack.onSingleClick {
                handleBack()
            }
        }
    }

    override fun initText() {

    }
    private fun initData(){
        val folder = intent.getStringExtra(HALLOWEEN_KEY) ?:return
        stickerList.clear()
        stickerList.addAll(getStickerFromAssets(folder))
    }
    private fun getStickerFromAssets(folder: String): List<String> {
        val stickers = ArrayList<String>()
        try {
            val files = assets.list(folder) ?: return emptyList()
            for (file in files) {
                stickers.add("$folder/$file")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return stickers
    }

    private fun initRcv(){
        binding.apply {
            rcv.adapter = stickerAdapter
            rcv.itemAnimator = null
            stickerAdapter.submitList(stickerList)
            Log.d("Sticker", "${stickerList.size}")
        }
    }

}