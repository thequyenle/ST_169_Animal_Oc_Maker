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
import kotlinx.coroutines.flow.combine
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
        // Observe suggestions và thumbnails kết hợp
        lifecycleScope.launch {
            combine(
                suggestionViewModel.suggestions,
                suggestionViewModel.thumbnails
            ) { suggestions: List<SuggestionModel>, thumbnails: Map<String, Bitmap> ->
                Pair(suggestions, thumbnails)
            }.collect { (suggestions, thumbnails) ->
                if (suggestions.isNotEmpty() && thumbnails.isNotEmpty()) {
                    dismissLoading()
                    displaySuggestions(suggestions, thumbnails)
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
     * Load suggestion thumbnail và set click listener
     */
    private fun loadSuggestion(
        suggestion: SuggestionModel,
        imageView: android.widget.ImageView,
        thumbnails: Map<String, android.graphics.Bitmap>
    ) {
        // Load actual thumbnail bitmap
        val thumbnail = thumbnails[suggestion.id]
        if (thumbnail != null) {
            imageView.setImageBitmap(thumbnail)
        } else {
            // Fallback: Load avatar if thumbnail generation failed
            Glide.with(this)
                .load(suggestion.characterData)
                .into(imageView)
        }

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
            putExtra(IntentKey.CHARACTER_INDEX, suggestion.characterIndex)
            putExtra(IntentKey.IS_SUGGESTION, true)
            putExtra(IntentKey.SUGGESTION_STATE, suggestion.randomState.toJson())
            putExtra(IntentKey.SUGGESTION_BACKGROUND, suggestion.background)
        }
        startActivity(intent)
    }
}