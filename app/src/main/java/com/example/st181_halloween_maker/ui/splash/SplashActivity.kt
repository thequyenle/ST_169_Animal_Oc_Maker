package com.example.st181_halloween_maker.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.lifecycleScope
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseActivity
import com.example.st181_halloween_maker.core.helper.AssetHelper
import com.example.st181_halloween_maker.core.helper.MediaHelper
import com.example.st181_halloween_maker.core.utils.SystemUtils
import com.example.st181_halloween_maker.core.utils.key.AssetsKey
import com.example.st181_halloween_maker.data.custom.LayerListModel
import com.example.st181_halloween_maker.databinding.ActivitySplashBinding
import com.example.st181_halloween_maker.ui.home.DataViewModel
import com.example.st181_halloween_maker.ui.intro.IntroActivity
import com.example.st181_halloween_maker.ui.language.LanguageActivity
import kotlinx.coroutines.launch
import java.util.ArrayList

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

    override fun onBackPressed() {
        if (check) {
            super.onBackPressed()
        } else {
            check = false
        }
    }

}