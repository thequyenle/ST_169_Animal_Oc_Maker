package com.animal.avatar.charactor.maker.ui.background

import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.animal.avatar.charactor.maker.R
import com.animal.avatar.charactor.maker.core.base.BaseActivity
import android.content.Intent
import android.graphics.Color
import com.animal.avatar.charactor.maker.core.extensions.handleBack
import com.animal.avatar.charactor.maker.core.extensions.onSingleClick
import com.animal.avatar.charactor.maker.core.extensions.showToast
import com.animal.avatar.charactor.maker.core.utils.SaveState
import com.animal.avatar.charactor.maker.core.utils.key.IntentKey
import com.animal.avatar.charactor.maker.core.utils.key.ValueKey
import com.animal.avatar.charactor.maker.databinding.ActivityBackgroundBinding
import com.animal.avatar.charactor.maker.ui.success.SuccessActivity
import com.girlmaker.create.avatar.creator.model.BackGroundModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.animal.avatar.charactor.maker.core.helper.BitmapHelper
import com.animal.avatar.charactor.maker.core.helper.InternetHelper
import com.animal.avatar.charactor.maker.core.helper.MediaHelper

class BackgroundActivity : BaseActivity<ActivityBackgroundBinding>() {
    private val backgroundAdapter by lazy { BackgroundAdapter(this) }
    private val viewModel: BackgroundViewModel by viewModels()
    private var selectedBackgroundPath: String? = null
    private var previousImagePath: String? = null
    private var categoryBackgroundRes: Int = R.drawable.img_bg_app
    private var categoryPosition: Int = 0
    private var categoryBackgroundColor: String? = null
    private var isNoneSelected = false

    override fun setViewBinding(): ActivityBackgroundBinding {
        return ActivityBackgroundBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // Load ảnh từ Intent
        previousImagePath = intent.getStringExtra(IntentKey.INTENT_KEY)
        categoryPosition = intent.getIntExtra(IntentKey.CATEGORY_POSITION_KEY, 0)
        val suggestionBackground = intent.getStringExtra(IntentKey.SUGGESTION_BACKGROUND)

        if (!previousImagePath.isNullOrEmpty()) {
            // Display layout
            Glide.with(this).load(previousImagePath).into(binding.ivPreviousImage)

            // ✅ My Creation layout
            Glide.with(this).load(previousImagePath).into(binding.ivPreviousImageMyCreation)

            // ✅ Download layout
            Glide.with(this).load(previousImagePath).into(binding.ivPreviousImageDownload)
        }

        // Set background based on category position
        categoryBackgroundRes = when(categoryPosition) {
            0 -> R.drawable.bg_data1
            1 -> R.drawable.bg_data2
            2 -> R.drawable.bg_data3
            else -> R.drawable.img_bg_app
        }
        binding.main.setBackgroundResource(categoryBackgroundRes)

        // Set background color for ivBackground based on category position
        categoryBackgroundColor = when(categoryPosition) {
            0 -> "#00FFFFFF"
            1 -> "#00FFFFFF"
            2 -> "#00FFFFFF"
            else -> "#00FFFFFF"
        }
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
            if (viewModel.isDataAPI()) {
                // Check internet when data is from API
                if (InternetHelper.checkInternet(this)) {
                    handleSelectBackground(item, position)
                } else {
                    showNoInternetDialog()
                }
            } else {
                handleSelectBackground(item, position)
            }
        }

