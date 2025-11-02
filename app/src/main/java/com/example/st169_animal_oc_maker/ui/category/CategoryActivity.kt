package com.example.st169_animal_oc_maker.ui.category

import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.st169_animal_oc_maker.core.base.BaseActivity
import com.example.st169_animal_oc_maker.core.extensions.handleBack
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.core.extensions.startIntent
import com.example.st169_animal_oc_maker.core.helper.AssetHelper
import com.example.st169_animal_oc_maker.core.helper.InternetHelper
import com.example.st169_animal_oc_maker.core.utils.key.ValueKey
import com.example.st169_animal_oc_maker.databinding.ActivityCategoryBinding
import com.example.st169_animal_oc_maker.ui.customize.CustomizeActivity
import com.example.st169_animal_oc_maker.ui.home.DataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue

class CategoryActivity : BaseActivity<ActivityCategoryBinding>() {
    private val dataViewModel: DataViewModel by viewModels()
    private val avatarAdapter by lazy { CategoryAdapter(this) }

    // 🚀 Track Character 0 preload status
    private var isCharacter0Preloaded = false
    private var isCharacter0Preloading = false

    override fun setViewBinding(): ActivityCategoryBinding {
        return ActivityCategoryBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        lifecycleScope.launch {
            showLoading()
            delay(300)
            dataViewModel.ensureData(this@CategoryActivity)

            // 🚀 Start preloading Character 0 in background
            preloadCharacter0InBackground()
        }
    }
    override fun dataObservable() {
        lifecycleScope.launch {
            dataViewModel.allData.collect { list ->
                if (list.isNotEmpty()) {
                    dismissLoading()
                    avatarAdapter.submitList(list)
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            btnBack.onSingleClick {
                handleBack()
            }
            swipeRefreshLayout.setOnRefreshListener {
                refreshData()
            }
        }
        handleRcv()
    }

    override fun initText() {

    }

    private fun initRcv() {
        binding.apply {
            rcv.adapter = avatarAdapter
            rcv.itemAnimator = null
        }
    }
    private fun handleRcv(){
        binding.apply {
            avatarAdapter.onItemClick = { path, position ->
                // Check internet for position 1 and 2 (0-indexed = item 2 and 3 for user)
                if (position == 1 || position == 2) {
                    if (InternetHelper.checkInternet(this@CategoryActivity)) {
                        startIntent(CustomizeActivity::class.java, position)
                    } else {
                        showNoInternetDialog()
                    }
                } else if (position == 0) {
                    // 🚀 Character 0: Preload all assets before entering CustomizeActivity
                    preloadCharacter0Assets(position)
                } else {
                    startIntent(CustomizeActivity::class.java, position)
                }
            }
        }
    }

    /**
     * 🚀 Preload Character 0 assets in background (silently)
     * Called when CategoryActivity loads - user doesn't see loading dialog
     */
    private fun preloadCharacter0InBackground() {
        if (isCharacter0Preloading || isCharacter0Preloaded) return

        isCharacter0Preloading = true
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                AssetHelper.getDataFromAsset(this@CategoryActivity)
                val endTime = System.currentTimeMillis()
                android.util.Log.d("CategoryActivity", "✅ Background preloaded Character 0 in ${endTime - startTime}ms")

                isCharacter0Preloaded = true
                isCharacter0Preloading = false
            } catch (e: Exception) {
                android.util.Log.e("CategoryActivity", "❌ Background preload failed: ${e.message}")
                isCharacter0Preloading = false
            }
        }
    }

    /**
     * 🚀 Handle Character 0 click - check if already preloaded
     */
    private fun preloadCharacter0Assets(position: Int) {
        if (isCharacter0Preloaded) {
            // Already preloaded in background - go directly!
            android.util.Log.d("CategoryActivity", "🚀 Character 0 already preloaded - entering immediately!")
            startIntent(CustomizeActivity::class.java, position)
        } else {
            // Still loading or not started - show loading dialog and wait
            android.util.Log.d("CategoryActivity", "⏳ Character 0 not ready - showing loading dialog")
            lifecycleScope.launch {
                showLoading()

                // Wait for background preload to finish
                withContext(Dispatchers.IO) {
                    while (isCharacter0Preloading) {
                        delay(100)
                    }

                    // If still not preloaded, load now
                    if (!isCharacter0Preloaded) {
                        val startTime = System.currentTimeMillis()
                        AssetHelper.getDataFromAsset(this@CategoryActivity)
                        val endTime = System.currentTimeMillis()
                        android.util.Log.d("CategoryActivity", "⏱️ Loaded Character 0 on-demand in ${endTime - startTime}ms")
                        isCharacter0Preloaded = true
                    }
                }

                dismissLoading()
                startIntent(CustomizeActivity::class.java, position)
            }
        }
    }

    private fun refreshData(){
        if (dataViewModel.allData.value.size < ValueKey.POSITION_API && InternetHelper.checkInternet(this)){
            lifecycleScope.launch {
                showLoading()
                delay(300)
                dataViewModel.ensureData(this@CategoryActivity)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }else{
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

}