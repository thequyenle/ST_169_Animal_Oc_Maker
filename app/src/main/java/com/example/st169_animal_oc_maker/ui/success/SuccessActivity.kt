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
import com.example.st169_animal_oc_maker.databinding.ActivityViewBinding
import com.example.st169_animal_oc_maker.ui.home.HomeActivity
import com.example.st169_animal_oc_maker.ui.mycreation.MycreationActivity
import kotlinx.coroutines.launch

class SuccessActivity : BaseActivity<ActivityViewBinding>() {
    override fun setViewBinding(): ActivityViewBinding {
        return ActivityViewBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // Get both image paths from intent
        val backgroundPath = intent.getStringExtra(IntentKey.BACKGROUND_IMAGE_KEY)
        val previousImagePath = intent.getStringExtra(IntentKey.PREVIOUS_IMAGE_KEY)

        // Display the background image
        if (!backgroundPath.isNullOrEmpty()) {
            Glide.with(this)
                .load(backgroundPath)
                .into(binding.ivBackground)
        }

        // Display the previous image on top
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

        // Add click listener for ic_home to return to HomeActivity
        binding.icHome.onSingleClick {
            startIntent(HomeActivity::class.java)
            finish()
        }

        // Add click listener for btnMyAlbum to navigate to MycreationActivity
        binding.btnMyAlbum.onSingleClick {
            startIntent(MycreationActivity::class.java)
            finish()
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