package com.example.st181_halloween_maker.ui.suggestion

import android.view.LayoutInflater
import com.example.st181_halloween_maker.core.base.BaseActivity
import com.example.st181_halloween_maker.core.extensions.handleBack
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.databinding.ActivitySuggestionBinding

class SuggestionActivity : BaseActivity<ActivitySuggestionBinding>() {

    override fun setViewBinding(): ActivitySuggestionBinding {
        return ActivitySuggestionBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // Initialize view components here
    }

    override fun viewListener() {
        binding.btnBack.onSingleClick {
            handleBack()
        }
    }

    override fun initText() {
        // Initialize text here if needed
    }
}
