package com.example.st169_animal_oc_maker.ui.success

import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import androidx.core.content.FileProvider
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
import com.example.st169_animal_oc_maker.ui.background.BackgroundActivity
import com.example.st169_animal_oc_maker.ui.home.HomeActivity
import com.example.st169_animal_oc_maker.ui.mycreation.MycreationActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SuccessActivity : BaseActivity<ActivitySuccessBinding>() {
    private var backgroundPath: String? = null
    private var previousImagePath: String? = null

    override fun setViewBinding(): ActivitySuccessBinding {
        return ActivitySuccessBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // Get both image paths from intent
        backgroundPath = intent.getStringExtra(IntentKey.BACKGROUND_IMAGE_KEY)
        previousImagePath = intent.getStringExtra(IntentKey.PREVIOUS_IMAGE_KEY)

        // Display background if available
        if (!backgroundPath.isNullOrEmpty()) {
            Glide.with(this)
                .load(backgroundPath)
                .into(binding.ivBackground)
        }

        // Display the character image
        if (!previousImagePath.isNullOrEmpty()) {
            Glide.with(this)
                .load(previousImagePath)
                .into(binding.imvImage)
        }
    }

    override fun viewListener() {
        // Add click listener for btnBack to go back to previous screen
        binding.btnBack.onSingleClick {
            // Quay lại BackgroundActivity với trạng thái đã chọn
            val intent = Intent(this, BackgroundActivity::class.java).apply {
                // Truyền lại previousImagePath (ảnh nhân vật)
                putExtra(IntentKey.INTENT_KEY, previousImagePath)

                // Truyền background đã chọn để pre-select
                putExtra(IntentKey.SUGGESTION_BACKGROUND, backgroundPath)
            }
            startActivity(intent)
            finish()
        }

        // Add click listener for ic_delete to return to HomeActivity
        binding.icHome.onSingleClick {
            startIntent(HomeActivity::class.java)
            finish()
        }

        // Add click listener for btnDownload to navigate to MycreationActivity
        binding.btnMyAlbum.onSingleClick {
            val intent = Intent(this, MycreationActivity::class.java).apply {
                putExtra(IntentKey.FROM_SUCCESS, true)
            }
            startActivity(intent)
            finish()
        }

        // Add click listener for btnShare (if needed)
        binding.btnShare.onSingleClick {
            shareImage()
        }
    }

    private fun shareImage() {
        lifecycleScope.launch {
            var bitmap: Bitmap? = null
            try {
                showLoading()

                // ✅ SOLUTION 1: Capture directly from ImageView (clean, no border)
                bitmap = withContext(Dispatchers.Default) {
                    BitmapHelper.createBitmapFromImageView(binding.imvImage)
                }

                // ✅ Alternative: If you need the entire layout but without border, uncomment this:
                // bitmap = withContext(Dispatchers.Default) {
                //     val fullBitmap = BitmapHelper.createBimapFromView(binding.layoutCustomLayer)
                //     BitmapHelper.cropTransparentEdges(fullBitmap).also {
                //         if (it != fullBitmap) fullBitmap.recycle() // Recycle original if cropped
                //     }
                // }

                // Save bitmap to cache (on IO thread for better performance)
                val file = withContext(Dispatchers.IO) {
                    with(MediaHelper) {
                            this@SuccessActivity.saveBitmapToCache(bitmap)
                    }
                }

                dismissLoading(true)

                // Create content URI using FileProvider for Android 7.0+
                val contentUri = FileProvider.getUriForFile(
                    this@SuccessActivity,
                    "${applicationContext.packageName}.provider",
                    file
                )

                // Create share intent
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    type = "image/png"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // Show share chooser
                val chooser = Intent.createChooser(shareIntent, getString(R.string.share))
                startActivity(chooser)

            } catch (e: Exception) {
                dismissLoading(true)
                showToast("Share failed: ${e.message}")
            } finally {
                // ✅ Clean up: Recycle bitmap to free memory
                bitmap?.recycle()
            }
        }
    }


    override fun initText() {

    }

}