package com.example.st169_animal_oc_maker

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import com.example.st169_animal_oc_maker.core.base.BaseActivity
import com.example.st169_animal_oc_maker.core.extensions.gone
import com.example.st169_animal_oc_maker.core.extensions.handleBack
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.core.utils.RatingPreferences
import com.example.st169_animal_oc_maker.databinding.ActivitySettingsBinding
import com.example.st169_animal_oc_maker.dialog.RatingDialog
import com.example.st169_animal_oc_maker.ui.language.LanguageActivity

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {

    private val ratingPrefs by lazy {
        RatingPreferences(this)
    }

    override fun setViewBinding(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(LayoutInflater.from(this))
    }



    override fun initView() {
        // Load and apply rating status
        if (ratingPrefs.isRated()) {
            binding.btnRate.gone()
        }
    }

    override fun viewListener() {
        binding.apply {
            btnBack.onSingleClick { handleBack() }

            btnLang.onSingleClick { navigateToLanguage() }

            btnRate.onSingleClick { showRatingDialog() }

            btnShare.onSingleClick { shareApp() }

            btnPolicy.onSingleClick { openPrivacyPolicy() }
        }
    }

    override fun initText() {
        // No text initialization needed
    }

    private fun navigateToLanguage() {
        val intent = Intent(this, LanguageActivity::class.java).apply {
            putExtra("from_settings", true)
        }
        startActivity(intent)
    }

    private fun showRatingDialog() {
        RatingDialog.show(
            this,
            onRatingSubmitted = { rating ->
                handleRatingSubmitted()
            },
            onDismiss = {
                // Dialog dismissed without rating
            }
        )
    }

    private fun handleRatingSubmitted() {
        ratingPrefs.setRated(true)
        animateAndHideRateButton()
    }

    private fun animateAndHideRateButton() {
        binding.btnRate.animate()
            .alpha(0f)
            .translationY(-binding.btnRate.height.toFloat())
            .setDuration(300)
            .withEndAction {
                binding.btnRate.visibility = View.GONE
            }
            .start()
    }

    private fun shareApp() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out this amazing app: http://play.google.com/store/apps/details?id=${packageName}"
            )
        }
        startActivity(Intent.createChooser(shareIntent, "Share app via"))
    }

    private fun openPrivacyPolicy() {
        val url = "https://sites.google.com/view/docx-reader-office-viewer/home"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = android.net.Uri.parse(url)
        }
        startActivity(intent)
    }
}