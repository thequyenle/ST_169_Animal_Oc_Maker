package com.example.st169_animal_oc_maker.ui.customize

import android.content.Intent
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

        // Set background cho layoutRcvColor dựa trên category position
        val colorBarBackground = when(categoryPosition) {
            0 -> R.drawable.bg_color_cus_1
            1 -> R.drawable.bg_color_cus_2
            2 -> R.drawable.bg_color_cus_3
            else -> R.drawable.bg_color_cus_1
        }
        binding.layoutRcvColor.setBackgroundResource(colorBarBackground)

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
                    colorLayerAdapter.submitList(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])
                    if (viewModel.colorItemNavList.value[viewModel.positionNavSelected.value].isNotEmpty()) {
                        binding.rcvColor.smoothScrollToPosition(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value].indexOfFirst { it.isSelected })
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
    private fun initRcv() {
        binding.apply {
            rcvLayer.apply {
                adapter = customizeLayerAdapter
                itemAnimator = null
            }

            rcvColor.apply {
                adapter = colorLayerAdapter
                itemAnimator = null
            }

            rcvNavigation.apply {
                adapter = bottomNavigationAdapter
                itemAnimator = null
            }
        }
    }

    private fun handleRcv() {
        customizeLayerAdapter.onItemClick = { item, position ->
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

        customizeLayerAdapter.onNoneClick = { position ->
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

        customizeLayerAdapter.onRandomClick = {
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

        colorLayerAdapter.onItemClick = { position ->
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

        bottomNavigationAdapter.onItemClick = { positionBottomNavigation ->
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

            // Get data from list
            val deferred1 = async {
                viewModel.resetDataList()
                viewModel.addValueToItemNavList()
                viewModel.setItemColorDefault()
                viewModel.setFocusItemNavDefault()
                viewModel.setPositionCustom(viewModel.dataCustomize.value!!.layerList.first().positionCustom)
                viewModel.setPositionNavSelected(viewModel.dataCustomize.value!!.layerList.first().positionNavigation)
                viewModel.setBottomNavigationListDefault()

                // ✅ Apply suggestion preset if exists
                if (viewModel.hasSuggestionPreset()) {
                    viewModel.applySuggestionPreset()
                }

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
                        pathImageDefault = viewModel.dataCustomize.value!!.layerList.first().layer.first().image
                        viewModel.setIsSelectedItem(viewModel.positionCustom.value)
                        viewModel.setPathSelected(viewModel.positionCustom.value, pathImageDefault)
                        viewModel.setKeySelected(viewModel.positionNavSelected.value, pathImageDefault)
                    }
                    dLog("deferred5")
                }
                return@async true
            }

            withContext(Dispatchers.Main){
                if (deferred1.await() && deferred2.await() && deferred3.await()){
                    // ✅ Load all images from preset
                    if (viewModel.hasSuggestionPreset()) {
                        viewModel.pathSelectedList.value.forEachIndexed { index, path ->
                            if (path.isNotEmpty()) {
                                Glide.with(this@CustomizeActivity)
                                    .load(path)
                                    .into(viewModel.imageViewList.value[index])
                            }
                        }
                    } else {
                        // Load default image
                        Glide.with(this@CustomizeActivity)
                            .load(pathImageDefault)
                            .into(viewModel.imageViewList.value[viewModel.positionCustom.value])
                    }

                    customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
                    colorLayerAdapter.submitList(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])

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
                    if (selectedItem?.path == AssetsKey.NONE_LAYER ||
                        viewModel.pathSelectedList.value[viewModel.positionCustom.value].isNullOrEmpty()) {
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
                                            Glide.with(this@CustomizeActivity)
                                                .load(pathSelected)
                                                .into(viewModel.imageViewList.value[viewModel.positionCustom.value])
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
    }
    private fun checkStatusColor() {
        if (viewModel.colorItemNavList.value[viewModel.positionNavSelected.value].isNotEmpty()) {
            // Có màu -> hiện btnColor
            binding.btnColor.visible()

            if (viewModel.isShowColorList.value[viewModel.positionNavSelected.value]) {
                // Mặc định hiển thị khi vào màn hình
                isColorBarVisible = true
                binding.layoutRcvColor.visible()
            } else {
                isColorBarVisible = false
                binding.layoutRcvColor.invisible()
            }
        } else {
            // Không có màu -> ẩn cả layoutRcvColor và btnColor
            isColorBarVisible = false
            binding.layoutRcvColor.invisible()
            binding.btnColor.invisible()
        }
        updateColorIcon()

        // ✅ XÓA đoạn này - để logic enable/disable ở initData() xử lý
        // Vì checkStatusColor() chỉ nên quan tâm đến việc hiển thị UI, không nên can thiệp vào enable state
    }


    private fun handleFillLayer(item: ItemNavCustomModel, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val pathSelected = viewModel.setClickFillLayer(item, position)
            withContext(Dispatchers.Main) {
                Glide.with(this@CustomizeActivity).load(pathSelected)
                    .into(viewModel.imageViewList.value[viewModel.positionCustom.value])
                customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
                // Enable lại rcvColor khi chọn item khác (không phải btnNone)
                setColorRecyclerViewEnabled(true)
            }
        }
    }

    private fun handleNoneLayer(position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.setIsSelectedItem(viewModel.positionCustom.value)
            viewModel.setPathSelected(viewModel.positionCustom.value, "")
            viewModel.setKeySelected(viewModel.positionNavSelected.value, "")
            viewModel.setItemNavList(viewModel.positionNavSelected.value, position)
            withContext(Dispatchers.Main) {
                Glide.with(this@CustomizeActivity).clear(viewModel.imageViewList.value[viewModel.positionCustom.value])
                customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
                // Disable rcvColor khi click btnNone
                setColorRecyclerViewEnabled(false)
            }
        }
    }

    private fun handleRandomLayer() {
        lifecycleScope.launch(Dispatchers.IO) {
            val (pathRandom, isMoreColors) = viewModel.setClickRandomLayer()
            withContext(Dispatchers.Main) {
                Glide.with(this@CustomizeActivity).load(pathRandom)
                    .into(viewModel.imageViewList.value[viewModel.positionCustom.value])
                customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
                if (isMoreColors) {
                    colorLayerAdapter.submitList(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])
                    binding.rcvColor.smoothScrollToPosition(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value].indexOfFirst { it.isSelected })
                }
                // Enable lại rcvColor khi click random
                setColorRecyclerViewEnabled(true)
            }
        }
    }

    private fun handleChangeColorLayer(position: Int) {
        // Chỉ cho phép thay đổi màu nếu rcvColor đang enabled
        if (!isColorEnabled) return

        lifecycleScope.launch(Dispatchers.IO) {
            val pathColor = viewModel.setClickChangeColor(position)
            withContext(Dispatchers.Main) {
                Glide.with(this@CustomizeActivity).load(pathColor).into(viewModel.imageViewList.value[viewModel.positionCustom.value])
                colorLayerAdapter.submitList(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])
            }
        }
    }

    private fun setColorRecyclerViewEnabled(enabled: Boolean) {
        isColorEnabled = enabled
        binding.rcvColor.alpha = if (enabled) 1.0f else 1.0f
        colorLayerAdapter.isEnabled = enabled
    }

    private fun handleClickBottomNavigation(positionBottomNavigation: Int) {
        if (positionBottomNavigation == viewModel.positionNavSelected.value) return
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.setPositionNavSelected(positionBottomNavigation)
            viewModel.setPositionCustom(viewModel.dataCustomize.value!!.layerList[positionBottomNavigation].positionCustom)
            viewModel.setClickBottomNavigation(positionBottomNavigation)
            withContext(Dispatchers.Main) { checkStatusColor() }
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
                        viewModel.imageViewList.value.forEach { imageView ->
                            Glide.with(this@CustomizeActivity).clear(imageView)
                        }
                        Glide.with(this@CustomizeActivity).load(pathDefault).into(viewModel.imageViewList.value[viewModel.dataCustomize.value!!.layerList.first().positionCustom])
                        customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
                        colorLayerAdapter.submitList(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])
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
            val timeStart = System.currentTimeMillis()
            val isOutTurn = viewModel.setClickRandomFullLayer()

            withContext(Dispatchers.Main) {
                // ✅ Load ảnh cho tất cả layers theo đúng thứ tự
                viewModel.dataCustomize.value?.layerList?.forEach { layerListModel ->
                    val positionCustom = layerListModel.positionCustom
                    val path = viewModel.pathSelectedList.value.getOrNull(positionCustom)
                    if (!path.isNullOrEmpty()) {
                        Glide.with(this@CustomizeActivity)
                            .load(path)
                            .into(viewModel.imageViewList.value[positionCustom])
                    } else {
                        // Clear nếu path rỗng
                        Glide.with(this@CustomizeActivity)
                            .clear(viewModel.imageViewList.value[positionCustom])
                    }
                }

                // ✅ Update adapter cho navigation hiện tại
                customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
                colorLayerAdapter.submitList(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])

                // ✅ CHECK: Nếu layer ở vị trí hiện tại không phải NONE thì enable rcvColor
                val currentSelectedItem = viewModel.itemNavList.value[viewModel.positionNavSelected.value]
                    .firstOrNull { it.isSelected }
                if (currentSelectedItem?.path != AssetsKey.NONE_LAYER &&
                    !viewModel.pathSelectedList.value[viewModel.positionCustom.value].isNullOrEmpty()) {
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
                                        Glide.with(this@CustomizeActivity)
                                            .load(pathSelected)
                                            .into(viewModel.imageViewList.value[viewModel.positionCustom.value])
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
