package com.example.st181_halloween_maker.ui.mycreation

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseActivity
import com.example.st181_halloween_maker.core.extensions.gone
import com.example.st181_halloween_maker.core.extensions.handleBack
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.extensions.show
import com.example.st181_halloween_maker.core.helper.MediaHelper
import com.example.st181_halloween_maker.core.utils.key.ValueKey
import com.example.st181_halloween_maker.databinding.ActivityMycreationBinding
import com.girlmaker.create.avatar.creator.model.MyCreationModel

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
            // Handle item click - you can add navigation or preview here if needed
        }
    }

    override fun onResume() {
        super.onResume()
        // Reload images when returning to this activity
        loadSavedImages()
        myCreationAdapter.submitList(myCreationList)
    }

}