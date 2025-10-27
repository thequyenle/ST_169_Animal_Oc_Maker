package com.example.st169_animal_oc_maker.ui.home

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import com.example.st169_animal_oc_maker.SettingsActivity
import com.example.st169_animal_oc_maker.core.base.BaseActivity
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.core.extensions.startIntentAnim
import com.example.st169_animal_oc_maker.core.utils.RatingPreferences
import com.example.st169_animal_oc_maker.databinding.ActivityHomeBinding
import com.example.st169_animal_oc_maker.dialog.RatingDialog
import com.example.st169_animal_oc_maker.ui.category.CategoryActivity
import com.example.st169_animal_oc_maker.ui.mycreation.MycreationActivity
import com.example.st169_animal_oc_maker.ui.suggestion.SuggestionActivity

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
                startIntentAnim(CategoryActivity::class.java)
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
            },
            onDismiss = {
                Log.d("HomeActivity", "Rating dialog dismissed, exiting app")
                // Close app when dialog is dismissed
                finishAffinity()
            }
        )
    }
}