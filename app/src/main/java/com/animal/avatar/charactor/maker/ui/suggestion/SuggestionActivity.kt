package com.animal.avatar.charactor.maker.ui.suggestion

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
//quyen
import com.lvt.ads.util.Admob
//quyen
import com.animal.avatar.charactor.maker.R
import com.animal.avatar.charactor.maker.core.base.BaseActivity
import com.animal.avatar.charactor.maker.core.extensions.handleBack
import com.animal.avatar.charactor.maker.core.extensions.onSingleClick
import com.animal.avatar.charactor.maker.core.extensions.showInterAll
import com.animal.avatar.charactor.maker.core.helper.InternetHelper
import com.animal.avatar.charactor.maker.core.utils.key.IntentKey
import com.animal.avatar.charactor.maker.data.suggestion.SuggestionModel
import com.animal.avatar.charactor.maker.databinding.ActivitySuggestionBinding
import com.animal.avatar.charactor.maker.ui.customize.CustomizeActivity
import com.animal.avatar.charactor.maker.ui.home.DataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SuggestionActivity : BaseActivity<ActivitySuggestionBinding>() {

    private val dataViewModel: DataViewModel by viewModels()
    private val suggestionViewModel: SuggestionViewModel by viewModels()

    private lateinit var tommyAdapter: SuggestionAdapter
    private lateinit var mileyAdapter: SuggestionAdapter
    private lateinit var dammyAdapter: SuggestionAdapter

    override fun setViewBinding(): ActivitySuggestionBinding {
        return ActivitySuggestionBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        // Setup RecyclerViews và adapters
        setupRecyclerViews()

        // Observe suggestions - chỉ setup adapters một lần
        lifecycleScope.launch {
            suggestionViewModel.suggestions.collect { suggestions ->
                if (suggestions.isNotEmpty()) {
                    dismissLoading()
                    displaySuggestions(suggestions)
                    Log.d("SuggestionActivity", "✅ Displayed ${suggestions.size} suggestions")
                }
            }
        }

        // Observe thumbnails - chỉ update thumbnails trong adapters
        lifecycleScope.launch {
            suggestionViewModel.thumbnails.collect { thumbnails ->
                if (thumbnails.isNotEmpty()) {
                    updateThumbnails(thumbnails)
                    Log.d("SuggestionActivity", "✅ Updated ${thumbnails.size} thumbnails")
                }
            }
        }

        // Load data
        lifecycleScope.launch {
            showLoading()
            try {
                withContext(Dispatchers.IO) {
                    dataViewModel.ensureData(this@SuggestionActivity)
                    val allData = dataViewModel.allData.first { it.isNotEmpty() }

                    Log.d("SuggestionActivity", "🚀 Starting to generate 10 suggestions per category (optimized)...")

                    // Generate 10 suggestions per category (optimized với semaphore để tránh lag/crash)
                    suggestionViewModel.generateAllSuggestions(
                        allData,
                        this@SuggestionActivity,
                        suggestionsPerCategory = 10
                    )
                }
            } catch (e: Exception) {
                dismissLoading()
                Log.e("SuggestionActivity", "❌ Error loading data: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun setupRecyclerViews() {
        // Use WrapContentGridLayoutManager to properly handle wrap_content in ScrollView
        binding.rcvTommy.apply {
            layoutManager = com.animal.avatar.charactor.maker.core.custom.WrapContentGridLayoutManager(this@SuggestionActivity, 2)
            isNestedScrollingEnabled = false
        }
        binding.rcvMiley.apply {
            layoutManager = com.animal.avatar.charactor.maker.core.custom.WrapContentGridLayoutManager(this@SuggestionActivity, 2)
            isNestedScrollingEnabled = false
        }
        binding.rcvDammy.apply {
            layoutManager = com.animal.avatar.charactor.maker.core.custom.WrapContentGridLayoutManager(this@SuggestionActivity, 2)
            isNestedScrollingEnabled = false
        }
    }

    override fun viewListener() {
        binding.btnBack.onSingleClick {
            //quyen
            showInterAll {
                handleBack()
            }
            //quyen
        }
    }

    private fun displaySuggestions(suggestions: List<SuggestionModel>) {
        Log.d("SuggestionActivity", "========================================")
        Log.d("SuggestionActivity", "📋 DISPLAYING SUGGESTIONS")
        Log.d("SuggestionActivity", "Total suggestions received: ${suggestions.size}")

        // Tommy suggestions (category 0) - 10 items
        val tommySuggestions = suggestions.filter { it.categoryPosition == 0 }.take(10)
        Log.d("SuggestionActivity", "Tommy filtered: ${tommySuggestions.size} items")
        if (!::tommyAdapter.isInitialized) {
            tommyAdapter = SuggestionAdapter(::openCustomizeWithSuggestion)
            binding.rcvTommy.adapter = tommyAdapter
            Log.d("SuggestionActivity", "Tommy adapter initialized")
        }
        tommyAdapter.submitList(tommySuggestions) {
            Log.d("SuggestionActivity", "✅ Tommy adapter list submitted: ${tommySuggestions.size} items")
            // Force RecyclerView to remeasure to show all items
            binding.rcvTommy.requestLayout()
        }

        // Miley suggestions (category 1) - 10 items
        val mileySuggestions = suggestions.filter { it.categoryPosition == 1 }.take(10)
        Log.d("SuggestionActivity", "Miley filtered: ${mileySuggestions.size} items")
        if (!::mileyAdapter.isInitialized) {
            mileyAdapter = SuggestionAdapter(::openCustomizeWithSuggestion)
            binding.rcvMiley.adapter = mileyAdapter
            Log.d("SuggestionActivity", "Miley adapter initialized")
        }
        mileyAdapter.submitList(mileySuggestions) {
            Log.d("SuggestionActivity", "✅ Miley adapter list submitted: ${mileySuggestions.size} items")
            // Force RecyclerView to remeasure to show all items
            binding.rcvMiley.requestLayout()
        }

        // Dammy suggestions (category 2) - 10 items
        val dammySuggestions = suggestions.filter { it.categoryPosition == 2 }.take(10)
        Log.d("SuggestionActivity", "Dammy filtered: ${dammySuggestions.size} items")
        if (!::dammyAdapter.isInitialized) {
            dammyAdapter = SuggestionAdapter(::openCustomizeWithSuggestion)
            binding.rcvDammy.adapter = dammyAdapter
            Log.d("SuggestionActivity", "Dammy adapter initialized")
        }
        dammyAdapter.submitList(dammySuggestions) {
            Log.d("SuggestionActivity", "✅ Dammy adapter list submitted: ${dammySuggestions.size} items")
            // Force RecyclerView to remeasure to show all items
            binding.rcvDammy.requestLayout()
        }

        Log.d("SuggestionActivity", "========================================")
    }

    private fun updateThumbnails(thumbnails: Map<String, Bitmap>) {
        if (::tommyAdapter.isInitialized) {
            tommyAdapter.updateThumbnails(thumbnails)
        }
        if (::mileyAdapter.isInitialized) {
            mileyAdapter.updateThumbnails(thumbnails)
        }
        if (::dammyAdapter.isInitialized) {
            dammyAdapter.updateThumbnails(thumbnails)
        }
    }

    private fun openCustomizeWithSuggestion(suggestion: SuggestionModel) {
        if (suggestion.characterIndex == 1 || suggestion.characterIndex == 2) {
            if (!InternetHelper.checkInternet(this)) {
                showNoInternetDialog()
                return
            }
        }

        //quyen
        showInterAll {
            val intent = Intent(this, CustomizeActivity::class.java).apply {
                putExtra(IntentKey.CATEGORY_POSITION_KEY, suggestion.categoryPosition)
                putExtra(IntentKey.CHARACTER_INDEX, suggestion.characterIndex)
                putExtra(IntentKey.IS_SUGGESTION, true)
                putExtra(IntentKey.SUGGESTION_STATE, suggestion.randomState.toJson())
                putExtra(IntentKey.SUGGESTION_BACKGROUND, suggestion.background)
            }
            startActivity(intent)
        }
        //quyen
    }

    override fun initText() {}

    //quyen
    override fun initAds() {
        super.initAds()
        Admob.getInstance().loadNativeCollap(this, getString(R.string.native_cl_suggest), binding.nativeAds2)
        Admob.getInstance().loadNativeAd(this, getString(R.string.native_suggest), binding.nativeAds, R.layout.ads_native_avg)
    }

    override fun onRestart() {
        super.onRestart()
        Admob.getInstance().loadNativeCollap(this, getString(R.string.native_cl_suggest), binding.nativeAds2)
    }
    //quyen
}