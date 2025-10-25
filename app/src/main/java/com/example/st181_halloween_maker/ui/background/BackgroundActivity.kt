package com.example.st181_halloween_maker.ui.background

import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseActivity
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.st181_halloween_maker.core.extensions.handleBack
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.extensions.showToast
import com.example.st181_halloween_maker.core.extensions.startIntent
import com.example.st181_halloween_maker.core.helper.BitmapHelper
import com.example.st181_halloween_maker.core.helper.MediaHelper
import com.example.st181_halloween_maker.core.helper.MediaHelper.saveBitmapToCache
import com.example.st181_halloween_maker.core.utils.SaveState
import com.example.st181_halloween_maker.core.utils.key.IntentKey
import com.example.st181_halloween_maker.core.utils.key.ValueKey
import com.example.st181_halloween_maker.databinding.ActivityBackgroundBinding
import com.example.st181_halloween_maker.ui.mycreation.MycreationActivity
import com.example.st181_halloween_maker.ui.success.SuccessActivity
import com.example.st181_halloween_maker.ui.view.ViewActivity
import com.girlmaker.create.avatar.creator.model.BackGroundModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        if (!previousImagePath.isNullOrEmpty()) {
            Glide.with(this).load(previousImagePath).into(binding.ivPreviousImage)
        }

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
            btnMyAlbum.onSingleClick { navigateToMyAlbum() }
            btnShare.onSingleClick { shareImage() }
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
            withContext(Dispatchers.Main) {
                Glide.with(this@BackgroundActivity)
                    .load(path)
                    .into(binding.ivBackground)
            }
        }
    }

    private fun handleRemoveBackground(position: Int) {
        lifecycleScope.launch(Dispatchers.Main) {
            selectedBackgroundPath = null
            Glide.with(this@BackgroundActivity).clear(binding.ivBackground)
        }
    }

    private fun handleSave() {
        // Save to MyCreation and keep the same navigation
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
                                showToast(R.string.image_has_been_saved_successfully)
                            }
                        }
                    }
            }
        } else {
            showToast(R.string.please_select_an_image)
        }
    }

    private fun navigateToMyAlbum() {
        startIntent(MycreationActivity::class.java)
    }

    private fun shareImage() {
        if (!selectedBackgroundPath.isNullOrEmpty()) {
            lifecycleScope.launch {
                try {
                    val bitmap = BitmapHelper.createBimapFromView(binding.layoutCustomLayer)
                    val file = saveBitmapToCache(bitmap)

                    val uri = FileProvider.getUriForFile(
                        this@BackgroundActivity,
                        "${packageName}.provider",
                        file
                    )

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
                } catch (e: Exception) {
                    showToast(R.string.share_failed)
                }
            }
        } else {
            showToast(R.string.please_select_an_image)
        }
    }

    override fun initText() {}
}
