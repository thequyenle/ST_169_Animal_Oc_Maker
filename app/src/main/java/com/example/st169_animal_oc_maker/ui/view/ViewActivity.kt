package com.example.st169_animal_oc_maker.ui.view

import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.st169_animal_oc_maker.R
import com.example.st169_animal_oc_maker.core.base.BaseActivity
import com.example.st169_animal_oc_maker.core.dialog.ConfirmDialog
import com.example.st169_animal_oc_maker.core.extensions.handleBack
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.core.extensions.showToast
import com.example.st169_animal_oc_maker.core.helper.BitmapHelper
import com.example.st169_animal_oc_maker.core.helper.MediaHelper
import com.example.st169_animal_oc_maker.core.utils.HandleState
import com.example.st169_animal_oc_maker.core.utils.key.IntentKey
import com.example.st169_animal_oc_maker.databinding.ActivityViewBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ViewActivity : BaseActivity<ActivityViewBinding>() {
    private var imagePath: String? = null

    override fun setViewBinding(): ActivityViewBinding {
        return ActivityViewBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // Get image path from intent
        imagePath = intent.getStringExtra(IntentKey.IMAGE_PATH_KEY)

        // Display the image
        if (!imagePath.isNullOrEmpty()) {
            Glide.with(this)
                .load(imagePath)
                .into(binding.imvImage)
        }
    }

    override fun viewListener() {
        // Add click listener for btnBack to go back to previous screen
        binding.btnBack.onSingleClick {
            handleBack()
        }

        // Add click listener for ic_home to navigate to home
        binding.icDelete.onSingleClick {
            handleBack()
        }

        // Add click listener for btnMyAlbum (was download)
        binding.btnDownload.onSingleClick {
            downloadImage()
        }

        // Add click listener for btnShare to share the image
        binding.btnShare.onSingleClick {
            shareImage()
        }
    }

    private fun showDeleteDialog() {
        val dialog = ConfirmDialog(
            this,
            R.string.delete,
            R.string.do_you_want_to_delete
        )
        dialog.onYesClick = {
            deleteImage()
            dialog.dismiss()
        }
        dialog.onNoClick = {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteImage() {
        if (!imagePath.isNullOrEmpty()) {
            lifecycleScope.launch {
                try {
                    val file = File(imagePath!!)
                    if (file.exists()) {
                        file.delete()
                        showToast(R.string.image_deleted_successfully)
                        finish() // Go back to previous screen after deleting
                    } else {
                        showToast(R.string.file_not_found)
                    }
                } catch (e: Exception) {
                    showToast(R.string.delete_failed)
                }
            }
        }
    }

    /**
     * ✅ OPTIMIZED: Download image without border/background
     * Same optimization as shareImage() - capture only the clean image
     */
    private fun downloadImage() {
        if (!imagePath.isNullOrEmpty()) {
            lifecycleScope.launch {
                var bitmap: Bitmap? = null
                try {
                    // ✅ Capture only the ImageView (clean, no gradient border)
                    bitmap = withContext(Dispatchers.Default) {
                        BitmapHelper.createBitmapFromImageView(binding.imvImage)
                    }

                    MediaHelper.saveBitmapToExternal(this@ViewActivity, bitmap)
                        .collect { state ->
                            when (state) {
                                HandleState.LOADING -> showLoading()
                                HandleState.SUCCESS -> {
                                    dismissLoading(true)
                                    showToast(R.string.image_has_been_saved_successfully)
                                }
                                HandleState.FAIL -> {
                                    dismissLoading(true)
                                    showToast(R.string.save_failed_please_try_again)
                                }
                                HandleState.NOT_SELECT -> {
                                    // Do nothing
                                }
                            }
                        }
                } catch (e: Exception) {
                    dismissLoading(true)
                    showToast(R.string.save_failed_please_try_again)
                } finally {
                    // ✅ Clean up: Recycle bitmap to free memory
                    bitmap?.recycle()
                }
            }
        }
    }

    /**
     * ✅ OPTIMIZED: Share image without border/background
     * Strategy:
     * 1. Capture ONLY the ImageView (imvImage) - no gradient border
     * 2. Use Dispatchers.IO for file operations
     * 3. Recycle bitmap after saving to prevent memory leak
     */
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
                        this@ViewActivity.saveBitmapToCache(bitmap)
                    }
                }

                dismissLoading(true)

                // Create content URI using FileProvider for Android 7.0+
                val contentUri = FileProvider.getUriForFile(
                    this@ViewActivity,
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