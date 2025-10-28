package com.example.st169_animal_oc_maker.ui.mycreation

import android.content.Intent
import android.view.LayoutInflater
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.st169_animal_oc_maker.R
import com.example.st169_animal_oc_maker.core.base.BaseActivity
import com.example.st169_animal_oc_maker.core.dialog.ConfirmDialog
import com.example.st169_animal_oc_maker.core.extensions.gone
import com.example.st169_animal_oc_maker.core.extensions.handleBack
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.core.extensions.show
import com.example.st169_animal_oc_maker.core.extensions.showToast
import com.example.st169_animal_oc_maker.core.helper.BitmapHelper
import com.example.st169_animal_oc_maker.core.helper.MediaHelper
import com.example.st169_animal_oc_maker.core.utils.HandleState
import com.example.st169_animal_oc_maker.core.utils.key.IntentKey
import com.example.st169_animal_oc_maker.core.utils.key.ValueKey
import com.example.st169_animal_oc_maker.databinding.ActivityMycreationBinding
import com.example.st169_animal_oc_maker.ui.home.HomeActivity
import com.example.st169_animal_oc_maker.ui.view.ViewActivity
import com.girlmaker.create.avatar.creator.model.MyCreationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MycreationActivity : BaseActivity<ActivityMycreationBinding>() {

    private val myCreationAdapter by lazy { MyCreationAdapter(this) }
    private val myCreationList = ArrayList<MyCreationModel>()
    private var isSelectionMode = false
    private var fromSuccess = false

    override fun setViewBinding(): ActivityMycreationBinding {
        return ActivityMycreationBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // Check if navigated from SuccessActivity
        fromSuccess = intent.getBooleanExtra(IntentKey.FROM_SUCCESS, false)

        loadSavedImages()
        initRcv()
        // Click on ScrollView to exit selection mode
        // Click on ScrollView to exit selection mode
        // Click on ScrollView content to exit selection mode
        binding.rcv.setOnClickListener {
            if (isSelectionMode) {
                exitSelectionMode()
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            // Click outside to exit selection mode
            main.setOnClickListener {
                if (isSelectionMode) {
                    exitSelectionMode()
                }
            }

            btnBack.onSingleClick {
                if (isSelectionMode) {
                    exitSelectionMode()
                } else {
                    // If from SuccessActivity, navigate to HomeActivity
                    if (fromSuccess) {
                        val intent = Intent(this@MycreationActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        handleBack()
                    }
                }
            }



            // Delete button - delete selected items
            delete.onSingleClick {
                deleteSelectedItems()
            }

            // Download button - download selected items
            btnDownload.onSingleClick {
                executeWithStoragePermission {
                    downloadSelectedItems()
                }
            }

            // Share button - share selected items
            btnShare.onSingleClick {
                shareSelectedItems()
            }

            // Select All button
            btnSelectAll.onSingleClick {
                toggleSelectAll()
            }
        }
        handleRcv()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (fromSuccess) {
            val intent = Intent(this@MycreationActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            handleBack()
        }
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

            // Click on empty space in RecyclerView to exit selection mode
            rcv.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: android.view.MotionEvent): Boolean {
                    if (isSelectionMode && e.action == android.view.MotionEvent.ACTION_UP) {
                        val child = rv.findChildViewUnder(e.x, e.y)
                        if (child == null) {
                            // Clicked on empty space
                            exitSelectionMode()
                            return true
                        }
                    }
                    return false
                }
            })
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
            executeWithStoragePermission {
                downloadImage(path)
            }
        }

        // Handle long press to enter selection mode
        myCreationAdapter.onLongClick = { position ->
            enterSelectionMode(position)
        }

        // Handle item tick/untick
        myCreationAdapter.onItemTick = { position ->
            toggleItemSelection(position)
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

                    // Exit selection mode after deleting
                    if (isSelectionMode) {
                        exitSelectionMode()
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
        if (!isSelectionMode) {
            loadSavedImages()
            myCreationAdapter.submitList(myCreationList)
        }
    }

    // ==================== SELECTION MODE FUNCTIONS ====================

    private fun enterSelectionMode(position: Int) {
        if (!isSelectionMode) {
            isSelectionMode = true

            // Show selection UI
            binding.delete.show()
            binding.btnDownload.show()
            binding.btnShare.show()
            binding.btnSelectAll.show()

            // Enable selection mode for all items
            myCreationList.forEach { it.isShowSelection = true }

            // Select the long-pressed item
            myCreationList[position].isSelected = true

            // Update adapter
            myCreationAdapter.submitList(myCreationList)

            // Update Select All checkbox
            updateSelectAllCheckbox()
        }
    }

    private fun exitSelectionMode() {
        isSelectionMode = false

        // Hide selection UI
        binding.delete.gone()
        binding.btnDownload.gone()
        binding.btnShare.gone()
        binding.btnSelectAll.gone()

        // Disable selection mode and clear selections
        myCreationList.forEach {
            it.isShowSelection = false
            it.isSelected = false
        }

        // Update adapter
        myCreationAdapter.submitList(myCreationList)
    }

    private fun toggleItemSelection(position: Int) {
        myCreationList[position].isSelected = !myCreationList[position].isSelected
        myCreationAdapter.submitItem(position, myCreationList[position].isSelected)
        updateSelectAllCheckbox()
    }

    private fun toggleSelectAll() {
        val allSelected = myCreationList.all { it.isSelected }

        myCreationList.forEach { it.isSelected = !allSelected }
        myCreationAdapter.submitList(myCreationList)
        updateSelectAllCheckbox()
    }

    private fun updateSelectAllCheckbox() {
        val allSelected = myCreationList.isNotEmpty() && myCreationList.all { it.isSelected }
        binding.checkImg.setImageResource(
            if (allSelected) R.drawable.ic_check else R.drawable.ic_uncheck
        )
    }

    private fun getSelectedItems(): List<MyCreationModel> {
        return myCreationList.filter { it.isSelected }
    }

    private fun deleteSelectedItems() {
        val selectedItems = getSelectedItems()
        if (selectedItems.isEmpty()) {
            showToast(R.string.please_select_at_least_one_item)
            return
        }

        val dialog = ConfirmDialog(
            this,
            R.string.delete,
            R.string.do_you_want_to_delete
        )
        dialog.onYesClick = {
            lifecycleScope.launch {
                try {
                    var deletedCount = 0
                    selectedItems.forEach { item ->
                        val file = File(item.path)
                        if (file.exists() && file.delete()) {
                            deletedCount++
                            myCreationList.remove(item)
                        }
                    }

                    if (deletedCount > 0) {
                        showToast(getString(R.string.deleted_successfully, deletedCount))
                        myCreationAdapter.submitList(myCreationList)

                        // Check if list is empty
                        if (myCreationList.isEmpty()) {
                            binding.layoutNoItem.show()
                            binding.rcv.gone()
                        }

                        // Always exit selection mode after deleting
                        exitSelectionMode()
                    }
                } catch (e: Exception) {
                    showToast(R.string.delete_failed)
                }
            }
            dialog.dismiss()
        }
        dialog.onNoClick = {
            dialog.dismiss()
        }
        dialog.show()
    }
    private fun downloadSelectedItems() {
        val selectedItems = getSelectedItems()
        if (selectedItems.isEmpty()) {
            showToast(R.string.please_select_at_least_one_item)
            return
        }

        lifecycleScope.launch {
            try {
                showLoading()

                val selectedPaths = selectedItems.map { it.path }
                val bitmaps = withContext(Dispatchers.IO) {
                    BitmapHelper.convertPathsToBitmaps(this@MycreationActivity, selectedPaths)
                }

                var savedCount = 0
                bitmaps.forEach { bitmap ->
                    MediaHelper.saveBitmapToExternal(this@MycreationActivity, bitmap)
                        .collect { state ->
                            when (state) {
                                HandleState.SUCCESS -> savedCount++
                                else -> {}
                            }
                        }
                }

                dismissLoading(true)
                if (savedCount > 0) {
                    showToast(getString(R.string.saved_successfully, savedCount))
                } else {
                    showToast(R.string.save_failed_please_try_again)
                }

            } catch (e: Exception) {
                dismissLoading(true)
                showToast(R.string.save_failed_please_try_again)
            }
        }
    }

    private fun shareSelectedItems() {
        val selectedItems = getSelectedItems()
        if (selectedItems.isEmpty()) {
            showToast(R.string.please_select_at_least_one_item)
            return
        }

        lifecycleScope.launch {
            try {
                val uris = ArrayList<android.net.Uri>()

                selectedItems.forEach { item ->
                    val file = File(item.path)
                    if (file.exists()) {
                        val contentUri = FileProvider.getUriForFile(
                            this@MycreationActivity,
                            "${applicationContext.packageName}.provider",
                            file
                        )
                        uris.add(contentUri)
                    }
                }

                if (uris.isEmpty()) {
                    showToast(R.string.file_not_found)
                    return@launch
                }

                val shareIntent = Intent().apply {
                    if (uris.size == 1) {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, uris[0])
                    } else {
                        action = Intent.ACTION_SEND_MULTIPLE
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    }
                    type = "image/png"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                val chooser = Intent.createChooser(shareIntent, getString(R.string.share))
                startActivity(chooser)

            } catch (e: Exception) {
                showToast("Share failed: ${e.message}")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                // Permission granted, execute download
                pendingActionAfterPermission?.invoke()
                pendingActionAfterPermission = null
            } else {
                // Permission denied - show dialog to go to Settings
                handleStoragePermissionDenied()
                pendingActionAfterPermission = null
            }
        }
    }
}