        backgroundAdapter.onNoneClick = { position ->
            if (viewModel.isDataAPI()) {
                // Check internet when data is from API
                if (InternetHelper.checkInternet(this)) {
                    handleRemoveBackground(position)
                } else {
                    showNoInternetDialog()
                }
            } else {
                handleRemoveBackground(position)
            }
        }
    }

    private fun handleSelectBackground(item: BackGroundModel, position: Int) {
        // Check if item is from online URL and internet is available
        if (item.path.startsWith("https://") || item.path.startsWith("http://")) {
            if (!InternetHelper.checkInternet(this)) {
                showNoInternetDialog()
                return
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val path = item.path
            selectedBackgroundPath = path
            isNoneSelected = false  // Reset None selection flag

            // Update selection state
            val currentList = backgroundAdapter.getCurrentList()
            currentList.forEach { it.isSelected = false }  // Deselect all
            item.isSelected = true  // Select current item

            withContext(Dispatchers.Main) {
                // ✅ FIX: Clear background drawable AND set transparent color
                binding.ivBackground.setBackgroundColor(Color.TRANSPARENT)
                binding.ivBackground.background = null  // Clear any drawable

                // Load background image - Display layout
                Glide.with(this@BackgroundActivity)
                    .load(path)
                    .into(binding.ivBackground)

                // ✅ Load into My Creation layout
                binding.layoutMyCreationCapture.setBackgroundColor(Color.TRANSPARENT)
                binding.ivBackgroundMyCreation.setBackgroundColor(Color.TRANSPARENT)
                Glide.with(this@BackgroundActivity)
                    .load(path)
                    .into(binding.ivBackgroundMyCreation)

                // ✅ Load into Download layout
                binding.layoutDownloadCapture.setBackgroundColor(Color.TRANSPARENT)
                binding.ivBackgroundDownload.setBackgroundColor(Color.TRANSPARENT)
                Glide.with(this@BackgroundActivity)
                    .load(path)
                    .into(binding.ivBackgroundDownload)

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

            // ✅ FIX: Clear image from Glide first, then set background color
            Glide.with(this@BackgroundActivity).clear(binding.ivBackground)
            binding.ivBackground.setImageDrawable(null)  // Clear any image

            // Display: Set background color based on category position
            categoryBackgroundColor?.let { colorHex ->
                binding.ivBackground.setBackgroundColor(Color.parseColor(colorHex))
            }

            // ✅ My Creation layout: WHITE background (#FFFFFF)
            // Clear the ImageView and set WHITE background on the parent FrameLayout
            Glide.with(this@BackgroundActivity).clear(binding.ivBackgroundMyCreation)
            binding.ivBackgroundMyCreation.setImageDrawable(null)
            binding.ivBackgroundMyCreation.setBackgroundColor(Color.TRANSPARENT)
            binding.layoutMyCreationCapture.setBackgroundColor(Color.WHITE)

            // ✅ Download layout: TRANSPARENT background
            Glide.with(this@BackgroundActivity).clear(binding.ivBackgroundDownload)
            binding.ivBackgroundDownload.setImageDrawable(null)
            binding.ivBackgroundDownload.setBackgroundColor(Color.TRANSPARENT)
            binding.layoutDownloadCapture.setBackgroundColor(Color.TRANSPARENT)

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

            // ✅ Clear image from Glide first
            Glide.with(this@BackgroundActivity).clear(binding.ivBackground)
            binding.ivBackground.setImageDrawable(null)

            // Display: Set background color based on category position
            categoryBackgroundColor?.let { colorHex ->
                binding.ivBackground.setBackgroundColor(Color.parseColor(colorHex))
            }

            // ✅ My Creation layout: WHITE background
            Glide.with(this@BackgroundActivity).clear(binding.ivBackgroundMyCreation)
            binding.ivBackgroundMyCreation.setImageDrawable(null)
            binding.ivBackgroundMyCreation.setBackgroundColor(Color.TRANSPARENT)
            binding.layoutMyCreationCapture.setBackgroundColor(Color.WHITE)

            // ✅ Download layout: TRANSPARENT background
            Glide.with(this@BackgroundActivity).clear(binding.ivBackgroundDownload)
            binding.ivBackgroundDownload.setImageDrawable(null)
            binding.ivBackgroundDownload.setBackgroundColor(Color.TRANSPARENT)
            binding.layoutDownloadCapture.setBackgroundColor(Color.TRANSPARENT)

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
                isNoneSelected = false  // Not None

                // Update selection state
                list.forEach { it.isSelected = false }  // Deselect all
                matchingItem.isSelected = true  // Select matching item

                withContext(Dispatchers.Main) {
                    // Clear background color and set transparent
                    binding.ivBackground.setBackgroundColor(Color.TRANSPARENT)
                    binding.ivBackground.background = null

                    // Load background image - Display layout
                    Glide.with(this@BackgroundActivity)
                        .load(backgroundPath)
                        .into(binding.ivBackground)

                    // ✅ My Creation layout
                    binding.layoutMyCreationCapture.setBackgroundColor(Color.TRANSPARENT)
                    binding.ivBackgroundMyCreation.setBackgroundColor(Color.TRANSPARENT)
                    Glide.with(this@BackgroundActivity)
                        .load(backgroundPath)
                        .into(binding.ivBackgroundMyCreation)

                    // ✅ Download layout
                    binding.layoutDownloadCapture.setBackgroundColor(Color.TRANSPARENT)
                    binding.ivBackgroundDownload.setBackgroundColor(Color.TRANSPARENT)
                    Glide.with(this@BackgroundActivity)
                        .load(backgroundPath)
                        .into(binding.ivBackgroundDownload)

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
            // ✅ Capture from layoutDownloadCapture (transparent background for None - for download/share)
            val bitmap = BitmapHelper.createBimapFromView(binding.layoutDownloadCapture)
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
                                    // ✅ When None is selected, pass background color
                                    putExtra(IntentKey.IS_NONE_SELECTED, true)
                                    putExtra(IntentKey.BACKGROUND_COLOR_KEY, categoryBackgroundColor)
                                } else {
                                    // Normal flow: pass selected background path
                                    putExtra(IntentKey.IS_NONE_SELECTED, false)
                                    putExtra(IntentKey.BACKGROUND_IMAGE_KEY, selectedBackgroundPath)
                                }
                                putExtra(IntentKey.PREVIOUS_IMAGE_KEY, previousImagePath)
                                putExtra(IntentKey.CATEGORY_POSITION_KEY, categoryPosition)
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