package com.example.st169_animal_oc_maker.ui.success

import android.view.LayoutInflater
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.st169_animal_oc_maker.R
import com.example.st169_animal_oc_maker.core.base.BaseActivity
import com.example.st169_animal_oc_maker.core.extensions.handleBack
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.core.extensions.showToast
import com.example.st169_animal_oc_maker.core.extensions.startIntent
import com.example.st169_animal_oc_maker.core.helper.BitmapHelper
import com.example.st169_animal_oc_maker.core.helper.MediaHelper
import com.example.st169_animal_oc_maker.core.utils.SaveState
import com.example.st169_animal_oc_maker.core.utils.key.IntentKey
import com.example.st169_animal_oc_maker.core.utils.key.ValueKey
import com.example.st169_animal_oc_maker.databinding.ActivitySuccessBinding
import com.example.st169_animal_oc_maker.ui.home.HomeActivity
import com.example.st169_animal_oc_maker.ui.mycreation.MycreationActivity
import kotlinx.coroutines.launch

class SuccessActivity : BaseActivity<ActivitySuccessBinding>() {
    override fun setViewBinding(): ActivitySuccessBinding {
        return ActivitySuccessBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // Get both image paths from intent
        val backgroundPath = intent.getStringExtra(IntentKey.BACKGROUND_IMAGE_KEY)
        val previousImagePath = intent.getStringExtra(IntentKey.PREVIOUS_IMAGE_KEY)

        // Display the image (activity_success.xml only has imvImage, no ivBackground)
        if (!previousImagePath.isNullOrEmpty()) {
            Glide.with(this)
                .load(previousImagePath)
                .into(binding.imvImage)
        }
    }

    override fun viewListener() {
        // Add click listener for btnBack to go back to previous screen
        binding.btnBack.onSingleClick {
            handleBack()
        }

        // Add click listener for ic_delete to return to HomeActivity
        binding.icHome.onSingleClick {
            startIntent(HomeActivity::class.java)
            finish()
        }

        // Add click listener for btnDownload to navigate to MycreationActivity
        binding.btnMyAlbum.onSingleClick {
            startIntent(MycreationActivity::class.java)
            finish()
        }

        // Add click listener for btnShare (if needed)
        binding.btnShare.onSingleClick {
            saveImageToAlbum()
        }
    }

    private fun saveImageToAlbum() {
        lifecycleScope.launch {
            val bitmap = BitmapHelper.createBimapFromView(binding.layoutCustomLayer)
            MediaHelper.saveBitmapToInternalStorage(this@SuccessActivity, ValueKey.DOWNLOAD_ALBUM, bitmap)
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
    }

    override fun initText() {

    }

}