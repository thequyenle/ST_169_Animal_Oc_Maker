package com.example.st181_halloween_maker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.SettingsActivity
import com.example.st181_halloween_maker.core.base.BaseActivity
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.extensions.startIntentAnim
import com.example.st181_halloween_maker.databinding.ActivityHomeBinding
import com.example.st181_halloween_maker.ui.category.CategoryActivity
import com.example.st181_halloween_maker.ui.category.CategoryAdapter
import com.example.st181_halloween_maker.ui.lissticker.ListStickerActivity
import com.example.st181_halloween_maker.ui.mycreation.MycreationActivity

class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    override fun setViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {

    }

    override fun viewListener() {
        binding.apply {
            btnCreate.onSingleClick {
                startIntentAnim(CategoryActivity::class.java)
            }
            btnSetting.onSingleClick {
                startIntentAnim(SettingsActivity::class.java)
            }
            btnSticker.onSingleClick {
                startIntentAnim(ListStickerActivity::class.java)
            }
            btnMyCreation.onSingleClick {
                startIntentAnim(MycreationActivity::class.java)
            }
        }
    }

    override fun initText() {

    }

}