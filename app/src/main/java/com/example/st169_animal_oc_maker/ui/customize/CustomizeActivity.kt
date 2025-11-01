package com.example.st169_animal_oc_maker.ui.customize

import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.st169_animal_oc_maker.R
import com.example.st169_animal_oc_maker.core.base.BaseActivity
import com.example.st169_animal_oc_maker.core.dialog.ConfirmDialog
import com.example.st169_animal_oc_maker.core.extensions.dLog
import com.example.st169_animal_oc_maker.core.extensions.eLog
import com.example.st169_animal_oc_maker.core.extensions.hideNavigation
import com.example.st169_animal_oc_maker.core.helper.InternetHelper
import com.example.st169_animal_oc_maker.core.extensions.invisible
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.core.extensions.showToast
import com.example.st169_animal_oc_maker.core.extensions.startIntent
import com.example.st169_animal_oc_maker.core.extensions.visible
import com.example.st169_animal_oc_maker.core.utils.SaveState
import com.example.st169_animal_oc_maker.core.utils.SystemUtils.setLocale
import com.example.st169_animal_oc_maker.core.utils.key.AssetsKey
import com.example.st169_animal_oc_maker.core.utils.key.IntentKey
import com.example.st169_animal_oc_maker.core.utils.key.ValueKey
import com.example.st169_animal_oc_maker.data.custom.ItemNavCustomModel
import com.example.st169_animal_oc_maker.databinding.ActivityCustomizeBinding
import com.example.st169_animal_oc_maker.ui.background.BackgroundActivity
import com.example.st169_animal_oc_maker.ui.home.DataViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.compareTo
import kotlin.jvm.java
import kotlin.text.get

class CustomizeActivity : BaseActivity<ActivityCustomizeBinding>() {
    private val viewModel: CustomizeViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    val colorLayerAdapter by lazy { ColorLayerAdapter(this) }
    val customizeLayerAdapter by lazy { CustomizeLayerAdapter(this) }
    val bottomNavigationAdapter by lazy { BottomNavigationAdapter(this) }

    // Thêm biến để lưu trạng thái color bar
    private var isColorBarVisible = true
    private var categoryPosition = 0
    private var isColorEnabled = true // Biến để lưu trạng thái enable/disable của rcvColor
    private var isSuggestion = false // Biến để lưu trạng thái mở từ suggestion

    override fun setViewBinding(): ActivityCustomizeBinding {
        return ActivityCustomizeBinding.inflate(LayoutInflater.from(this))
    }

    // Thêm vào CustomizeActivity.kt

    override fun initView() {
        initRcv()
        lifecycleScope.launch { showLoading() }
        dataViewModel.ensureData(this)

        // Check if opening from suggestion
        isSuggestion = intent.getBooleanExtra(IntentKey.IS_SUGGESTION, false)

        // Get character index - ưu tiên CHARACTER_INDEX nếu có (từ suggestion)
        val characterIndex = if (intent.hasExtra(IntentKey.CHARACTER_INDEX)) {
            intent.getIntExtra(IntentKey.CHARACTER_INDEX, 0)
        } else if (isSuggestion) {
            intent.getIntExtra(IntentKey.CATEGORY_POSITION_KEY, 0)
        } else {
            intent.getIntExtra(IntentKey.INTENT_KEY, 0)
        }

        // Lưu category position để dùng cho btnColor
        categoryPosition = characterIndex

        // Set category position cho adapter
        colorLayerAdapter.categoryPosition = categoryPosition

        // Set background based on category position
        val backgroundDrawable = when(categoryPosition) {
            0 -> R.drawable.bg_data1
            1 -> R.drawable.bg_data2
            2 -> R.drawable.bg_data3
            else -> R.drawable.img_bg_app
        }
        binding.main.setBackgroundResource(backgroundDrawable)


        // Set icon color tương ứng
        updateColorIcon()

        // Store suggestion data if exists
        if (isSuggestion) {
            val suggestionStateJson = intent.getStringExtra(IntentKey.SUGGESTION_STATE)
            val suggestionBackground = intent.getStringExtra(IntentKey.SUGGESTION_BACKGROUND)
            viewModel.setSuggestionPreset(suggestionStateJson, suggestionBackground)
        }
    }
    private fun updateColorIcon() {
        val iconRes = when(categoryPosition) {
            0 -> if (isColorBarVisible) R.drawable.ic_color_1_enable else R.drawable.ic_color_1_disable
            1 -> if (isColorBarVisible) R.drawable.ic_color_2_enable else R.drawable.ic_color_2_disable
            2 -> if (isColorBarVisible) R.drawable.ic_color_3_enable else R.drawable.ic_color_3_disable
            else -> if (isColorBarVisible) R.drawable.ic_color_1_enable else R.drawable.ic_color_1_disable
        }
        binding.btnColor.setImageResource(iconRes)
    }

    private fun toggleColorBar() {
        isColorBarVisible = !isColorBarVisible

        if (isColorBarVisible) {
            binding.layoutRcvColor.visible()
        } else {
            binding.layoutRcvColor.invisible()
        }

        updateColorIcon()
    }

