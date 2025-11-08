package com.animal.avatar.charactor.maker.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import com.animal.avatar.charactor.maker.R
import com.animal.avatar.charactor.maker.SettingsActivity
import com.animal.avatar.charactor.maker.core.base.BaseActivity
import com.animal.avatar.charactor.maker.core.extensions.onSingleClick
import com.animal.avatar.charactor.maker.core.extensions.showInterAll
import com.animal.avatar.charactor.maker.core.extensions.startIntentAnim
import com.animal.avatar.charactor.maker.core.utils.RatingPreferences
import com.animal.avatar.charactor.maker.databinding.ActivityHomeBinding
import com.animal.avatar.charactor.maker.dialog.RatingDialog
import com.animal.avatar.charactor.maker.ui.category.CategoryActivity
import com.animal.avatar.charactor.maker.ui.mycreation.MycreationActivity
import com.animal.avatar.charactor.maker.ui.suggestion.SuggestionActivity
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewManagerFactory
import com.lvt.ads.util.Admob
import java.lang.Void
import kotlin.system.exitProcess

class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    private val ratingPrefs by lazy {
        RatingPreferences(this)
    }

    override fun setViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // No need to setup callback, we override onBackPressed directly
    }

    override fun viewListener() {
        binding.apply {
            btnCreate.onSingleClick {
                showInterAll {
                    startIntentAnim(CategoryActivity::class.java)
                }

            }
            btnSetting.onSingleClick {
                startIntentAnim(SettingsActivity::class.java)
            }
            btnMyCreation.onSingleClick {
                startIntentAnim(MycreationActivity::class.java)
            }
            btnSuggestion.onSingleClick {
                startIntentAnim(SuggestionActivity::class.java)
            }
        }
    }

    override fun initText() {

    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        Log.d("HomeActivity", "onBackPressed() called")

        // Check if user has already rated
        val hasRated = ratingPrefs.isRated()
        Log.d("HomeActivity", "Has user rated: $hasRated")

        if (hasRated) {
            Log.d("HomeActivity", "User already rated, exiting app")
            finishAffinity()
            return
        }

        // Increment back press count
        val currentCount = ratingPrefs.incrementBackPressCount()
        Log.d("HomeActivity", "Back press count: $currentCount")

        // Show rating dialog on even numbered back presses (2, 4, 6, 8...)
        if (currentCount % 2 == 0) {
            Log.d("HomeActivity", "Even number ($currentCount) - Showing rating dialog")
            showRatingDialog()
        } else {
            Log.d("HomeActivity", "Odd number ($currentCount) - Exiting app")
            // Odd number - just exit the app
            finishAffinity()
        }
    }

    private fun showRatingDialog() {
        Log.d("HomeActivity", "showRatingDialog() called")
        RatingDialog.show(
            context = this,
            onRatingSubmitted = { rating ->
                Log.d("HomeActivity", "User submitted rating: $rating")
                // Save that user has rated
                ratingPrefs.setRated(true)
                Log.d("HomeActivity", "Rating saved, exiting app")
                finishAffinity()
                if (rating >= 4) {
                    reviewApp(this, true)
                }

            },
            onDismiss = {
                Log.d("HomeActivity", "Rating dialog dismissed, exiting app")
                // Close app when dialog is dismissed
                finishAffinity()
            }
        )
    }

    fun reviewApp(context: Activity, isBackPress: Boolean) {
        val manager = ReviewManagerFactory.create(context)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = (context as Activity?)?.let { manager.launchReviewFlow(it, reviewInfo) }
                flow?.addOnCompleteListener { task2: Task<Void> ->
                    if (isBackPress) {
                        exitProcess(0)
                    }
                }
            } else {
                if (isBackPress) {
                    exitProcess(0)
                }
            }
        }
    }
    override fun initAds() {
        Admob.getInstance().loadInterAll(this, getString(R.string.inter_all))
        Admob.getInstance().loadNativeAll(this, getString(R.string.native_all))
       // Admob.getInstance().loadNativeCollap(this, getString(R.string.native_cl_home), binding.nativeAds)
    }
}