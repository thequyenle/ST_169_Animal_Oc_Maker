package com.example.st181_halloween_maker.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseActivity
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.extensions.startIntent
import com.example.st181_halloween_maker.core.utils.key.IntentKey
import com.example.st181_halloween_maker.databinding.ActivityViewBinding
import com.example.st181_halloween_maker.ui.home.HomeActivity

class ViewActivity : BaseActivity<ActivityViewBinding>() {
    override fun setViewBinding(): ActivityViewBinding {
        return ActivityViewBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // Get the image path from intent
        val imagePath = intent.getStringExtra(IntentKey.INTENT_KEY)

        // Display the image in imvImage
        if (!imagePath.isNullOrEmpty()) {
            Glide.with(this)
                .load(imagePath)
                .into(binding.imvImage)
        }
    }

    override fun viewListener() {
        // Add click listener for ic_home to return to HomeActivity
        binding.icHome.onSingleClick {
            startIntent(HomeActivity::class.java)
            finish()
        }
    }

    override fun initText() {

    }

}