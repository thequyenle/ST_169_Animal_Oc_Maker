package com.animal.avatar.charactor.maker.ui

import android.view.LayoutInflater
import com.animal.avatar.charactor.maker.R
import com.animal.avatar.charactor.maker.core.base.BaseActivity
import com.animal.avatar.charactor.maker.core.extensions.gone
import com.animal.avatar.charactor.maker.core.extensions.handleBackLeftToRight
import com.animal.avatar.charactor.maker.core.extensions.policy
import com.animal.avatar.charactor.maker.core.extensions.select
import com.animal.avatar.charactor.maker.core.extensions.setImageActionBar
import com.animal.avatar.charactor.maker.core.extensions.setTextActionBar
import com.animal.avatar.charactor.maker.core.extensions.shareApp
import com.animal.avatar.charactor.maker.core.extensions.startIntentRightToLeft
import com.animal.avatar.charactor.maker.core.extensions.visible
import com.animal.avatar.charactor.maker.core.utils.key.IntentKey
import com.animal.avatar.charactor.maker.core.utils.state.RateState
import com.animal.avatar.charactor.maker.databinding.ActivitySettingsBinding
import com.animal.avatar.charactor.maker.ui.language.LanguageActivity
import com.animal.avatar.charactor.maker.core.extensions.tap
import com.animal.avatar.charactor.maker.core.helper.RateHelper
import kotlin.jvm.java

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    override fun setViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRate()
    }

    override fun viewListener() {
        binding.apply {
            actionBar.btnActionBarLeft.tap { handleBackLeftToRight() }
            btnLang.tap { startIntentRightToLeft(LanguageActivity::class.java, IntentKey.INTENT_KEY) }
            btnShareApp.tap(1500) { shareApp() }
            btnRate.tap {
                RateHelper.showRateDialog(this@SettingsActivity, sharePreference){ state ->
                    if (state != RateState.CANCEL){
                        btnRate.gone()
                        showToast(R.string.have_rated)
                    }
                }
            }
            btnPolicy.tap(1500) { policy() }
        }
    }

    override fun initText() {
        binding.actionBar.tvCenter.select()
    }

    override fun initActionBar() {
        binding.actionBar.apply {
            setImageActionBar(btnActionBarLeft, R.drawable.ic_back)
            setTextActionBar(tvCenter, getString(R.string.settings))
        }
    }

    private fun initRate() {
        if (sharePreference.getIsRate(this)) {
            binding.btnRate.gone()
        } else {
            binding.btnRate.visible()
        }
    }
}