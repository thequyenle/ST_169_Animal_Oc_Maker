package com.example.st169_animal_oc_maker.ui.suggestion

import android.content.Intent
import android.graphics.Bitmap
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.st169_animal_oc_maker.core.base.BaseActivity
import com.example.st169_animal_oc_maker.core.extensions.handleBack
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.core.utils.key.IntentKey
import com.example.st169_animal_oc_maker.data.suggestion.SuggestionModel
import com.example.st169_animal_oc_maker.databinding.ActivitySuggestionBinding
import com.example.st169_animal_oc_maker.ui.customize.CustomizeActivity
import com.example.st169_animal_oc_maker.ui.home.DataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SuggestionActivity : BaseActivity<ActivitySuggestionBinding>() {

    private val dataViewModel: DataViewModel by viewModels()
    private val suggestionViewModel: SuggestionViewModel by viewModels()

    override fun setViewBinding(): ActivitySuggestionBinding {
        return ActivitySuggestionBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // ✅ OPTIMIZED: Progressive loading strategy
        // 1. Show suggestions with placeholders ASAP
        // 2. Update thumbnails progressively as they load

        // Observe suggestions - show immediately with placeholders
        lifecycleScope.launch {
            suggestionViewModel.suggestions.collect { suggestions ->
                if (suggestions.isNotEmpty()) {
                    dismissLoading()
                    // Display with placeholders first
                    displaySuggestions(suggestions, emptyMap())
                }
            }
        }

        // Observe thumbnails - update progressively as each loads
        lifecycleScope.launch {
            suggestionViewModel.thumbnails.collect { thumbnails ->
                if (thumbnails.isNotEmpty()) {
                    // Update only the thumbnails that are ready
                    val suggestions = suggestionViewModel.suggestions.value
                    if (suggestions.isNotEmpty()) {
                        displaySuggestions(suggestions, thumbnails)
                    }
                }
            }
        }

        // Load data và generate suggestions - Tối ưu với background thread
        lifecycleScope.launch {
            showLoading()

            try {
                // ✅ Chạy tác vụ nặng trong background thread (Dispatchers.IO)
                withContext(Dispatchers.IO) {
                    // Ensure data is loaded
                    dataViewModel.ensureData(this@SuggestionActivity)

                    // Lấy data một lần khi đã có
                    val allData = dataViewModel.allData.first { it.isNotEmpty() }

                    // Generate suggestions với thumbnails
                    suggestionViewModel.generateAllSuggestions(allData, this@SuggestionActivity)
                }

                // dismissLoading() sẽ được gọi tự động khi suggestions emit data
                // thông qua observer ở trên (lines 32-44)
            } catch (e: Exception) {
                // ✅ Đảm bảo dismiss loading nếu có lỗi
                dismissLoading()
                // TODO: Handle error (show error message)
                e.printStackTrace()
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
    private fun displaySuggestions(
        suggestions: List<SuggestionModel>,
        thumbnails: Map<String, android.graphics.Bitmap>
    ) {
        // Tommy suggestions (category 0)
        val tommySuggestions = suggestions.filter { it.categoryPosition == 0 }
        if (tommySuggestions.size >= 2) {
            loadSuggestion(tommySuggestions[0], binding.imvTommy1, thumbnails)
            loadSuggestion(tommySuggestions[1], binding.imvTommy2, thumbnails)
        }

        // Miley suggestions (category 1)
        val mileySuggestions = suggestions.filter { it.categoryPosition == 1 }
        if (mileySuggestions.size >= 2) {
            loadSuggestion(mileySuggestions[0], binding.imvMiley1, thumbnails)
            loadSuggestion(mileySuggestions[1], binding.imvMiley2, thumbnails)
        }

        // Dammy suggestions (category 2)
        val dammySuggestions = suggestions.filter { it.categoryPosition == 2 }
        if (dammySuggestions.size >= 2) {
            loadSuggestion(dammySuggestions[0], binding.imvDammy1, thumbnails)
            loadSuggestion(dammySuggestions[1], binding.imvDammy2, thumbnails)
        }
    }

    /**
     * ✅ OPTIMIZED: Load suggestion thumbnail with progressive loading
     * - Show placeholder immediately if thumbnail not ready
     * - Update to actual thumbnail when available
     */
    private fun loadSuggestion(
        suggestion: SuggestionModel,
        imageView: android.widget.ImageView,
        thumbnails: Map<String, android.graphics.Bitmap>
    ) {
        val thumbnail = thumbnails[suggestion.id]
        if (thumbnail != null) {
            // ✅ Thumbnail ready - show it
            imageView.setImageBitmap(thumbnail)
        } else {
            // ✅ Thumbnail not ready yet - show placeholder
            // Use Glide for smooth loading with placeholder
            Glide.with(this)
                .load(suggestion.characterData)
                .placeholder(android.R.drawable.ic_menu_gallery) // System placeholder
                .into(imageView)
        }

        // Set click listener (always clickable, even with placeholder)
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
            putExtra(IntentKey.CHARACTER_INDEX, suggestion.characterIndex)
            putExtra(IntentKey.IS_SUGGESTION, true)
            putExtra(IntentKey.SUGGESTION_STATE, suggestion.randomState.toJson())
            putExtra(IntentKey.SUGGESTION_BACKGROUND, suggestion.background)
        }
        startActivity(intent)
    }
}