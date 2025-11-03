package com.animal.avatar.charactor.maker.ui.intro

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import com.animal.avatar.charactor.maker.core.base.BaseActivity
import com.animal.avatar.charactor.maker.core.utils.DataLocal
import com.animal.avatar.charactor.maker.core.utils.SystemUtils
import com.animal.avatar.charactor.maker.databinding.ActivityIntroBinding
import com.animal.avatar.charactor.maker.ui.home.HomeActivity
import com.animal.avatar.charactor.maker.ui.permission.PermissionActivity
import kotlin.system.exitProcess

class IntroActivity : BaseActivity<ActivityIntroBinding>() {
    private var checkStarHome = false
    private val adapter = IntroAdapter(this, DataLocal.itemIntroList)
    override fun setViewBinding(): ActivityIntroBinding {
        return ActivityIntroBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initVpg()
    }

    override fun viewListener() {
        binding.txtNext.setOnClickListener {
            handleNext()
        }
    }

    override fun initText() {

    }
    private fun initVpg() {
        binding.apply {
            binding.vpgTutorial.adapter = adapter
            binding.dotsIndicator.setViewPager2(binding.vpgTutorial)

        }
    }
    private fun handleNext(){
        binding.apply {
            val nextItem = binding.vpgTutorial.currentItem + 1
            if (nextItem < DataLocal.itemIntroList.size) {
                vpgTutorial.setCurrentItem(nextItem, true)
            } else {
                if (!checkStarHome) {
                    if (SystemUtils.getFirstPermission(this@IntroActivity)) {
                        checkStarHome = true
                        val intent = Intent(this@IntroActivity, PermissionActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        checkStarHome = true
                        val intent = Intent(this@IntroActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    }
                }
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        exitProcess(0)
    }

}