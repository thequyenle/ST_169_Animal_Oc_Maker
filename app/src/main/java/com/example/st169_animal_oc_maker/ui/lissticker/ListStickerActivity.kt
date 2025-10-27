package com.example.st169_animal_oc_maker.ui.lissticker

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import com.example.st169_animal_oc_maker.core.base.BaseActivity
import com.example.st169_animal_oc_maker.core.extensions.handleBack
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.core.utils.DataLocal.getAvatarStickerAsset
import com.example.st169_animal_oc_maker.core.utils.KeyApp.HALLOWEEN_KEY
import com.example.st169_animal_oc_maker.databinding.ActivityListStickerBinding
import com.example.st169_animal_oc_maker.ui.itemsticker.ItemStickerActivity

class ListStickerActivity : BaseActivity<ActivityListStickerBinding>() {

    private val avatarStickerList = ArrayList<String>()

    private val stickerAdapter by lazy {
        ListStickerAdapter(this)
    }
    override fun setViewBinding(): ActivityListStickerBinding {
        return ActivityListStickerBinding.inflate(LayoutInflater.from(this))
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
        handleRcv()
    }

    override fun initText() {

    }
    private fun initData() {
        binding.apply {
            avatarStickerList.clear()
            avatarStickerList.addAll(getAvatarStickerAsset(this@ListStickerActivity))
        }
    }
    private fun initRcv() {
        binding.apply {
            rcv.adapter = stickerAdapter
            rcv.itemAnimator = null
            stickerAdapter.submitList(avatarStickerList)
            Log.d("Sticker", "${avatarStickerList.size}")
        }
    }
    private fun handleRcv(){
        binding.apply {
            stickerAdapter.onItemClick = { path,position ->
                val stickerFolder = "sticker/${position + 1}"
                val intent = Intent(this@ListStickerActivity, ItemStickerActivity::class.java)
                intent.putExtra(HALLOWEEN_KEY,stickerFolder)
                startActivity(intent)
            }
        }
    }

}