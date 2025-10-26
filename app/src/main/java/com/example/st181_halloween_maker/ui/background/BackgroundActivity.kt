package com.example.st181_halloween_maker.ui.background

import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseActivity
import android.content.Intent
import com.example.st181_halloween_maker.core.extensions.handleBack
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.extensions.showToast
import com.example.st181_halloween_maker.core.extensions.startIntent
import com.example.st181_halloween_maker.core.utils.SaveState
import com.example.st181_halloween_maker.core.utils.key.IntentKey
import com.example.st181_halloween_maker.core.utils.key.ValueKey
import com.example.st181_halloween_maker.databinding.ActivityBackgroundBinding
import com.example.st181_halloween_maker.ui.success.SuccessActivity
import com.girlmaker.create.avatar.creator.model.BackGroundModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.st181_halloween_maker.core.helper.BitmapHelper
import com.example.st181_halloween_maker.core.helper.MediaHelper

class BackgroundActivity : BaseActivity<ActivityBackgroundBinding>() {
    private val backgroundAdapter by lazy { BackgroundAdapter(this) }
    private val viewModel: BackgroundViewModel by viewModels()
    private var selectedBackgroundPath: String? = null
    private var previousImagePath: String? = null

    override fun setViewBinding(): ActivityBackgroundBinding {
        return ActivityBackgroundBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // Load ảnh từ Intent
        previousImagePath = intent.getStringExtra(IntentKey.INTENT_KEY)
        val categoryPosition = intent.getIntExtra(IntentKey.CATEGORY_POSITION_KEY, 0)
        if (!previousImagePath.isNullOrEmpty()) {
            Glide.with(this).load(previousImagePath).into(binding.ivPreviousImage)
        }

        // Set background based on category position
        val backgroundDrawable = when(categoryPosition) {
            0 -> R.drawable.bg_data1
            1 -> R.drawable.bg_data2
            2 -> R.drawable.bg_data3
            else -> R.drawable.img_bg_app
        }
        binding.main.setBackgroundResource(backgroundDrawable)

        // Load danh sách background
        val list = viewModel.getListBackground(this)
        backgroundAdapter.submitList(list)
        binding.rcvLayer.adapter = backgroundAdapter
        backgroundAdapter.onItemClick = { item, position ->
            if (viewModel.isDataAPI()) {
                viewModel.checkDataInternet(this) {
                    handleSelectBackground(item, position)
                }
            } else {
                handleSelectBackground(item, position)
            }
        }

    }

    override fun viewListener() {
        binding.apply {
            btnBack.onSingleClick { handleBack() }
            btnSave.onSingleClick { handleSave() }
        }
        handleRcv()
    }

    private fun handleRcv() {
        backgroundAdapter.onItemClick = { item, position ->
            viewModel.checkDataInternet(this) {
                handleSelectBackground(item, position)
            }
        }

        backgroundAdapter.onNoneClick = { position ->
            viewModel.checkDataInternet(this) {
                handleRemoveBackground(position)
            }
        }
    }

    private fun handleSelectBackground(item: BackGroundModel, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val path = item.path
            selectedBackgroundPath = path

            // Update selection state
            val currentList = backgroundAdapter.getCurrentList()
            currentList.forEach { it.isSelected = false }  // Deselect all
            item.isSelected = true  // Select current item

            withContext(Dispatchers.Main) {
                Glide.with(this@BackgroundActivity)
                    .load(path)
                    .into(binding.ivBackground)
                backgroundAdapter.notifyDataSetChanged()  // Refresh adapter to show border
            }
        }
    }

    private fun handleRemoveBackground(position: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            selectedBackgroundPath = null

            // Update selection state - mark "None" item as selected
            val currentList = backgroundAdapter.getCurrentList()
            currentList.forEach { it.isSelected = false }  // Deselect all
            if (position < currentList.size) {
                currentList[position].isSelected = true  // Select the "None" item
            }

            Glide.with(this@BackgroundActivity).clear(binding.ivBackground)
            backgroundAdapter.notifyDataSetChanged()  // Refresh adapter to show border on None
        }
    }

    private fun handleSave() {
        // Save to My Creation and navigate to SuccessActivity
        if (!selectedBackgroundPath.isNullOrEmpty()) {
            lifecycleScope.launch {
                val bitmap = BitmapHelper.createBimapFromView(binding.layoutCustomLayer)
                MediaHelper.saveBitmapToInternalStorage(this@BackgroundActivity, ValueKey.DOWNLOAD_ALBUM, bitmap)
                    .collect { result ->
                        when (result) {
                            is SaveState.Loading -> showLoading()
                            is SaveState.Error -> {
                                dismissLoading(true)
                                showToast(R.string.save_failed_please_try_again)
                            }
                            is SaveState.Success -> {
                                dismissLoading(true)
                                // Navigate to SuccessActivity after saving
                                val intent = Intent(this@BackgroundActivity, SuccessActivity::class.java).apply {
                                    putExtra(IntentKey.BACKGROUND_IMAGE_KEY, selectedBackgroundPath)
                                    putExtra(IntentKey.PREVIOUS_IMAGE_KEY, previousImagePath)
                                }
                                startActivity(intent)
                                finish()
                            }
                        }
                    }
            }
        } else {
            showToast(R.string.please_select_an_image)
        }
    }

    override fun initText() {}
}
