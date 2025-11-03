package com.animal.avatar.charactor.maker.ui.success

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.view.LayoutInflater
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.animal.avatar.charactor.maker.R
import com.animal.avatar.charactor.maker.core.base.BaseActivity
import com.animal.avatar.charactor.maker.core.extensions.onSingleClick
import com.animal.avatar.charactor.maker.core.extensions.showToast
import com.animal.avatar.charactor.maker.core.extensions.startIntent
import com.animal.avatar.charactor.maker.core.helper.BitmapHelper
import com.animal.avatar.charactor.maker.core.utils.key.IntentKey
import com.animal.avatar.charactor.maker.databinding.ActivitySuccessBinding
import com.animal.avatar.charactor.maker.ui.background.BackgroundActivity
import com.animal.avatar.charactor.maker.ui.home.HomeActivity
import com.animal.avatar.charactor.maker.ui.mycreation.MycreationActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SuccessActivity : BaseActivity<ActivitySuccessBinding>() {
    // ✅ FIX: Lưu tất cả thông tin cần thiết
    private var backgroundPath: String? = null
    private var previousImagePath: String? = null
    private var categoryPosition: Int = 0
    private var isNoneSelected: Boolean = false
    private var backgroundColor: String? = null

    override fun setViewBinding(): ActivitySuccessBinding {
        return ActivitySuccessBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // ✅ FIX: Lưu tất cả data từ Intent
        previousImagePath = intent.getStringExtra(IntentKey.PREVIOUS_IMAGE_KEY)
        categoryPosition = intent.getIntExtra(IntentKey.CATEGORY_POSITION_KEY, 0)
        isNoneSelected = intent.getBooleanExtra(IntentKey.IS_NONE_SELECTED, false)

        // Load ảnh nhân vật vào cả 2 layouts
        if (!previousImagePath.isNullOrEmpty()) {
            Glide.with(this).load(previousImagePath).into(binding.imvImage)
            Glide.with(this).load(previousImagePath).into(binding.imvImageShare)
        }

        // Handle background
        if (isNoneSelected) {
            // ✅ Display: Show background color for None selection
            backgroundColor = intent.getStringExtra(IntentKey.BACKGROUND_COLOR_KEY)
            backgroundColor?.let {
                binding.ivBackground.setBackgroundColor(Color.parseColor(it))
            }

            // ✅ Share layout: TRANSPARENT background khi None
            binding.ivBackgroundShare.setBackgroundColor(Color.TRANSPARENT)
            binding.ivBackgroundShare.setImageDrawable(null)

            backgroundPath = null  // No background image
        } else {
            // ✅ Show background image trong cả 2 layouts
            backgroundPath = intent.getStringExtra(IntentKey.BACKGROUND_IMAGE_KEY)
            if (!backgroundPath.isNullOrEmpty()) {
                // Display layout
                binding.ivBackground.setBackgroundColor(Color.TRANSPARENT)
                binding.ivBackground.background = null
                Glide.with(this).load(backgroundPath).into(binding.ivBackground)

                // Share layout
                binding.ivBackgroundShare.setBackgroundColor(Color.TRANSPARENT)
                Glide.with(this).load(backgroundPath).into(binding.ivBackgroundShare)
            }
        }
    }

    override fun viewListener() {
        // ✅ FIX: Truyền đầy đủ thông tin khi quay lại BackgroundActivity
        binding.btnBack.onSingleClick {
            val intent = Intent(this, BackgroundActivity::class.java).apply {
                // Truyền ảnh nhân vật
                putExtra(IntentKey.INTENT_KEY, previousImagePath)

                // Truyền categoryPosition (QUAN TRỌNG!)
                putExtra(IntentKey.CATEGORY_POSITION_KEY, categoryPosition)

                // Truyền background đã chọn để pre-select
                if (!isNoneSelected && !backgroundPath.isNullOrEmpty()) {
                    putExtra(IntentKey.SUGGESTION_BACKGROUND, backgroundPath)
                }
                // Nếu đã chọn None, không cần truyền SUGGESTION_BACKGROUND
                // BackgroundActivity sẽ tự động focus vào None
            }
            startActivity(intent)
            finish()
        }

        // Add click listener for ic_home to return to HomeActivity
        binding.icHome.onSingleClick {
            startIntent(HomeActivity::class.java)
            finish()
        }

        // Add click listener for btnMyAlbum to navigate to MycreationActivity
        binding.btnMyAlbum.onSingleClick {
            val intent = Intent(this, MycreationActivity::class.java).apply {
                putExtra(IntentKey.FROM_SUCCESS, true)
            }
            startActivity(intent)
            finish()
        }

        // Add click listener for btnShare
        binding.btnShare.onSingleClick {
            shareImage()
        }
    }

    private fun shareImage() {
        lifecycleScope.launch {
            var bitmap: Bitmap? = null
            try {
                showLoading()

                // ✅ FIX 1: Clear OLD cache files (keep only last 3 most recent)
                withContext(Dispatchers.IO) {
                    val cacheDir = cacheDir
                    val shareFiles = cacheDir.listFiles()?.filter {
                        it.name.startsWith("share_") && it.name.endsWith(".png")
                    }?.sortedByDescending { it.lastModified() } ?: emptyList()

                    // Delete all except the 2 most recent (keep current + previous)
                    shareFiles.drop(2).forEach { it.delete() }
                }

                // ✅ FIX 2: Force layout to redraw before capturing
                withContext(Dispatchers.Main) {
                    binding.layoutShareCapture.invalidate()
                    binding.layoutShareCapture.requestLayout()
                }

                //  Capture từ layoutShareCapture (không có viền, full background)
                bitmap = withContext(Dispatchers.Default) {
                    BitmapHelper.createBimapFromView(binding.layoutShareCapture)
                }

                // ✅ FIX 3: Save bitmap with unique timestamp filename
                val shareFile = withContext(Dispatchers.IO) {
                    val timestamp = System.currentTimeMillis()
                    val fileName = "share_${timestamp}.png"
                    val file = java.io.File(cacheDir, fileName)

                    // Save bitmap to file
                    java.io.FileOutputStream(file).use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                        out.flush()
                    }

                    file
                }

                dismissLoading(true)

                // Create content URI using FileProvider for Android 7.0+
                val contentUri = FileProvider.getUriForFile(
                    this@SuccessActivity,
                    "${applicationContext.packageName}.provider",
                    shareFile
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

                // ✅ NOTE: We don't delete the file immediately anymore
                // Let the system handle it, or it will be cleaned up on next share
                // This ensures other apps can read the file even after we return

            } catch (e: Exception) {
                dismissLoading(true)
                showToast("Share failed: ${e.message}")
                android.util.Log.e("SuccessActivity", "Share error", e)
            } finally {
                // Clean up: Recycle bitmap to free memory
                bitmap?.recycle()
            }
        }
    }

    override fun initText() {
        // Initialize text here if needed
    }
}