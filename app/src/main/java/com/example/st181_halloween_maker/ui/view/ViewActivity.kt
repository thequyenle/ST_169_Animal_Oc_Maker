package com.example.st181_halloween_maker.ui.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseActivity
import com.example.st181_halloween_maker.core.dialog.ConfirmDialog
import com.example.st181_halloween_maker.core.extensions.handleBack
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.extensions.showToast
import com.example.st181_halloween_maker.core.extensions.startIntent
import com.example.st181_halloween_maker.core.helper.BitmapHelper
import com.example.st181_halloween_maker.core.helper.MediaHelper
import com.example.st181_halloween_maker.core.utils.HandleState
import com.example.st181_halloween_maker.core.utils.key.IntentKey
import com.example.st181_halloween_maker.databinding.ActivitySuccessBinding
import com.example.st181_halloween_maker.ui.home.HomeActivity
import com.example.st181_halloween_maker.ui.mycreation.MycreationActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ViewActivity : BaseActivity<ActivitySuccessBinding>() {
    private var imagePath: String? = null

    override fun setViewBinding(): ActivitySuccessBinding {
        return ActivitySuccessBinding.inflate(LayoutInflater.from(this))
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

        // Add click listener for ic_delete to delete the image
        binding.icDelete.onSingleClick {
            showDeleteDialog()
        }

        // Add click listener for btnDownload to download the image
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

    private fun downloadImage() {
        if (!imagePath.isNullOrEmpty()) {
            lifecycleScope.launch {
                try {
                    val bitmap = BitmapHelper.createBimapFromView(binding.layoutCustomLayer)
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
                }
            }
        }
    }

    private fun shareImage() {
        lifecycleScope.launch {
            try {
                showLoading()

                // Create bitmap from view
                val bitmap = withContext(Dispatchers.Default) {
                    BitmapHelper.createBimapFromView(binding.layoutCustomLayer)
                }

                // Save bitmap to cache
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
            }
        }
    }

    override fun initText() {

    }

}