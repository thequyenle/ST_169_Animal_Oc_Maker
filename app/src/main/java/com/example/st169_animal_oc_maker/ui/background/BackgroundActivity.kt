package com.example.st169_animal_oc_maker.ui.background

import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.st169_animal_oc_maker.R
import com.example.st169_animal_oc_maker.core.base.BaseActivity
import android.content.Intent
import com.example.st169_animal_oc_maker.core.extensions.handleBack
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.core.extensions.showToast
import com.example.st169_animal_oc_maker.core.utils.SaveState
import com.example.st169_animal_oc_maker.core.utils.key.IntentKey
import com.example.st169_animal_oc_maker.core.utils.key.ValueKey
import com.example.st169_animal_oc_maker.databinding.ActivityBackgroundBinding
import com.example.st169_animal_oc_maker.ui.success.SuccessActivity
import com.girlmaker.create.avatar.creator.model.BackGroundModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.st169_animal_oc_maker.core.helper.BitmapHelper
import com.example.st169_animal_oc_maker.core.helper.MediaHelper

class BackgroundActivity : BaseActivity<ActivityBackgroundBinding>() {
    private val backgroundAdapter by lazy { BackgroundAdapter(this) }
    private val viewModel: BackgroundViewModel by viewModels()
    private var selectedBackgroundPath: String? = null
    private var previousImagePath: String? = null
    private var categoryBackgroundRes: Int = R.drawable.img_bg_app
    private var isNoneSelected = false

    override fun setViewBinding(): ActivityBackgroundBinding {
        return ActivityBackgroundBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // Load ảnh từ Intent
        previousImagePath = intent.getStringExtra(IntentKey.INTENT_KEY)
        val categoryPosition = intent.getIntExtra(IntentKey.CATEGORY_POSITION_KEY, 0)
        val suggestionBackground = intent.getStringExtra(IntentKey.SUGGESTION_BACKGROUND)

        if (!previousImagePath.isNullOrEmpty()) {
            Glide.with(this).load(previousImagePath).into(binding.ivPreviousImage)
        }

        // Set background based on category position
        categoryBackgroundRes = when(categoryPosition) {
            0 -> R.drawable.bg_data1
            1 -> R.drawable.bg_data2
            2 -> R.drawable.bg_data3
            else -> R.drawable.img_bg_app
        }
        binding.main.setBackgroundResource(categoryBackgroundRes)

        // Load danh sách background
        val list = viewModel.getListBackground(this)
        backgroundAdapter.submitList(list)
        binding.rcvLayer.adapter = backgroundAdapter

        // Pre-select suggestion background if exists, otherwise focus on btnNone
        if (!suggestionBackground.isNullOrEmpty()) {
            preSelectBackground(list, suggestionBackground)
        } else {
            // Focus mặc định vào btnNone (position 0)
            focusOnNone(list)
        }

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
            isNoneSelected = false  // Reset None selection flag

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
            isNoneSelected = true  // Mark None as selected

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

    /**
     * Focus on None item by default
     */
    private fun focusOnNone(list: List<BackGroundModel>) {
        lifecycleScope.launch(Dispatchers.Main) {
            // Select the first item (None item)
            list.forEach { it.isSelected = false }  // Deselect all
            if (list.isNotEmpty()) {
                list[0].isSelected = true  // Select None item
            }

            isNoneSelected = true
            selectedBackgroundPath = null

            // Clear ivBackground to show only category background
            Glide.with(this@BackgroundActivity).clear(binding.ivBackground)

            // Refresh adapter to show border on None
            backgroundAdapter.notifyDataSetChanged()
        }
    }

    /**
     * Pre-select background from suggestion
     */
    private fun preSelectBackground(list: List<BackGroundModel>, backgroundPath: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Find matching background in list
            val matchingIndex = list.indexOfFirst { it.path == backgroundPath }

            if (matchingIndex >= 0) {
                val matchingItem = list[matchingIndex]
                selectedBackgroundPath = backgroundPath

                // Update selection state
                list.forEach { it.isSelected = false }  // Deselect all
                matchingItem.isSelected = true  // Select matching item

                withContext(Dispatchers.Main) {
                    // Load background image
                    Glide.with(this@BackgroundActivity)
                        .load(backgroundPath)
                        .into(binding.ivBackground)

                    // Refresh adapter to show border
                    backgroundAdapter.notifyDataSetChanged()

                    // Scroll to selected item
                    binding.rcvLayer.scrollToPosition(matchingIndex)
                }
            }
        }
    }

    private fun handleSave() {
        // Save to My Creation and navigate to SuccessActivity
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
                                if (isNoneSelected) {
                                    // When None is selected, pass category background resource ID
                                    putExtra(IntentKey.CATEGORY_BACKGROUND_RES, categoryBackgroundRes)
                                } else {
                                    // Normal flow: pass selected background path
                                    putExtra(IntentKey.BACKGROUND_IMAGE_KEY, selectedBackgroundPath)
                                }
                                putExtra(IntentKey.PREVIOUS_IMAGE_KEY, previousImagePath)
                            }
                            startActivity(intent)
                            finish()
                        }
                    }
                }
        }
    }

    override fun initText() {}
}
