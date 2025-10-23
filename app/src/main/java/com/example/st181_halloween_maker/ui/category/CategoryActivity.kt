package com.example.st181_halloween_maker.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseActivity
import com.example.st181_halloween_maker.core.extensions.handleBack
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.extensions.startIntent
import com.example.st181_halloween_maker.core.extensions.startIntentAnim
import com.example.st181_halloween_maker.core.helper.InternetHelper
import com.example.st181_halloween_maker.core.utils.DataLocal.getAvatarAsset
import com.example.st181_halloween_maker.core.utils.key.ValueKey
import com.example.st181_halloween_maker.databinding.ActivityCategoryBinding
import com.example.st181_halloween_maker.ui.customize.CustomizeActivity
import com.example.st181_halloween_maker.ui.home.DataViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue

class CategoryActivity : BaseActivity<ActivityCategoryBinding>() {
    private val dataViewModel: DataViewModel by viewModels()
    private val avatarAdapter by lazy { CategoryAdapter(this) }
    override fun setViewBinding(): ActivityCategoryBinding {
        return ActivityCategoryBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        lifecycleScope.launch {
            showLoading()
            delay(300)
            dataViewModel.ensureData(this@CategoryActivity)
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