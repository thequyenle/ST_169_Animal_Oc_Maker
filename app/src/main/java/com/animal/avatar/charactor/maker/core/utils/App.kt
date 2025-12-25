package com.animal.avatar.charactor.maker.core.utils

import com.lvt.ads.util.AdsApplication
import com.lvt.ads.util.AppOpenManager
import com.animal.avatar.charactor.maker.R
import com.animal.avatar.charactor.maker.ui.splash.SplashActivity
import kotlin.jvm.java

class App : AdsApplication() {


    override fun onCreate() {
        super.onCreate()
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)
    }

    override fun enableAdsResume(): Boolean {
        return true
    }

    override fun getListTestDeviceId(): MutableList<String>? {
        return null
    }

    override fun getResumeAdId(): String {
        return getString(R.string.open_resume)
    }

    override fun buildDebug(): Boolean {
        return true
    }
}