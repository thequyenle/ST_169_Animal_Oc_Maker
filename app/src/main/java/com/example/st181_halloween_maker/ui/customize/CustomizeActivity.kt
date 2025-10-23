package com.example.st181_halloween_maker.ui.customize

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseActivity
import com.example.st181_halloween_maker.core.dialog.ConfirmDialog
import com.example.st181_halloween_maker.core.extensions.dLog
import com.example.st181_halloween_maker.core.extensions.eLog
import com.example.st181_halloween_maker.core.extensions.hideNavigation
import com.example.st181_halloween_maker.core.extensions.invisible
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.extensions.showToast
import com.example.st181_halloween_maker.core.extensions.startIntent
import com.example.st181_halloween_maker.core.extensions.visible
import com.example.st181_halloween_maker.core.utils.SaveState
import com.example.st181_halloween_maker.core.utils.SystemUtils.setLocale
import com.example.st181_halloween_maker.core.utils.key.IntentKey
import com.example.st181_halloween_maker.core.utils.key.ValueKey
import com.example.st181_halloween_maker.data.custom.ItemNavCustomModel
import com.example.st181_halloween_maker.databinding.ActivityCustomizeBinding
import com.example.st181_halloween_maker.ui.background.BackgroundActivity
import com.example.st181_halloween_maker.ui.home.DataViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.jvm.java
import kotlin.text.clear
import kotlin.text.get

class CustomizeActivity : BaseActivity<ActivityCustomizeBinding>() {
    private val viewModel: CustomizeViewModel by viewModels()
    private val dataViewModel: DataViewModel by viewModels()
    val colorLayerAdapter by lazy { ColorLayerAdapter(this) }
    val customizeLayerAdapter by lazy { CustomizeLayerAdapter(this) }
    val bottomNavigationAdapter by lazy { BottomNavigationAdapter(this) }

    override fun setViewBinding(): ActivityCustomizeBinding {
        return ActivityCustomizeBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        lifecycleScope.launch { showLoading() }
        dataViewModel.ensureData(this)
    }

    override fun dataObservable() {
        // allData
        lifecycleScope.launch {
            dataViewModel.allData.collect { list ->
                if (list.isNotEmpty()) {
                    viewModel.positionSelected = intent.getIntExtra(IntentKey.INTENT_KEY, 0)
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
            btnRandom.onSingleClick { viewModel.checkDataInternet(this@CustomizeActivity) { handleRandomAllLayer() } }
            btnReset.onSingleClick { handleReset() }
            btnFlip.onSingleClick { viewModel.setIsFlip() }
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
        customizeLayerAdapter.onItemClick =
            { item, position -> viewModel.checkDataInternet(this) { handleFillLayer(item, position) } }

        customizeLayerAdapter.onNoneClick =
            { position -> viewModel.checkDataInternet(this) { handleNoneLayer(position) } }

        customizeLayerAdapter.onRandomClick = { viewModel.checkDataInternet(this) { handleRandomLayer() } }

        colorLayerAdapter.onItemClick =
            { position -> viewModel.checkDataInternet(this) { handleChangeColorLayer(position) } }

        bottomNavigationAdapter.onItemClick =
            { positionBottomNavigation -> handleClickBottomNavigation(positionBottomNavigation) }
    }

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
            // Fill data default
            val deferred3 = async {
                if (deferred1.await() && deferred2.await()) {
                    pathImageDefault = viewModel.dataCustomize.value!!.layerList.first().layer.first().image
                    viewModel.setIsSelectedItem(viewModel.positionCustom.value)
                    viewModel.setPathSelected(viewModel.positionCustom.value, pathImageDefault)
                    viewModel.setKeySelected(viewModel.positionNavSelected.value, pathImageDefault)
                    dLog("deferred5")
                }
                return@async true
            }

            withContext(Dispatchers.Main){
                if (deferred1.await() && deferred2.await() && deferred3.await()){
                    Glide.with(this@CustomizeActivity).load(pathImageDefault).into(viewModel.imageViewList.value[viewModel.positionCustom.value])
                    customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
                    colorLayerAdapter.submitList(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])
                    checkStatusColor()
                    dismissLoading()
                    dLog("main")
                }
            }
        }
    }

    private fun checkStatusColor() {
        if (viewModel.colorItemNavList.value[viewModel.positionNavSelected.value].isNotEmpty()) {
            if (viewModel.isShowColorList.value[viewModel.positionNavSelected.value]) {
                binding.layoutRcvColor.visible()
            } else {
                binding.layoutRcvColor.invisible()
            }
        } else {
            binding.layoutRcvColor.invisible()
        }
    }

    private fun handleFillLayer(item: ItemNavCustomModel, position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val pathSelected = viewModel.setClickFillLayer(item, position)
            withContext(Dispatchers.Main) {
                Glide.with(this@CustomizeActivity).load(pathSelected)
                    .into(viewModel.imageViewList.value[viewModel.positionCustom.value])
                customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
            }
        }
    }

    private fun handleNoneLayer(position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            viewModel.setIsSelectedItem(viewModel.positionCustom.value)
            viewModel.setPathSelected(viewModel.positionCustom.value, "")
            viewModel.setItemNavList(viewModel.positionNavSelected.value, position)
            withContext(Dispatchers.Main) {
                Glide.with(this@CustomizeActivity).clear(viewModel.imageViewList.value[viewModel.positionCustom.value])
                customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
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
            }
        }
    }

    private fun handleChangeColorLayer(position: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val pathColor = viewModel.setClickChangeColor(position)
            withContext(Dispatchers.Main) {
                Glide.with(this@CustomizeActivity).load(pathColor).into(viewModel.imageViewList.value[viewModel.positionCustom.value])
                colorLayerAdapter.submitList(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])
            }
        }
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
                        startIntent(BackgroundActivity::class.java, result.path)
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
                viewModel.pathSelectedList.value.forEachIndexed { index, path ->
                    if (path != "") Glide.with(this@CustomizeActivity).load(path).into(viewModel.imageViewList.value[index])
                }
                customizeLayerAdapter.submitList(viewModel.itemNavList.value[viewModel.positionNavSelected.value])
                colorLayerAdapter.submitList(viewModel.colorItemNavList.value[viewModel.positionNavSelected.value])
                if (isOutTurn) binding.btnRandom.invisible()
                val timeEnd = System.currentTimeMillis()
                dLog("time random all : ${timeEnd - timeStart}")
            }
        }
    }
}