    override fun dataObservable() {
        // allData
        lifecycleScope.launch {
            dataViewModel.allData.collect { list ->
                if (list.isNotEmpty()) {
                    // Dùng CHARACTER_INDEX nếu có, fallback về INTENT_KEY
                    viewModel.positionSelected = if (intent.hasExtra(IntentKey.CHARACTER_INDEX)) {
                        intent.getIntExtra(IntentKey.CHARACTER_INDEX, 0)
                    } else {
                        intent.getIntExtra(IntentKey.INTENT_KEY, 0)
                    }

                    // ✅ LOG: Character data khi load vào CustomizeActivity (disabled for performance)
                    // if (viewModel.positionSelected == 0) {
                    //     logCharacter0Data(list[viewModel.positionSelected], "CUSTOMIZE - dataObservable")
                    // }
                    // if (viewModel.positionSelected == 1) {
                    //     logMileyCharacterData(list[viewModel.positionSelected], "CUSTOMIZE - dataObservable")
                    // }
                    // if (viewModel.positionSelected == 2) {
                    //     logDammyCharacterData(list[viewModel.positionSelected], "CUSTOMIZE - dataObservable")
                    // }

                    viewModel.setDataCustomize(list[viewModel.positionSelected])
                    viewModel.setIsDataAPI(viewModel.positionSelected >= ValueKey.POSITION_API)
                    initData()
                }
            }
        }

        // isFlip
        lifecycleScope.launch {
            viewModel.isFlip.collect { status ->
                val rotation = if (status) -180f else 0f
                viewModel.imageViewList.value.forEachIndexed { index, view ->
                    view.rotationY = rotation
                }
            }
        }

        // bottomNavigationList
        lifecycleScope.launch {
            viewModel.bottomNavigationList.collect { bottomNavigationList ->
                if (bottomNavigationList.isNotEmpty()) {
                    bottomNavigationAdapter.submitList(bottomNavigationList)
                    customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
                    colorLayerAdapter.submitListWithLog(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])

                    // ✅ Optimized: Scroll to selected color if exists
                    binding.rcvColor.post {
                        if (viewModel.colorItemNavList.value[viewModel.positionNavSelected.value].isNotEmpty()) {
                            binding.rcvColor.smoothScrollToPosition(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value].indexOfFirst { it.isSelected })
                        }
                    }
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnBack.onSingleClick { confirmExit() }
                btnNext.onSingleClick { handleSave() }
            }
            btnRandom.onSingleClick {
                // Check internet for Miley (1) and Dammy (2)
                if (categoryPosition == 1 || categoryPosition == 2) {
                    if (!InternetHelper.checkInternet(this@CustomizeActivity)) {
                        showNoInternetDialog()
                        return@onSingleClick
                    }
                }

                if (viewModel.isDataAPI()) {
                    if (InternetHelper.checkInternet(this@CustomizeActivity)) {
                        handleRandomAllLayer()
                    } else {
                        showNoInternetDialog()
                    }
                } else {
                    handleRandomAllLayer()
                }
            }
            btnReset.onSingleClick { handleReset() }
            btnFlip.onSingleClick { viewModel.setIsFlip() }
            btnColor.onSingleClick { toggleColorBar() }

        }
        handleRcv()
    }

    override fun initText() {

    }

    /**
     * ✅ LOG: Log chi tiết toàn bộ data của Character 0
     */
    private fun logCharacter0Data(character: com.example.st169_animal_oc_maker.data.custom.CustomizeModel, source: String) {
        Log.d("CustomizeActivity", "========================================")
        Log.d("CustomizeActivity", "📊 CHARACTER 0 DATA - $source")
        Log.d("CustomizeActivity", "========================================")
        Log.d("CustomizeActivity", "Avatar: ${character.avatar}")
        Log.d("CustomizeActivity", "Total layers: ${character.layerList.size}")
        Log.d("CustomizeActivity", "")

        character.layerList.forEachIndexed { layerIndex, layer ->
            Log.d("CustomizeActivity", "--- Layer $layerIndex ---")
            Log.d("CustomizeActivity", "  positionCustom: ${layer.positionCustom}")
            Log.d("CustomizeActivity", "  positionNavigation: ${layer.positionNavigation}")
            Log.d("CustomizeActivity", "  imageNavigation: ${layer.imageNavigation}")
            Log.d("CustomizeActivity", "  Total items: ${layer.layer.size}")

            // Log chi tiết layer 0 (body)
            if (layerIndex == 0) {
                Log.d("CustomizeActivity", "  ⚠️ LAYER 0 (BODY) DETAILS:")
                layer.layer.forEachIndexed { itemIndex, item ->
                    Log.d("CustomizeActivity", "    Item $itemIndex:")
                    Log.d("CustomizeActivity", "      image: ${item.image}")
                    Log.d("CustomizeActivity", "      isMoreColors: ${item.isMoreColors}")
                    Log.d("CustomizeActivity", "      colors count: ${item.listColor.size}")
                    if (item.isMoreColors && item.listColor.isNotEmpty()) {
                        Log.d("CustomizeActivity", "      First 3 color paths:")
                        item.listColor.take(3).forEachIndexed { colorIndex, color ->
                            Log.d("CustomizeActivity", "        [$colorIndex] ${color.path}")
                        }
                        if (item.listColor.size > 3) {
                            Log.d("CustomizeActivity", "        ... and ${item.listColor.size - 3} more colors")
                        }
                    }
                }
            } else {
                // Log tóm tắt các layer khác
                if (layer.layer.isNotEmpty()) {
                    Log.d("CustomizeActivity", "  Sample item 0: ${layer.layer[0].image}")
                    Log.d("CustomizeActivity", "  Has colors: ${layer.layer[0].isMoreColors}")
                }
            }
            Log.d("CustomizeActivity", "")
        }
        Log.d("CustomizeActivity", "========================================")
    }

    /**
     * ✅ LOG: Log chi tiết toàn bộ data của Miley character
     */
    private fun logMileyCharacterData(character: com.example.st169_animal_oc_maker.data.custom.CustomizeModel, source: String) {
        Log.d("CustomizeActivity", "========================================")
        Log.d("CustomizeActivity", "📊 MILEY CHARACTER DATA - $source")
        Log.d("CustomizeActivity", "========================================")
        Log.d("CustomizeActivity", "Avatar: ${character.avatar}")
        Log.d("CustomizeActivity", "Total layers: ${character.layerList.size}")
        Log.d("CustomizeActivity", "")

        character.layerList.forEachIndexed { layerIndex, layer ->
            Log.d("CustomizeActivity", "--- Layer $layerIndex ---")
            Log.d("CustomizeActivity", "  positionCustom: ${layer.positionCustom}")
            Log.d("CustomizeActivity", "  positionNavigation: ${layer.positionNavigation}")
            Log.d("CustomizeActivity", "  imageNavigation: ${layer.imageNavigation}")
            Log.d("CustomizeActivity", "  Total items: ${layer.layer.size}")

            // Log chi tiết layer 0 (body)
            if (layerIndex == 0) {
                Log.d("CustomizeActivity", "  ⚠️ LAYER 0 (BODY) DETAILS:")
                layer.layer.forEachIndexed { itemIndex, item ->
                    Log.d("CustomizeActivity", "    Item $itemIndex:")
                    Log.d("CustomizeActivity", "      image: ${item.image}")
                    Log.d("CustomizeActivity", "      isMoreColors: ${item.isMoreColors}")
                    Log.d("CustomizeActivity", "      colors count: ${item.listColor.size}")
                    if (item.isMoreColors && item.listColor.isNotEmpty()) {
                        Log.d("CustomizeActivity", "      First 3 color paths:")
                        item.listColor.take(3).forEachIndexed { colorIndex, color ->
                            Log.d("CustomizeActivity", "        [$colorIndex] ${color.path}")
                        }
                        if (item.listColor.size > 3) {
                            Log.d("CustomizeActivity", "        ... and ${item.listColor.size - 3} more colors")
                        }
                    }
                }
            } else {
                // Log tóm tắt các layer khác
                if (layer.layer.isNotEmpty()) {
                    Log.d("CustomizeActivity", "  Sample item 0: ${layer.layer[0].image}")
                    Log.d("CustomizeActivity", "  Has colors: ${layer.layer[0].isMoreColors}")
                }
            }
            Log.d("CustomizeActivity", "")
        }
        Log.d("CustomizeActivity", "========================================")
    }

    /**
     * ✅ LOG: Log chi tiết toàn bộ data của Dammy character
     */
    private fun logDammyCharacterData(character: com.example.st169_animal_oc_maker.data.custom.CustomizeModel, source: String) {
        Log.d("CustomizeActivity", "========================================")
        Log.d("CustomizeActivity", "📊 DAMMY CHARACTER DATA - $source")
        Log.d("CustomizeActivity", "========================================")
        Log.d("CustomizeActivity", "Avatar: ${character.avatar}")
        Log.d("CustomizeActivity", "Total layers: ${character.layerList.size}")
        Log.d("CustomizeActivity", "")

        character.layerList.forEachIndexed { layerIndex, layer ->
            Log.d("CustomizeActivity", "--- Layer $layerIndex ---")
            Log.d("CustomizeActivity", "  positionCustom: ${layer.positionCustom}")
            Log.d("CustomizeActivity", "  positionNavigation: ${layer.positionNavigation}")
            Log.d("CustomizeActivity", "  imageNavigation: ${layer.imageNavigation}")
            Log.d("CustomizeActivity", "  Total items: ${layer.layer.size}")

            // Log chi tiết layer 0 (body)
            if (layerIndex == 0) {
                Log.d("CustomizeActivity", "  ⚠️ LAYER 0 (BODY) DETAILS:")
                layer.layer.forEachIndexed { itemIndex, item ->
                    Log.d("CustomizeActivity", "    Item $itemIndex:")
                    Log.d("CustomizeActivity", "      image: ${item.image}")
                    Log.d("CustomizeActivity", "      isMoreColors: ${item.isMoreColors}")
                    Log.d("CustomizeActivity", "      colors count: ${item.listColor.size}")
                    if (item.isMoreColors && item.listColor.isNotEmpty()) {
                        Log.d("CustomizeActivity", "      First 3 color paths:")
                        item.listColor.take(3).forEachIndexed { colorIndex, color ->
                            Log.d("CustomizeActivity", "        [$colorIndex] ${color.path}")
                        }
                        if (item.listColor.size > 3) {
                            Log.d("CustomizeActivity", "        ... and ${item.listColor.size - 3} more colors")
                        }
                    }
                }
            } else {
                // Log tóm tắt các layer khác
                if (layer.layer.isNotEmpty()) {
                    Log.d("CustomizeActivity", "  Sample item 0: ${layer.layer[0].image}")
                    Log.d("CustomizeActivity", "  Has colors: ${layer.layer[0].isMoreColors}")
                }
            }
            Log.d("CustomizeActivity", "")
        }
        Log.d("CustomizeActivity", "========================================")
    }
    private fun initRcv() {
        binding.apply {
            rcvLayer.apply {
                adapter = customizeLayerAdapter
                itemAnimator = null
                // ✅ PERFORMANCE: Enable fixed size for better recycling
                setHasFixedSize(true)
                // ✅ PERFORMANCE: Increase view cache size to reduce re-binding
                setItemViewCacheSize(20)
                // ✅ PERFORMANCE: Enable nested scrolling optimization
                isNestedScrollingEnabled = false
            }

            rcvColor.apply {
                // ✅ FIX: Ensure LinearLayoutManager is set with horizontal orientation for Android 8
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
                    this@CustomizeActivity,
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                    false
                )
                adapter = colorLayerAdapter
                itemAnimator = null
                // ✅ PERFORMANCE: Enable fixed size and increase cache
                setHasFixedSize(true)
                setItemViewCacheSize(10)
                isNestedScrollingEnabled = false
                dLog("🔧 rcvColor initialized: layoutManager=${layoutManager}, adapter=${adapter}")
            }

            rcvNavigation.apply {
                adapter = bottomNavigationAdapter
                itemAnimator = null
                //  PERFORMANCE: Enable fixed size for better recycling
                setHasFixedSize(true)
                //  PERFORMANCE: Small cache for bottom navigation (limited items)
                setItemViewCacheSize(8)
                isNestedScrollingEnabled = false
            }
        }
    }

    private fun handleRcv() {
        customizeLayerAdapter.onItemClick = onItemClick@{ item, position ->
            // Check internet for Miley (1) and Dammy (2)
            if (categoryPosition == 1 || categoryPosition == 2) {
                if (!InternetHelper.checkInternet(this)) {
                    showNoInternetDialog()
                    return@onItemClick
                }
            }

            if (viewModel.isDataAPI()) {
                if (InternetHelper.checkInternet(this)) {
                    handleFillLayer(item, position)
                } else {
                    showNoInternetDialog()
                }
            } else {
                handleFillLayer(item, position)
            }
        }

        customizeLayerAdapter.onNoneClick = onNoneClick@{ position ->
            // Check internet for Miley (1) and Dammy (2)
            if (categoryPosition == 1 || categoryPosition == 2) {
                if (!InternetHelper.checkInternet(this)) {
                    showNoInternetDialog()
                    return@onNoneClick
                }
            }

            if (viewModel.isDataAPI()) {
                if (InternetHelper.checkInternet(this)) {
                    handleNoneLayer(position)
                } else {
                    showNoInternetDialog()
                }
            } else {
                handleNoneLayer(position)
            }
        }

        customizeLayerAdapter.onRandomClick = onRandomClick@{
            // Check internet for Miley (1) and Dammy (2)
            if (categoryPosition == 1 || categoryPosition == 2) {
                if (!InternetHelper.checkInternet(this)) {
                    showNoInternetDialog()
                    return@onRandomClick
                }
            }

            if (viewModel.isDataAPI()) {
                if (InternetHelper.checkInternet(this)) {
                    handleRandomLayer()
                } else {
                    showNoInternetDialog()
                }
            } else {
                handleRandomLayer()
            }
        }

        colorLayerAdapter.onItemClick = colorItemClick@{ position ->
            // Check internet for Miley (1) and Dammy (2)
            if (categoryPosition == 1 || categoryPosition == 2) {
                if (!InternetHelper.checkInternet(this)) {
                    showNoInternetDialog()
                    return@colorItemClick
                }
            }

            if (viewModel.isDataAPI()) {
                if (InternetHelper.checkInternet(this)) {
                    handleChangeColorLayer(position)
                } else {
                    showNoInternetDialog()
                }
            } else {
                handleChangeColorLayer(position)
            }
        }

        bottomNavigationAdapter.onItemClick = navItemClick@{ positionBottomNavigation ->
            // Check internet for Miley (1) and Dammy (2)
            if (categoryPosition == 1 || categoryPosition == 2) {
                if (!InternetHelper.checkInternet(this)) {
                    showNoInternetDialog()
                    return@navItemClick
                }
            }

            if (viewModel.isDataAPI()) {
                if (InternetHelper.checkInternet(this)) {
                    handleClickBottomNavigation(positionBottomNavigation)
                } else {
                    showNoInternetDialog()
                }
            } else {
                handleClickBottomNavigation(positionBottomNavigation)
            }
        }
    }

