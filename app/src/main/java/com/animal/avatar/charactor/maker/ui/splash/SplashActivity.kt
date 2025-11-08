package com.animal.avatar.charactor.maker.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.animal.avatar.charactor.maker.R
//quyen
import com.lvt.ads.callback.InterCallback
import com.lvt.ads.util.Admob
//quyen
import com.animal.avatar.charactor.maker.core.base.BaseActivity
import com.animal.avatar.charactor.maker.core.utils.SystemUtils
import com.animal.avatar.charactor.maker.databinding.ActivitySplashBinding
import com.animal.avatar.charactor.maker.ui.home.DataViewModel
import com.animal.avatar.charactor.maker.ui.intro.IntroActivity
import com.animal.avatar.charactor.maker.ui.language.LanguageActivity
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    private var check = false
    private val viewModel: DataViewModel by viewModels()
    //quyen
    var interCallBack: InterCallback? = null
    //quyen
    override fun setViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this))
    }



    override fun initView() {
        if (!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intent.action != null && intent.action.equals(
                Intent.ACTION_MAIN
            )
        ) {
            finish(); return;
        }
        Admob.getInstance().setTimeLimitShowAds(30000)
        viewModel.ensureData(this)

    }

    override fun dataObservable() {
        lifecycleScope.launch {
            viewModel.allData.collect { data ->
                if (data.isNotEmpty()){
                    kotlinx.coroutines.delay(3000)
                    //quyen
                    moveNextScreen()
                    //quyen
                }
            }
        }
    }

    //quyen
    private fun moveNextScreen() {
        val nextIntent = if (SystemUtils.isFirstLang(this)) {
            Intent(this, LanguageActivity::class.java)
        } else {
            Intent(this, IntroActivity::class.java)
        }

        interCallBack = object : InterCallback() {
            override fun onNextAction() {
                super.onNextAction()
                startActivity(nextIntent)
                check = true
                finishAffinity()
            }
        }

        Admob.getInstance().loadSplashInterAds(
            this,
            getString(R.string.inter_splash),
            30000,
            3000,
            interCallBack
        )
    }
    //quyen
    override fun viewListener() {

    }

    override fun initText() {

    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (check) {
           // super.onBackPressed()
        } else {
            check = false
        }
    }

    //quyen
    override fun onResume() {
        super.onResume()
        Admob.getInstance().onCheckShowSplashWhenFail(this, interCallBack, 1000)
    }
    //quyen

}