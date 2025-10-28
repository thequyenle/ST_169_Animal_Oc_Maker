package com.example.st169_animal_oc_maker.ui.success

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
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

        // Load ảnh nhân vật
        if (!previousImagePath.isNullOrEmpty()) {
            Glide.with(this).load(previousImagePath).into(binding.imvImage)
        }

        // Handle background
        if (isNoneSelected) {
            // ✅ Show background color for None selection
            backgroundColor = intent.getStringExtra(IntentKey.BACKGROUND_COLOR_KEY)
            backgroundColor?.let {
                binding.ivBackground.setBackgroundColor(Color.parseColor(it))
            }
            backgroundPath = null  // No background image
        } else {
            // ✅ Show background image
            backgroundPath = intent.getStringExtra(IntentKey.BACKGROUND_IMAGE_KEY)
            if (!backgroundPath.isNullOrEmpty()) {
                binding.ivBackground.setBackgroundColor(Color.TRANSPARENT)
                binding.ivBackground.background = null
                Glide.with(this).load(backgroundPath).into(binding.ivBackground)
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

                // Capture from layout
                bitmap = withContext(Dispatchers.Default) {
                    BitmapHelper.createBimapFromView(binding.layoutCustomLayer)
                }

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
                // Clean up: Recycle bitmap to free memory
                bitmap?.recycle()
            }
        }
    }

    override fun initText() {
        // Initialize text here if needed
    }
}