// Update initData() trong CustomizeActivity.kt

    private fun initData(){
        val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
            eLog("initData: ${throwable.message}")
            CoroutineScope(Dispatchers.Main).launch {
                dismissLoading(true)
                val dialogExit = ConfirmDialog(this@CustomizeActivity, R.string.error, R.string.an_error_occurred)
                dialogExit.show()
                dialogExit.onNoClick = {
                    dialogExit.dismiss()
                    finish()
                }
                dialogExit.onYesClick = {
                    dialogExit.dismiss()
                    hideNavigation(true)
                    startIntent(CustomizeActivity::class.java, viewModel.positionSelected)
                    finish()
                }
            }
        }

        CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {
            var pathImageDefault = ""

            // ✅ LOG: Log suggestion preset nếu có (disabled for performance)
            // if (viewModel.positionSelected == 0 && viewModel.hasSuggestionPreset()) {
            //     Log.d("CustomizeActivity", "========================================")
            //     Log.d("CustomizeActivity", "📊 CHARACTER 0 - SUGGESTION PRESET DATA")
            //     Log.d("CustomizeActivity", "========================================")
            //     Log.d("CustomizeActivity", "isSuggestion: $isSuggestion")
            //     Log.d("CustomizeActivity", "categoryPosition: $categoryPosition")
            //     val suggestionStateJson = intent.getStringExtra(IntentKey.SUGGESTION_STATE)
            //     val suggestionBackground = intent.getStringExtra(IntentKey.SUGGESTION_BACKGROUND)
            //     Log.d("CustomizeActivity", "suggestionStateJson: $suggestionStateJson")
            //     Log.d("CustomizeActivity", "suggestionBackground: $suggestionBackground")
            //     Log.d("CustomizeActivity", "========================================")
            // }
            // if (viewModel.positionSelected == 1 && viewModel.hasSuggestionPreset()) {
            //     Log.d("CustomizeActivity", "========================================")
            //     Log.d("CustomizeActivity", "📊 MILEY - SUGGESTION PRESET DATA")
            //     Log.d("CustomizeActivity", "========================================")
            //     Log.d("CustomizeActivity", "isSuggestion: $isSuggestion")
            //     Log.d("CustomizeActivity", "categoryPosition: $categoryPosition")
            //     val suggestionStateJson = intent.getStringExtra(IntentKey.SUGGESTION_STATE)
            //     val suggestionBackground = intent.getStringExtra(IntentKey.SUGGESTION_BACKGROUND)
            //     Log.d("CustomizeActivity", "suggestionStateJson: $suggestionStateJson")
            //     Log.d("CustomizeActivity", "suggestionBackground: $suggestionBackground")
            //     Log.d("CustomizeActivity", "========================================")
            // }
            // if (viewModel.positionSelected == 2 && viewModel.hasSuggestionPreset()) {
            //     Log.d("CustomizeActivity", "========================================")
            //     Log.d("CustomizeActivity", "📊 DAMMY - SUGGESTION PRESET DATA")
            //     Log.d("CustomizeActivity", "========================================")
            //     Log.d("CustomizeActivity", "isSuggestion: $isSuggestion")
            //     Log.d("CustomizeActivity", "categoryPosition: $categoryPosition")
            //     val suggestionStateJson = intent.getStringExtra(IntentKey.SUGGESTION_STATE)
            //     val suggestionBackground = intent.getStringExtra(IntentKey.SUGGESTION_BACKGROUND)
            //     Log.d("CustomizeActivity", "suggestionStateJson: $suggestionStateJson")
            //     Log.d("CustomizeActivity", "suggestionBackground: $suggestionBackground")
            //     Log.d("CustomizeActivity", "========================================")
            // }

            // Get data from list
            val deferred1 = async {
                viewModel.resetDataList()
                viewModel.addValueToItemNavList()
                viewModel.setItemColorDefault()

                // ✅ CRITICAL FIX: Apply suggestion preset BEFORE setFocusItemNavDefault
                // If we have a preset, use it. Otherwise, set defaults.
                if (viewModel.hasSuggestionPreset()) {
                    // Apply preset selections (includes positionCustom, positionNav, etc.)
                    viewModel.applySuggestionPreset()

                    // ✅ LOG: Log sau khi apply preset (disabled for performance)
                    // if (viewModel.positionSelected == 0) {
                    //     Log.d("CustomizeActivity", "✅ CHARACTER 0 - After applySuggestionPreset()")
                    //     Log.d("CustomizeActivity", "pathSelectedList: ${viewModel.pathSelectedList.value}")
                    //     Log.d("CustomizeActivity", "positionCustom: ${viewModel.positionCustom.value}")
                    //     Log.d("CustomizeActivity", "positionNavSelected: ${viewModel.positionNavSelected.value}")
                    // }
                    // if (viewModel.positionSelected == 1) {
                    //     Log.d("CustomizeActivity", "✅ MILEY - After applySuggestionPreset()")
                    //     Log.d("CustomizeActivity", "pathSelectedList: ${viewModel.pathSelectedList.value}")
                    //     Log.d("CustomizeActivity", "positionCustom: ${viewModel.positionCustom.value}")
                    //     Log.d("CustomizeActivity", "positionNavSelected: ${viewModel.positionNavSelected.value}")
                    // }
                    // if (viewModel.positionSelected == 2) {
                    //     Log.d("CustomizeActivity", "✅ DAMMY - After applySuggestionPreset()")
                    //     Log.d("CustomizeActivity", "pathSelectedList: ${viewModel.pathSelectedList.value}")
                    //     Log.d("CustomizeActivity", "positionCustom: ${viewModel.positionCustom.value}")
                    //     Log.d("CustomizeActivity", "positionNavSelected: ${viewModel.positionNavSelected.value}")
                    // }
                } else {
                    // No preset: set defaults
                    viewModel.setFocusItemNavDefault()
                    viewModel.setPositionCustom(viewModel.dataCustomize.value!!.layerList.first().positionCustom)
                    viewModel.setPositionNavSelected(viewModel.dataCustomize.value!!.layerList.first().positionNavigation)
                }

                viewModel.setBottomNavigationListDefault()


                dLog("deferred1")
                return@async true
            }

            // Add custom view in FrameLayout
            val deferred2 = async(Dispatchers.Main) {
                if (deferred1.await()) {
                    viewModel.setImageViewList(binding.layoutCustomLayer)
                    dLog("deferred3")
                }
                return@async true
            }

            // Fill data default or preset
            val deferred3 = async {
                if (deferred1.await() && deferred2.await()) {
                    // ✅ Load images from preset paths instead of default
                    if (viewModel.hasSuggestionPreset()) {
                        // All paths already set in applySuggestionPreset()
                        pathImageDefault = ""  // Not needed for preset
                    } else {
                        // ✅ FIX: Chỉ load ảnh mặc định cho tab 0 (body)
                        // Các tab khác không load ảnh (để NONE)
                        if (viewModel.positionNavSelected.value == 0) {
                            // Tab 0 (body): load ảnh đầu tiên
                            pathImageDefault = viewModel.dataCustomize.value!!.layerList.first().layer.first().image
                            viewModel.setIsSelectedItem(viewModel.positionCustom.value)

                            // ✅ FIX: Body layer lưu vào index 0
                            viewModel.setPathSelected(0, pathImageDefault)
                            viewModel.setKeySelected(viewModel.positionNavSelected.value, pathImageDefault)
                        } else {
                            // Các tab khác: giữ nguyên NONE (không load ảnh)
                            pathImageDefault = ""
                        }
                    }
                    dLog("deferred5")
                }
                return@async true
            }

            withContext(Dispatchers.Main){
                if (deferred1.await() && deferred2.await() && deferred3.await()){
                    // ✅ FIX: Render tất cả layers thay vì load từng ảnh
                    renderAllLayers()

                    customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])

                    // ✅ DEBUG: Log trước khi submit color list
                    val colorList = viewModel.colorItemNavList.value[viewModel.positionNavSelected.value]
                    dLog("🎨 [initData] Submitting color list: size=${colorList.size}")

                    colorLayerAdapter.submitListWithLog(colorList)

                    // ✅ Optimized: Single layout pass only for Android 8
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
                        binding.rcvColor.post {
                            binding.rcvColor.requestLayout()
                        }
                    }

                    // ✅ Scroll to selected item if has suggestion preset
                    if (viewModel.hasSuggestionPreset()) {
                        val selectedIndex = viewModel.itemNavList.value[viewModel.positionNavSelected.value]
                            .indexOfFirst { it.isSelected }
                        if (selectedIndex >= 0) {
                            binding.rcvLayer.scrollToPosition(selectedIndex)
                        }

                        // Scroll to selected color if exists
                        if (viewModel.colorItemNavList.value[viewModel.positionNavSelected.value].isNotEmpty()) {
                            val selectedColorIndex = viewModel.colorItemNavList.value[viewModel.positionNavSelected.value]
                                .indexOfFirst { it.isSelected }
                            if (selectedColorIndex >= 0) {
                                binding.rcvColor.scrollToPosition(selectedColorIndex)
                            }
                        }
                    }

                    // ✅ SỬA: Gọi checkStatusColor() TRƯỚC để xử lý logic hiển thị color bar
                    checkStatusColor()

                    // ✅ SỬA: SAU ĐÓ mới check enable/disable rcvColor dựa trên item selected
                    val selectedItem = viewModel.itemNavList.value[viewModel.positionNavSelected.value]
                        .firstOrNull { it.isSelected }

                    // Chỉ disable nếu là NONE_LAYER hoặc path rỗng
                    // ✅ AUTO-DETECT: Tự động chọn pathIndex phù hợp với data structure
                    val pathIndex = viewModel.getPathIndexForLayer(viewModel.positionNavSelected.value)
                    if (selectedItem?.path == AssetsKey.NONE_LAYER ||
                        viewModel.pathSelectedList.value[pathIndex].isNullOrEmpty()) {
                        setColorRecyclerViewEnabled(false)
                    } else {
                        // Enable nếu có path và không phải NONE
                        setColorRecyclerViewEnabled(true)
                    }

                    dismissLoading()
                    dLog("main")

                    // ✅ WORKAROUND: Auto-trigger lại layer 0 nếu:
                    // 1. Mở từ CHARACTER_INDEX = 1 (category Miley)
                    // 2. HOẶC mở từ suggestion
                    // 3. VÀ đang ở tab 0 (positionNavSelected == 0)
                    if ((categoryPosition == 1 || (isSuggestion && viewModel.hasSuggestionPreset()))
                        && viewModel.positionNavSelected.value == 0) {
                        // Delay ngắn để đảm bảo UI đã render xong
                        binding.rcvLayer.postDelayed({
                            val selectedItemPosition = viewModel.itemNavList.value[0].indexOfFirst { it.isSelected }
                            if (selectedItemPosition >= 0) {
                                val selectedItem = viewModel.itemNavList.value[0][selectedItemPosition]
                                // Trigger handleFillLayer để reload ảnh
                                if (selectedItem.path != AssetsKey.RANDOM_LAYER && selectedItem.path != AssetsKey.NONE_LAYER) {
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        val pathSelected = viewModel.setClickFillLayer(selectedItem, selectedItemPosition)
                                        withContext(Dispatchers.Main) {
                                            // ✅ FIX: Render lại tất cả layers
                                            renderAllLayers()
                                            dLog("🔧 WORKAROUND: Re-triggered layer 0 for category $categoryPosition")
                                        }
                                    }
                                }
                            }
                        }, 100) // 100ms delay để đảm bảo UI đã render
                    }
                }
            }

        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
            // Reduced delay to 150ms for better performance on Android 8
            binding.root.postDelayed({
                // Force refresh layout
                binding.layoutCustomLayer.requestLayout()
                binding.rcvColor.requestLayout()
            }, 150)
        }
    }
    private fun checkStatusColor() {
        val colorListSize = viewModel.colorItemNavList.value[viewModel.positionNavSelected.value].size
        val shouldShowColor = viewModel.isShowColorList.value[viewModel.positionNavSelected.value]

        dLog("🎨 checkStatusColor: positionNav=${viewModel.positionNavSelected.value}")
        dLog("🎨 colorListSize=$colorListSize, shouldShowColor=$shouldShowColor")

        if (viewModel.colorItemNavList.value[viewModel.positionNavSelected.value].isNotEmpty()) {
            // Có màu -> hiện btnColor
            binding.btnColor.visible()
            dLog("🎨 Has colors -> show btnColor")

            if (viewModel.isShowColorList.value[viewModel.positionNavSelected.value]) {
                // Mặc định hiển thị khi vào màn hình
                isColorBarVisible = true
                binding.layoutRcvColor.visible()
                dLog("🎨 Should show colors -> layoutRcvColor VISIBLE")
            } else {
                isColorBarVisible = false
                binding.layoutRcvColor.invisible()
                dLog("🎨 Should NOT show colors -> layoutRcvColor INVISIBLE")
            }
        } else {
            // Không có màu -> ẩn cả layoutRcvColor và btnColor
            isColorBarVisible = false
            binding.layoutRcvColor.invisible()
            binding.btnColor.invisible()
            dLog("🎨 NO colors -> hide layoutRcvColor and btnColor")
        }
        updateColorIcon()

        // ✅ XÓA đoạn này - để logic enable/disable ở initData() xử lý
        // Vì checkStatusColor() chỉ nên quan tâm đến việc hiển thị UI, không nên can thiệp vào enable state
    }


    private fun handleFillLayer(item: ItemNavCustomModel, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            // ✅ LOG: Log khi click vào item (disabled for performance)
            // if (categoryPosition == 0) {
            //     Log.d("CustomizeActivity", "========================================")
            //     Log.d("CustomizeActivity", "🖱️ CHARACTER 0 - handleFillLayer CLICKED")
            //     Log.d("CustomizeActivity", "========================================")
            //     Log.d("CustomizeActivity", "Item position: $position")
            //     Log.d("CustomizeActivity", "Item path: ${item.path}")
            //     Log.d("CustomizeActivity", "Item isSelected: ${item.isSelected}")
            //     Log.d("CustomizeActivity", "Item colors count: ${item.listImageColor.size}")
            //     Log.d("CustomizeActivity", "positionCustom: ${viewModel.positionCustom.value}")
            //     Log.d("CustomizeActivity", "positionNavSelected: ${viewModel.positionNavSelected.value}")
            // }
            // if (categoryPosition == 1) {
            //     Log.d("CustomizeActivity", "========================================")
            //     Log.d("CustomizeActivity", "🖱️ MILEY - handleFillLayer CLICKED")
            //     Log.d("CustomizeActivity", "========================================")
            //     Log.d("CustomizeActivity", "Item position: $position")
            //     Log.d("CustomizeActivity", "Item path: ${item.path}")
            //     Log.d("CustomizeActivity", "Item isSelected: ${item.isSelected}")
            //     Log.d("CustomizeActivity", "Item colors count: ${item.listImageColor.size}")
            //     Log.d("CustomizeActivity", "positionCustom: ${viewModel.positionCustom.value}")
            //     Log.d("CustomizeActivity", "positionNavSelected: ${viewModel.positionNavSelected.value}")
            // }
            // if (categoryPosition == 2) {
            //     Log.d("CustomizeActivity", "========================================")
            //     Log.d("CustomizeActivity", "🖱️ DAMMY - handleFillLayer CLICKED")
            //     Log.d("CustomizeActivity", "========================================")
            //     Log.d("CustomizeActivity", "Item position: $position")
            //     Log.d("CustomizeActivity", "Item path: ${item.path}")
            //     Log.d("CustomizeActivity", "Item isSelected: ${item.isSelected}")

            Log.d("CustomizeActivity", "")
            Log.d("CustomizeActivity", "👆 USER CLICKED ITEM")
            Log.d("CustomizeActivity", "Item: ${item.path.substringAfterLast("/")}")
            Log.d("CustomizeActivity", "Position in RCV: $position")
            Log.d("CustomizeActivity", "Item colors count: ${item.listImageColor.size}")
            Log.d("CustomizeActivity", "positionCustom: ${viewModel.positionCustom.value}")
            Log.d("CustomizeActivity", "positionNavSelected: ${viewModel.positionNavSelected.value}")

            val pathSelected = viewModel.setClickFillLayer(item, position)

            Log.d("CustomizeActivity", "✅ pathSelected: ${pathSelected.substringAfterLast("/")}")
            Log.d("CustomizeActivity", "pathSelectedList after click:")
            viewModel.pathSelectedList.value.forEachIndexed { idx, path ->
                if (path.isNotEmpty()) {
                    Log.d("CustomizeActivity", "  [$idx] = ${path.substringAfterLast("/")}")
                } else {
                    Log.d("CustomizeActivity", "  [$idx] = EMPTY")
                }
            }

            withContext(Dispatchers.Main) {
                // ✅ FIX: Render lại TẤT CẢ layers, không chỉ layer vừa click
                // Vì Body và Ears cùng ImageView[1], cần load cả 2
                renderAllLayers()

                customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])

                // ✅ FIX: Dùng colorItemNavList từ ViewModel (đã được cập nhật đúng màu trong setClickFillLayer)
                colorLayerAdapter.submitListWithLog(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])

                // Scroll to selected color if needed
                val selectedColorIndex = viewModel.colorItemNavList.value[viewModel.positionNavSelected.value]
                    .indexOfFirst { it.isSelected }
                if (selectedColorIndex >= 0) {
                    binding.rcvColor.post {
                        binding.rcvColor.smoothScrollToPosition(selectedColorIndex)
                    }
                }

                // Enable lại rcvColor khi chọn item khác (không phải btnNone)
                setColorRecyclerViewEnabled(true)
            }
        }
    }

    /**
     * ✅ Render tất cả layers theo đúng thứ tự
     * Body dùng ImageView riêng, các layer khác dùng imageViewList
     */
    private fun renderAllLayers() {
        Log.d("CustomizeActivity", "════════════════════════════════════════")
        Log.d("CustomizeActivity", "🎨 RENDER ALL LAYERS START")
        Log.d("CustomizeActivity", "════════════════════════════════════════")

        // 🔧 HARDFIX Character 1: Render Layer[24] vào Layer24ImageView riêng (z-index 0 - dưới cùng)
        if (categoryPosition == 1) {
            val layer24 = viewModel.dataCustomize.value?.layerList?.getOrNull(24)
            if (layer24 != null && layer24.positionNavigation == 24) {
                val path24 = viewModel.pathSelectedList.value.getOrNull(24)
                val layer24ImageView = viewModel.layer24ImageView.value

                if (!path24.isNullOrEmpty() && layer24ImageView != null) {
                    Log.d("CustomizeActivity", "🔧 HARDFIX Miley: Render Layer[24] to Layer24ImageView (z-index 0)")
                    Glide.with(this@CustomizeActivity)
                        .load(path24)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                        .skipMemoryCache(false)
                        .into(layer24ImageView)
                } else if (layer24ImageView != null) {
                    // Clear Layer24ImageView nếu path rỗng
                    Log.d("CustomizeActivity", "🔧 HARDFIX Miley: Clear Layer24ImageView")
                    Glide.with(this@CustomizeActivity).clear(layer24ImageView)
                }
            }
        }

        viewModel.dataCustomize.value?.layerList?.forEachIndexed { index, layerListModel ->
            val pathIndex = index
            val path = viewModel.pathSelectedList.value.getOrNull(pathIndex)

            // 🔍 LOG: Chi tiết từng layer
            Log.d("CustomizeActivity", "Layer[$index]: posNav=${layerListModel.positionNavigation}, posCus=${layerListModel.positionCustom}, path=${if(path.isNullOrEmpty()) "EMPTY" else path.substringAfterLast("/")}")

            if (index == 0) {
                // ✅ FIX: Body layer → Dùng ImageView riêng
                if (!path.isNullOrEmpty()) {
                    Log.d("CustomizeActivity", "  → RENDER to BODY ImageView")

                    // 🔍 DEBUG: Chi tiết body layer
                    val bodyImageView = viewModel.bodyImageView.value
                    Log.d("CustomizeActivity", "     ├─ Path: $path")
                    Log.d("CustomizeActivity", "     ├─ BodyImageView: $bodyImageView")
                    Log.d("CustomizeActivity", "     ├─ BodyImageView ID: ${bodyImageView?.id}")
                    Log.d("CustomizeActivity", "     ├─ BodyImageView Visibility: ${bodyImageView?.visibility}")
                    Log.d("CustomizeActivity", "     ├─ BodyImageView Alpha: ${bodyImageView?.alpha}")
                    Log.d("CustomizeActivity", "     ├─ BodyImageView Size: ${bodyImageView?.width}x${bodyImageView?.height}")

                    // Kiểm tra file tồn tại trong assets
                    val fileExists = try {
                        assets.open(path).use { true }
                    } catch (e: Exception) {
                        Log.e("CustomizeActivity", "     └─ ✗ File NOT found in assets: ${e.message}")
                        false
                    }
                    Log.d("CustomizeActivity", "     ├─ File exists in assets: $fileExists")

                    bodyImageView?.let { imgView ->
                        Glide.with(this@CustomizeActivity)
                            .load(path)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                            .skipMemoryCache(false)
                            .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                                override fun onLoadFailed(
                                    e: com.bumptech.glide.load.engine.GlideException?,
                                    model: Any?,
                                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    Log.e("CustomizeActivity", "     └─ ✗ BODY GLIDE LOAD FAILED: ${e?.message}")
                                    e?.logRootCauses("CustomizeActivity")
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: android.graphics.drawable.Drawable,
                                    model: Any,
                                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                                    dataSource: com.bumptech.glide.load.DataSource,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    Log.d("CustomizeActivity", "     └─ ✓ BODY GLIDE SUCCESS: ${resource.intrinsicWidth}x${resource.intrinsicHeight}")
                                    return false
                                }
                            })
                            .into(imgView)
                    } ?: Log.e("CustomizeActivity", "     └─ ✗ BodyImageView is NULL")
                } else {
                    Log.d("CustomizeActivity", "  → CLEAR BODY ImageView")
                    // Clear body ImageView nếu rỗng
                    viewModel.bodyImageView.value?.let { bodyImageView ->
                        Glide.with(this@CustomizeActivity).clear(bodyImageView)
                    }
                }
            } else {
                // 🔧 HARDFIX Character 1: Skip Layer[24] vì đã render riêng
                if (categoryPosition == 1 && index == 24) {
                    Log.d("CustomizeActivity", "  → SKIP Layer[24] (already rendered to Layer24ImageView)")
                    return@forEachIndexed
                }

                // ✅ Các layer khác → Dùng imageViewList theo positionCustom
                if (!path.isNullOrEmpty()) {
                    Log.d("CustomizeActivity", "  → RENDER to ImageView[${layerListModel.positionCustom}]")

                    // 🔍 DEBUG: Chi tiết load ảnh
                    val imageView = viewModel.imageViewList.value.getOrNull(layerListModel.positionCustom)
                    Log.d("CustomizeActivity", "     ├─ Path: $path")
                    Log.d("CustomizeActivity", "     ├─ ImageView: $imageView")
                    Log.d("CustomizeActivity", "     ├─ ImageView ID: ${imageView?.id}")
                    Log.d("CustomizeActivity", "     ├─ ImageView Visibility: ${imageView?.visibility}")
                    Log.d("CustomizeActivity", "     ├─ ImageView Alpha: ${imageView?.alpha}")
                    Log.d("CustomizeActivity", "     ├─ ImageView Size: ${imageView?.width}x${imageView?.height}")
                    Log.d("CustomizeActivity", "     ├─ ImageView Parent: ${imageView?.parent}")

                    // Kiểm tra file tồn tại trong assets
                    val fileExists = try {
                        assets.open(path).use { true }
                    } catch (e: Exception) {
                        Log.e("CustomizeActivity", "     └─ ✗ File NOT found in assets: ${e.message}")
                        false
                    }
                    Log.d("CustomizeActivity", "     ├─ File exists in assets: $fileExists")

                    if (imageView != null) {
                        Glide.with(this@CustomizeActivity)
                            .load(path)
                            .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.ALL)
                            .skipMemoryCache(false)
                            .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                                override fun onLoadFailed(
                                    e: com.bumptech.glide.load.engine.GlideException?,
                                    model: Any?,
                                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    Log.e("CustomizeActivity", "     └─ ✗ GLIDE LOAD FAILED: ${e?.message}")
                                    e?.logRootCauses("CustomizeActivity")
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: android.graphics.drawable.Drawable,
                                    model: Any,
                                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                                    dataSource: com.bumptech.glide.load.DataSource,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    Log.d("CustomizeActivity", "     └─ ✓ GLIDE SUCCESS: ${resource.intrinsicWidth}x${resource.intrinsicHeight}")
                                    return false
                                }
                            })
                            .into(imageView)
                    } else {
                        Log.e("CustomizeActivity", "     └─ ✗ ImageView is NULL at position ${layerListModel.positionCustom}")
                    }
                } else {
                    Log.d("CustomizeActivity", "  → CLEAR ImageView[${layerListModel.positionCustom}]")
                    // Clear nếu path rỗng
                    Glide.with(this@CustomizeActivity)
                        .clear(viewModel.imageViewList.value[layerListModel.positionCustom])
                }
            }
        }

        Log.d("CustomizeActivity", "════════════════════════════════════════")
        Log.d("CustomizeActivity", "🎨 RENDER ALL LAYERS END")
        Log.d("CustomizeActivity", "════════════════════════════════════════")
    }

    private fun handleNoneLayer(position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            // ✅ AUTO-DETECT: Tự động chọn pathIndex phù hợp với data structure
            val pathIndex = viewModel.getPathIndexForLayer(viewModel.positionNavSelected.value)

            // 🎯 FIX: Xử lý pathIndex với mapping fix
            if (pathIndex == -1) {
                Log.e("CustomizeActivity", "❌ Cannot clear layer: positionNav=${viewModel.positionNavSelected.value} not found")
                withContext(Dispatchers.Main) {
                    // Vẫn update UI để user thấy None được chọn
                    viewModel.setItemNavList(viewModel.positionNavSelected.value, position)
                    customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
                    // ✅ FIX: Vẫn enable color picker để user có thể scroll và select
                }
                return@launch
            } else {
                Log.d("CustomizeActivity", "✅ NONE: Clear pathIndex=$pathIndex")
            }

            viewModel.setIsSelectedItem(viewModel.positionCustom.value)
            viewModel.setPathSelected(pathIndex, "")
            viewModel.setKeySelected(viewModel.positionNavSelected.value, "")
            viewModel.setItemNavList(viewModel.positionNavSelected.value, position)
            withContext(Dispatchers.Main) {
                // ✅ FIX: Render lại tất cả layers thay vì chỉ clear 1 ImageView
                renderAllLayers()
                customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
                // ✅ FIX: Vẫn enable color picker để user có thể scroll và select màu
                // Màu sẽ được apply khi user click vào màu (sẽ tự động chuyển từ None sang item có màu)
            }
        }
    }

    private fun handleRandomLayer() {
        lifecycleScope.launch(Dispatchers.IO) {
            val (pathRandom, isMoreColors) = viewModel.setClickRandomLayer()
            withContext(Dispatchers.Main) {
                //  FIX: Render lại tất cả layers thay vì chỉ load 1 ảnh
                renderAllLayers()
                customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
                if (isMoreColors) {
                    colorLayerAdapter.submitListWithLog(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])
                    binding.rcvColor.post {
                        binding.rcvColor.smoothScrollToPosition(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value].indexOfFirst { it.isSelected })
                    }
                }
                // Enable lại rcvColor khi click random
                setColorRecyclerViewEnabled(true)
            }
        }
    }

    private fun handleChangeColorLayer(position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            // ✅ FIX: Kiểm tra nếu đang ở trạng thái None
            val currentSelectedItem = viewModel.itemNavList.value[viewModel.positionNavSelected.value]
                .firstOrNull { it.isSelected }

            if (currentSelectedItem?.path == AssetsKey.NONE_LAYER) {
                // ✅ CHỈ lưu positionColorItemList, KHÔNG update colorItemNavList
                // Vì đang ở None, colorItemNavList có thể chứa màu của item cũ (không chính xác)
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.setPositionColorForLayer(viewModel.positionNavSelected.value, position)
                    withContext(Dispatchers.Main) {
                        // Update UI để hiển thị màu được chọn
                        viewModel.setColorItemNav(viewModel.positionNavSelected.value, position)
                        colorLayerAdapter.submitListWithLog(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])
                        Log.d("CustomizeActivity", "🎨 Color selected in None mode (position=$position) - Will apply when item selected")
                    }
                }
                return@launch
            }

            // ✅ Nếu KHÔNG phải None, apply màu bình thường
            val pathColor = viewModel.setClickChangeColor(position)
            withContext(Dispatchers.Main) {
                // ✅ FIX: Render lại tất cả layers
                renderAllLayers()
                customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
                colorLayerAdapter.submitListWithLog(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])
            }
        }
    }

    private fun setColorRecyclerViewEnabled(enabled: Boolean) {
        isColorEnabled = enabled
        // ✅ FIX: Luôn giữ alpha = 1.0f và cho phép tương tác với color picker
        binding.rcvColor.alpha = 1.0f
        // ✅ FIX: Luôn enable adapter để cho phép scroll và select
        colorLayerAdapter.isEnabled = true

        // ✅ DEBUG: Log chi tiết về rcvColor
        dLog("🎨 setColorRecyclerViewEnabled: enabled=$enabled")
        dLog("🎨 rcvColor visibility: ${binding.rcvColor.visibility}")
        dLog("🎨 rcvColor alpha: ${binding.rcvColor.alpha}")
        dLog("🎨 rcvColor width: ${binding.rcvColor.width}, height: ${binding.rcvColor.height}")
        dLog("🎨 rcvColor adapter itemCount: ${binding.rcvColor.adapter?.itemCount ?: 0}")
        dLog("🎨 layoutRcvColor visibility: ${binding.layoutRcvColor.visibility}")
        dLog("🎨 layoutRcvColor width: ${binding.layoutRcvColor.width}, height: ${binding.layoutRcvColor.height}")
    }

    private fun handleClickBottomNavigation(positionBottomNavigation: Int) {
        if (positionBottomNavigation == viewModel.positionNavSelected.value) return
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.setPositionNavSelected(positionBottomNavigation)
            viewModel.setPositionCustom(viewModel.dataCustomize.value!!.layerList[positionBottomNavigation].positionCustom)
            viewModel.setClickBottomNavigation(positionBottomNavigation)
            withContext(Dispatchers.Main) {
                // ✅ FIX: Update adapters with the correct lists for the new navigation tab
                customizeLayerAdapter.submitList(viewModel.itemNavList.value[positionBottomNavigation])
                colorLayerAdapter.submitListWithLog(viewModel.colorItemNavList.value[positionBottomNavigation])

                // ✅ Optimized: Scroll to selected color if exists
                binding.rcvColor.post {
                    if (viewModel.colorItemNavList.value[positionBottomNavigation].isNotEmpty()) {
                        val selectedColorIndex = viewModel.colorItemNavList.value[positionBottomNavigation]
                            .indexOfFirst { it.isSelected }
                        if (selectedColorIndex >= 0) {
                            binding.rcvColor.smoothScrollToPosition(selectedColorIndex)
                        }
                    }
                }

                // Check if the selected item in this tab is NONE or empty, then disable color
                val selectedItem = viewModel.itemNavList.value[positionBottomNavigation]
                    .firstOrNull { it.isSelected }

                // ✅ AUTO-DETECT: Tự động chọn pathIndex phù hợp với data structure
                val pathIndex = viewModel.getPathIndexForLayer(viewModel.positionNavSelected.value)
                if (selectedItem?.path == AssetsKey.NONE_LAYER ||
                    viewModel.pathSelectedList.value[pathIndex].isNullOrEmpty()) {
                    setColorRecyclerViewEnabled(false)
                } else {
                    setColorRecyclerViewEnabled(true)
                }

                checkStatusColor()
            }
        }
    }

    private fun confirmExit() {
        val dialog =
            ConfirmDialog(this, R.string.exit_your_customize, R.string.haven_t_saved_it_yet_do_you_want_to_exit)
        setLocale(this)
        dialog.show()
        dialog.onYesClick = {
            dialog.dismiss()
            finish()
        }
        dialog.onNoClick = {
            dialog.dismiss()
            hideNavigation()
        }
    }

    private fun handleSave() {
        lifecycleScope.launch {
            viewModel.saveImageFromView(this@CustomizeActivity, binding.layoutCustomLayer).collect { result ->
                when (result) {
                    is SaveState.Loading -> showLoading()
                    is SaveState.Error -> {
                        dismissLoading(true)
                        showToast(R.string.save_failed_please_try_again)
                    }

                    is SaveState.Success -> {
                        dismissLoading(true)
                        // Pass suggestion background if exists
                        val intent = Intent(this@CustomizeActivity, BackgroundActivity::class.java).apply {
                            putExtra(IntentKey.INTENT_KEY, result.path)
                            putExtra(IntentKey.CATEGORY_POSITION_KEY, viewModel.positionSelected)
                            // Pass suggestion background to BackgroundActivity
                            viewModel.getSuggestionBackground()?.let { bg ->
                                putExtra(IntentKey.SUGGESTION_BACKGROUND, bg)
                            }
                        }
                        startActivity(intent)
                    }
                }
            }
        }
    }

    private fun handleReset() {
        val dialog = ConfirmDialog(
            this@CustomizeActivity,
            R.string.reset_your_customize,
            R.string.change_your_whole_design_are_you_sure
        )
        setLocale(this)
        dialog.show()
        dialog.onYesClick = {
            viewModel.checkDataInternet(this) {
                dialog.dismiss()
                lifecycleScope.launch(Dispatchers.IO) {
                    val pathDefault = viewModel.setClickReset()
                    withContext(Dispatchers.Main) {
                        // ✅ FIX: Render lại tất cả layers
                        renderAllLayers()
                        customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
                        colorLayerAdapter.submitListWithLog(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])
                        binding.rcvColor.post {
                            binding.rcvColor.requestLayout()
                        }
                        hideNavigation()
                    }
                }
            }
        }
        dialog.onNoClick = {
            dialog.dismiss()
            hideNavigation()
        }
    }


    private fun handleRandomAllLayer() {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d("CustomizeActivity", "")
            Log.d("CustomizeActivity", "🎲 RANDOM ALL CLICKED")

            val timeStart = System.currentTimeMillis()
            val isOutTurn = viewModel.setClickRandomFullLayer()

            Log.d("CustomizeActivity", "pathSelectedList after Random All:")
            viewModel.pathSelectedList.value.forEachIndexed { idx, path ->
                if (path.isNotEmpty()) {
                    val layer = viewModel.dataCustomize.value?.layerList?.getOrNull(idx)
                    Log.d("CustomizeActivity", "  [$idx] posNav=${layer?.positionNavigation}, posCus=${layer?.positionCustom}, path=${path.substringAfterLast("/")}")
                } else {
                    Log.d("CustomizeActivity", "  [$idx] = EMPTY")
                }
            }

            withContext(Dispatchers.Main) {
                // ✅ Load ảnh cho tất cả layers theo đúng thứ tự (logging disabled for performance)

                // ✅ FIX: Render tất cả layers
                renderAllLayers()

                // ✅ Update adapter cho navigation hiện tại
                customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
                colorLayerAdapter.submitListWithLog(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])
                binding.rcvColor.post {
                    binding.rcvColor.requestLayout()
                }

                // ✅ CHECK: Nếu layer ở vị trí hiện tại không phải NONE thì enable rcvColor
                val currentSelectedItem = viewModel.itemNavList.value[viewModel.positionNavSelected.value]
                    .firstOrNull { it.isSelected }
                // ✅ AUTO-DETECT: Tự động chọn pathIndex phù hợp với data structure
                val pathIndex = viewModel.getPathIndexForLayer(viewModel.positionNavSelected.value)
                if (currentSelectedItem?.path != AssetsKey.NONE_LAYER &&
                    !viewModel.pathSelectedList.value[pathIndex].isNullOrEmpty()) {
                    setColorRecyclerViewEnabled(true)
                } else {
                    setColorRecyclerViewEnabled(false)
                }

                // ✅ SCROLL đến item đã được chọn sau khi random
                val selectedIndex = viewModel.itemNavList.value[viewModel.positionNavSelected.value]
                    .indexOfFirst { it.isSelected }
                if (selectedIndex >= 0) {
                    binding.rcvLayer.post {
                        binding.rcvLayer.smoothScrollToPosition(selectedIndex)
                    }
                }

                // ✅ SCROLL đến màu đã được chọn (nếu có)
                if (viewModel.colorItemNavList.value[viewModel.positionNavSelected.value].isNotEmpty()) {
                    val selectedColorIndex = viewModel.colorItemNavList.value[viewModel.positionNavSelected.value]
                        .indexOfFirst { it.isSelected }
                    if (selectedColorIndex >= 0) {
                        binding.rcvColor.post {
                            binding.rcvColor.smoothScrollToPosition(selectedColorIndex)
                        }
                    }
                }

                if (isOutTurn) binding.btnRandom.invisible()
                val timeEnd = System.currentTimeMillis()
                dLog("time random all : ${timeEnd - timeStart}")

                // ✅ WORKAROUND: Auto-click lại item đã focus ở tab 0 nếu:
                // 1. Mở từ CHARACTER_INDEX = 1 (category)
                // 2. HOẶC mở từ suggestion (Miley)
                // 3. VÀ đang ở tab 0 (positionNavSelected == 0)
                if ((categoryPosition == 1 || isSuggestion) && viewModel.positionNavSelected.value == 0) {
                    // Delay ngắn để đảm bảo adapter đã update xong
                    binding.rcvLayer.postDelayed({
                        val selectedItemPosition = viewModel.itemNavList.value[0].indexOfFirst { it.isSelected }
                        if (selectedItemPosition >= 0) {
                            val selectedItem = viewModel.itemNavList.value[0][selectedItemPosition]
                            // Trigger handleFillLayer để reload ảnh
                            if (selectedItem.path != AssetsKey.RANDOM_LAYER && selectedItem.path != AssetsKey.NONE_LAYER) {
                                lifecycleScope.launch(Dispatchers.IO) {
                                    val pathSelected = viewModel.setClickFillLayer(selectedItem, selectedItemPosition)
                                    withContext(Dispatchers.Main) {
                                        // ✅ FIX: Render lại tất cả layers
                                        renderAllLayers()
                                    }
                                }
                            }
                        }
                    }, 50) // 50ms delay
                }
            }
        }
    }
}
