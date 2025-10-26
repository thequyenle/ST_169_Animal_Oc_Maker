package com.example.st181_halloween_maker.ui.suggestion

import android.content.Intent
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.st181_halloween_maker.core.base.BaseActivity
import com.example.st181_halloween_maker.core.extensions.handleBack
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.utils.key.IntentKey
import com.example.st181_halloween_maker.data.suggestion.SuggestionModel
import com.example.st181_halloween_maker.databinding.ActivitySuggestionBinding
import com.example.st181_halloween_maker.ui.customize.CustomizeActivity
import com.example.st181_halloween_maker.ui.home.DataViewModel
import kotlinx.coroutines.launch

class SuggestionActivity : BaseActivity<ActivitySuggestionBinding>() {

    private val dataViewModel: DataViewModel by viewModels()
    private val suggestionViewModel: SuggestionViewModel by viewModels()

    override fun setViewBinding(): ActivitySuggestionBinding {
        return ActivitySuggestionBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // Load data và generate suggestions
        lifecycleScope.launch {
            showLoading()

            // Ensure data is loaded
            dataViewModel.ensureData(this@SuggestionActivity)

            // Wait for data
            dataViewModel.allData.collect { allData ->
                if (allData.isNotEmpty()) {
                    // Generate suggestions
                    suggestionViewModel.generateAllSuggestions(allData, this@SuggestionActivity)
                }
            }
        }

        // Observe suggestions and display
        lifecycleScope.launch {
            suggestionViewModel.suggestions.collect { suggestions ->
                if (suggestions.isNotEmpty()) {
                    dismissLoading()
                    displaySuggestions(suggestions)
                }
            }
        }
    }

    override fun viewListener() {
        binding.btnBack.onSingleClick {
            handleBack()
        }
    }

    override fun initText() {
        // Initialize text here if needed
    }

    /**
     * Display all 6 suggestions (2 for each category)
     */
    private fun displaySuggestions(suggestions: List<SuggestionModel>) {
        // Tommy suggestions (category 0)
        val tommySuggestions = suggestions.filter { it.categoryPosition == 0 }
        if (tommySuggestions.size >= 2) {
            loadSuggestion(tommySuggestions[0], binding.imvTommy1)
            loadSuggestion(tommySuggestions[1], binding.imvTommy2)
        }

        // Miley suggestions (category 1)
        val mileySuggestions = suggestions.filter { it.categoryPosition == 1 }
        if (mileySuggestions.size >= 2) {
            loadSuggestion(mileySuggestions[0], binding.imvMiley1)
            loadSuggestion(mileySuggestions[1], binding.imvMiley2)
        }

        // Dammy suggestions (category 2)
        val dammySuggestions = suggestions.filter { it.categoryPosition == 2 }
        if (dammySuggestions.size >= 2) {
            loadSuggestion(dammySuggestions[0], binding.imvDammy1)
            loadSuggestion(dammySuggestions[1], binding.imvDammy2)
        }
    }

    /**
     * Load suggestion thumbnail và set click listener
     */
    private fun loadSuggestion(
        suggestion: SuggestionModel,
        imageView: android.widget.ImageView
    ) {
        // Load thumbnail (sử dụng avatar làm placeholder, sau này có thể generate bitmap)
        Glide.with(this)
            .load(suggestion.characterData)
            .into(imageView)

        // Set click listener
        imageView.onSingleClick {
            openCustomizeWithSuggestion(suggestion)
        }
    }

    /**
     * Open CustomizeActivity with suggestion preset
     */
    private fun openCustomizeWithSuggestion(suggestion: SuggestionModel) {
        val intent = Intent(this, CustomizeActivity::class.java).apply {
            putExtra(IntentKey.CATEGORY_POSITION_KEY, suggestion.categoryPosition)
            putExtra(IntentKey.IS_SUGGESTION, true)
            putExtra(IntentKey.SUGGESTION_STATE, suggestion.randomState.toJson())
            putExtra(IntentKey.SUGGESTION_BACKGROUND, suggestion.background)
        }
        startActivity(intent)
    }
}