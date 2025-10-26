package com.example.st181_halloween_maker.ui.mycreation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseActivity
import com.example.st181_halloween_maker.core.dialog.ConfirmDialog
import com.example.st181_halloween_maker.core.extensions.gone
import com.example.st181_halloween_maker.core.extensions.handleBack
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.extensions.show
import com.example.st181_halloween_maker.core.extensions.showToast
import com.example.st181_halloween_maker.core.helper.BitmapHelper
import com.example.st181_halloween_maker.core.helper.MediaHelper
import com.example.st181_halloween_maker.core.utils.HandleState
import com.example.st181_halloween_maker.core.utils.key.IntentKey
import com.example.st181_halloween_maker.core.utils.key.ValueKey
import com.example.st181_halloween_maker.databinding.ActivityMycreationBinding
import com.example.st181_halloween_maker.ui.view.ViewActivity
import com.girlmaker.create.avatar.creator.model.MyCreationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MycreationActivity : BaseActivity<ActivityMycreationBinding>() {

    private val myCreationAdapter by lazy { MyCreationAdapter(this) }
    private val myCreationList = ArrayList<MyCreationModel>()

    override fun setViewBinding(): ActivityMycreationBinding {
        return ActivityMycreationBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        loadSavedImages()
        initRcv()
    }

    override fun viewListener() {
        binding.apply {
            btnBack.onSingleClick {
                handleBack()
            }
        }
        handleRcv()
    }

    override fun initText() {

    }

    private fun loadSavedImages() {
        myCreationList.clear()

        // Load images from internal storage
        val imagePaths = MediaHelper.getImageInternal(this, ValueKey.DOWNLOAD_ALBUM)

        // Convert to MyCreationModel list
        imagePaths.forEach { path ->
            myCreationList.add(MyCreationModel(path, false, false))
        }

        // Show/hide no items layout
        if (myCreationList.isEmpty()) {
            binding.layoutNoItem.show()
            binding.rcv.gone()
        } else {
            binding.layoutNoItem.gone()
            binding.rcv.show()
        }
    }

    private fun initRcv() {
        binding.apply {
            rcv.adapter = myCreationAdapter
            rcv.itemAnimator = null
            myCreationAdapter.submitList(myCreationList)
        }
    }

    private fun handleRcv() {
        myCreationAdapter.onItemClick = { path ->
            // Navigate to ViewActivity with the image path
            val intent = Intent(this, ViewActivity::class.java).apply {
                putExtra(IntentKey.IMAGE_PATH_KEY, path)
            }
            startActivity(intent)
        }

        myCreationAdapter.onDeleteClick = { path, position ->
            showDeleteDialog(path, position)
        }

        myCreationAdapter.onShareClick = { path ->
            shareImage(path)
        }

        myCreationAdapter.onDownloadClick = { path ->
            downloadImage(path)
        }
    }

    private fun showDeleteDialog(imagePath: String, position: Int) {
        val dialog = ConfirmDialog(
            this,
            R.string.delete,
            R.string.do_you_want_to_delete
        )
        dialog.onYesClick = {
            deleteImage(imagePath, position)
            dialog.dismiss()
        }
        dialog.onNoClick = {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteImage(imagePath: String, position: Int) {
        lifecycleScope.launch {
            try {
                val file = File(imagePath)
                if (file.exists()) {
                    file.delete()
                    showToast(R.string.image_deleted_successfully)
                    // Remove from list and refresh
                    myCreationList.removeAt(position)
                    myCreationAdapter.submitList(myCreationList)

                    // Check if list is empty
                    if (myCreationList.isEmpty()) {
                        binding.layoutNoItem.show()
                        binding.rcv.gone()
                    }
                } else {
                    showToast(R.string.file_not_found)
                }
            } catch (e: Exception) {
                showToast(R.string.delete_failed)
            }
        }
    }

    private fun shareImage(imagePath: String) {
        lifecycleScope.launch {
            try {
                val file = File(imagePath)
                if (!file.exists()) {
                    showToast(R.string.file_not_found)
                    return@launch
                }

                // Create content URI using FileProvider
                val contentUri = FileProvider.getUriForFile(
                    this@MycreationActivity,
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
                showToast("Share failed: ${e.message}")
            }
        }
    }

    private fun downloadImage(imagePath: String) {
        lifecycleScope.launch {
            try {
                showLoading()

                val file = File(imagePath)
                if (!file.exists()) {
                    dismissLoading(true)
                    showToast(R.string.file_not_found)
                    return@launch
                }

                // Load bitmap from file
                val bitmap = withContext(Dispatchers.IO) {
                    val bitmapList = BitmapHelper.convertPathsToBitmaps(this@MycreationActivity, listOf(imagePath))
                    bitmapList.firstOrNull() ?: throw Exception("Failed to load bitmap")
                }

                // Save to external storage
                MediaHelper.saveBitmapToExternal(this@MycreationActivity, bitmap)
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

    override fun onResume() {
        super.onResume()
        // Reload images when returning to this activity
        loadSavedImages()
        myCreationAdapter.submitList(myCreationList)
    }

}