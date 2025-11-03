package com.animal.avatar.charactor.maker.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
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

        viewModel.ensureData(this)

    }

    override fun dataObservable() {
        lifecycleScope.launch {
            viewModel.allData.collect { data ->
                if (data.isNotEmpty()){
                    kotlinx.coroutines.delay(3000)

                    if (SystemUtils.isFirstLang(this@SplashActivity)) {
                        startActivity(Intent(this@SplashActivity, LanguageActivity::class.java))
                        check = true
                        finishAffinity()
                    } else {
                        startActivity(Intent(this@SplashActivity, IntroActivity::class.java))
                        check = true
                        finishAffinity()
                    }
                }
            }
        }
    }
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

}