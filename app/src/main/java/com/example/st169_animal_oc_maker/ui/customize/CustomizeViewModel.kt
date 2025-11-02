package com.example.st169_animal_oc_maker.ui.customize

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.lifecycle.ViewModel
import com.example.st169_animal_oc_maker.R
import com.example.st169_animal_oc_maker.core.extensions.showToast
import com.example.st169_animal_oc_maker.core.helper.BitmapHelper
import com.example.st169_animal_oc_maker.core.helper.InternetHelper
import com.example.st169_animal_oc_maker.core.helper.MediaHelper
import com.example.st169_animal_oc_maker.core.utils.HandleState
import com.example.st169_animal_oc_maker.core.utils.SaveState
import com.example.st169_animal_oc_maker.core.utils.key.AssetsKey
import com.example.st169_animal_oc_maker.core.utils.key.ValueKey
import com.example.st169_animal_oc_maker.data.custom.CustomizeModel
import com.example.st169_animal_oc_maker.data.custom.ItemColorImageModel
import com.example.st169_animal_oc_maker.data.custom.ItemColorModel
import com.example.st169_animal_oc_maker.data.custom.ItemNavCustomModel
import com.example.st169_animal_oc_maker.data.custom.LayerListModel
import com.example.st169_animal_oc_maker.data.custom.NavigationModel
import com.example.st169_animal_oc_maker.data.suggestion.RandomState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.apply
import kotlin.collections.eachCount
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.forEachIndexed
import kotlin.collections.groupingBy
import kotlin.collections.indexOfFirst
import kotlin.collections.isNotEmpty
import kotlin.collections.mapIndexed
import kotlin.collections.toCollection
import kotlin.ranges.random
import kotlin.ranges.until
import kotlin.to

class CustomizeViewModel : ViewModel() {
    // Đếm số lần random, chỉ số được chọn
    var countRandom = 0
    var positionSelected = 0

    // Data từ API hay không
    private val _isDataAPI = MutableStateFlow(false)

    // Trạng thái flip
    private val _isFlip = MutableStateFlow(false)
    val isFlip = _isFlip.asStateFlow()

    // Biến lưu suggestion preset
    private val _suggestionState = MutableStateFlow<RandomState?>(null)
    private val _suggestionBackground = MutableStateFlow<String?>(null)

    //----------------------------------------------------------------------------------------------------------------------
    private val _positionNavSelected = MutableStateFlow(-1)
    val positionNavSelected = _positionNavSelected.asStateFlow()

    private val _positionCustom = MutableStateFlow(-1)
    val positionCustom = _positionCustom.asStateFlow()

    // Data gốc
    private val _dataCustomize = MutableStateFlow<CustomizeModel?>(null)
    val dataCustomize = _dataCustomize.asStateFlow()

    // Danh sách Navigation bottom
    private val _bottomNavigationList = MutableStateFlow(arrayListOf<NavigationModel>())
    val bottomNavigationList = _bottomNavigationList.asStateFlow()

    // Danh sách layer & item
//    private val _categoryList = MutableStateFlow(arrayListOf<ArrayList<LayerModel>>())
//    val categoryList = _categoryList.asStateFlow()

    private val _itemNavList = MutableStateFlow(arrayListOf<ArrayList<ItemNavCustomModel>>())
    val itemNavList = _itemNavList.asStateFlow()

    // Danh sách màu
    private val _colorItemNavList = MutableStateFlow(arrayListOf<ArrayList<ItemColorModel>>())
    val colorItemNavList = _colorItemNavList.asStateFlow()

    // Trạng thái chọn item/màu
    private val _positionColorItemList = MutableStateFlow(arrayListOf<Int>())
    val positionColorItemList = _positionColorItemList.asStateFlow()

    private val _isSelectedItemList = MutableStateFlow(arrayListOf<Boolean>())
    val isSelectedItemList = _isSelectedItemList.asStateFlow()

    private val _isShowColorList = MutableStateFlow(arrayListOf<Boolean>())
    val isShowColorList = _isShowColorList.asStateFlow()

    // Key + Path đã chọn
    private val _keySelectedItemList = MutableStateFlow(arrayListOf<String>())
    val keySelectedItemList = _keySelectedItemList.asStateFlow()

    private val _pathSelectedList = MutableStateFlow(arrayListOf<String>())
    val pathSelectedList = _pathSelectedList.asStateFlow()

    // Danh sách ImageView trên layout
    private val _imageViewList = MutableStateFlow(arrayListOf<ImageView>())
    val imageViewList = _imageViewList.asStateFlow()

    // ✅ FIX: ImageView riêng cho Body layer (để tránh conflict với Ears)
    private val _bodyImageView = MutableStateFlow<ImageView?>(null)
    val bodyImageView = _bodyImageView.asStateFlow()

    // 🔧 HARDFIX: ImageView riêng cho Layer[24] của Character 1 (Miley) (render trước Body)
    private val _layer24ImageView = MutableStateFlow<ImageView?>(null)
    val layer24ImageView = _layer24ImageView.asStateFlow()

    private val _colorListMost = MutableStateFlow(arrayListOf<String>())
    val colorListMost = _colorListMost.asStateFlow()

    // ✅ PERFORMANCE: Cache for layer index mapping (positionNavigation -> layerIndex)
    private val _layerIndexCache = MutableStateFlow<Map<Int, Int>>(emptyMap())

    //----------------------------------------------------------------------------------------------------------------------
    // Base setter
    suspend fun setPositionNavSelected(position: Int) {
        _positionNavSelected.value = position
    }

    suspend fun setPositionCustom(position: Int) {
        _positionCustom.value = position
    }

    fun setDataCustomize(data: CustomizeModel) {
        _dataCustomize.value = data
        // ✅ Build cache when data is set
        buildLayerIndexCache()
    }

    private fun buildLayerIndexCache() {
        val layerList = _dataCustomize.value?.layerList ?: return
        val cache = mutableMapOf<Int, Int>()

        layerList.forEachIndexed { index, layer ->
            // 🔧 HARDFIX: Character 1 & 2, Layer[21] có posNav=20 trong data
            // → Bỏ qua duplicate warning và thêm vào cache với posNav=21
            if ((positionSelected == 1 || positionSelected == 2) && index == 21 && layer.positionNavigation == 20) {
                cache[21] = index  // Thêm vào cache với key=21
                Log.d("CustomizeViewModel", "🔧 HARDFIX Character $positionSelected: Layer[21] mapped to posNav=21 (actual posNav=20, posCus=${layer.positionCustom})")
                return@forEachIndexed
            }

            // ⚠️ Detect duplicate positionNavigation
            if (cache.containsKey(layer.positionNavigation)) {
                Log.e("CustomizeViewModel", "⚠️ DUPLICATE positionNavigation=${layer.positionNavigation}!")
                Log.e("CustomizeViewModel", "   Layer[${cache[layer.positionNavigation]}]: posNav=${layer.positionNavigation}")
                Log.e("CustomizeViewModel", "   Layer[$index]: posNav=${layer.positionNavigation}, posCus=${layer.positionCustom}")
                Log.e("CustomizeViewModel", "   → Using FIRST occurrence (Layer[${cache[layer.positionNavigation]}])")
                // ✅ KHÔNG ghi đè - giữ layer đầu tiên
            } else {
                cache[layer.positionNavigation] = index
            }
        }

        _layerIndexCache.value = cache

        // 🔍 LOG: Full cache for debugging
        Log.d("CustomizeViewModel", "📋 Layer Index Cache built: ${cache.size} entries (Character $positionSelected)")
        cache.entries.sortedBy { it.key }.forEach { (posNav, layerIndex) ->
            val layer = layerList[layerIndex]
            Log.d("CustomizeViewModel", "   posNav=$posNav → Layer[$layerIndex] (posCus=${layer.positionCustom})")
        }
    }

    fun setIsDataAPI(isAPI: Boolean) {
        _isDataAPI.value = isAPI
    }

    fun isDataAPI(): Boolean {
        return _isDataAPI.value
    }

    fun setIsFlip() {
        _isFlip.value = !_isFlip.value
    }

    fun setPositionColorItemList(positionList: ArrayList<Int>) {
        _positionColorItemList.value = positionList
    }

    fun setPositionColorForLayer(layerPosition: Int, colorPosition: Int) {
        _positionColorItemList.value[layerPosition] = colorPosition
        Log.d("CustomizeViewModel", "🎨 setPositionColorForLayer: layer=$layerPosition, color=$colorPosition")
    }

    fun setIsSelectedItemList(selectedList: ArrayList<Boolean>) {
        _isSelectedItemList.value = selectedList
    }

    fun setIsShowColorList(showList: ArrayList<Boolean>) {
        _isShowColorList.value = showList
    }

//    suspend fun setCategoryList(categoryList: ArrayList<ArrayList<LayerModel>>) {
//        _categoryList.value = categoryList
//    }

    fun setKeySelectedItemList(keyList: ArrayList<String>) {
        _keySelectedItemList.value = keyList
    }

    fun setPathSelectedList(pathList: ArrayList<String>) {
        _pathSelectedList.value = pathList
    }

    fun setColorListMost(colorList: ArrayList<String>) {
        _colorListMost.value = colorList
    }

    fun setSuggestionPreset(stateJson: String?, background: String?) {
        stateJson?.let {
            _suggestionState.value = RandomState.fromJson(it)
        }
        _suggestionBackground.value = background
    }

    /**
     * Apply suggestion preset after character loaded
     * Call this after setDataCustomize() and before setFocusItemNavDefault()
     */
    suspend fun applySuggestionPreset() {
        val preset = _suggestionState.value ?: return

        Log.d("CustomizeViewModel", "📊 APPLYING SUGGESTION PRESET (${preset.layerSelections.size} layers)")

        // 🎯 LOG: Chỉ log Miley keys
        preset.layerSelections.forEach { (key, sel) ->
            if (key == 20 || key == 17 || key == 22 || sel.path.contains("18-21") || sel.path.contains("23-21") || sel.path.contains("21-18")) {
                Log.d("CustomizeViewModel", "🎯 MILEY: key=$key, itemIndex=${sel.itemIndex}, path=${sel.path.substringAfterLast("/")}")
            }
        }

        // 🎯 LOG: Chỉ log Miley layers và duplicates
        _dataCustomize.value?.layerList?.forEachIndexed { index, layer ->
            if (layer.positionNavigation == 20) {
                Log.d("CustomizeViewModel", "🎯 MILEY LAYER: index=$index, positionCustom=${layer.positionCustom}, items=${layer.layer.size}")
            }
        }

        // 🎯 Check duplicates và missing
        val layers = _dataCustomize.value?.layerList ?: return
        val posNavs = layers.map { it.positionNavigation }.sorted()
        val duplicates = layers.groupingBy { it.positionNavigation }.eachCount().filter { it.value > 1 }

        duplicates.forEach { (posNav, count) ->
            Log.e("CustomizeViewModel", "❌ DUPLICATE positionNav=$posNav ($count times)")
        }

        // Check missing positionNav
        val expected = (0 until layers.size).toList()
        val missing = expected - posNavs.toSet()
        if (missing.isNotEmpty()) {
            Log.e("CustomizeViewModel", "❌ MISSING positionNav: $missing")
        }

        // Apply each layer selection
        preset.layerSelections.forEach { (storageKey, selection) ->
            // 🎯 LOG: Chỉ log Miley processing
            if (storageKey == 20 || storageKey == 17 || storageKey == 22) {
                Log.d("CustomizeViewModel", "🎯 PROCESSING MILEY: key=$storageKey, itemIndex=${selection.itemIndex}")
            }

            // ✅ ULTIMATE FIX: Tìm layer theo positionNavigation thay vì positionCustom
            // Vì có thể có nhiều layers cùng positionCustom (Body và Ears đều có positionCustom=1)
            val layer = if (storageKey == -1) {
                // Key=-1 là body layer (positionNavigation=0)
                val bodyLayer = _dataCustomize.value?.layerList?.find { it.positionNavigation == 0 }
                if (bodyLayer == null) {
                    Log.e("CustomizeViewModel", "❌ Body layer (positionNavigation=0) not found for key=-1")
                    return@forEach
                }
                Log.d("CustomizeViewModel", "✅ Found body layer: positionNav=0, positionCustom=${bodyLayer.positionCustom}")
                bodyLayer
            } else {
                // Tìm layer có positionCustom = storageKey
                // ⚠️ NHƯNG nếu có nhiều layers cùng positionCustom, cần tìm layer KHÔNG phải body
                val candidateLayers = _dataCustomize.value?.layerList?.filter { it.positionCustom == storageKey }

                if (candidateLayers.isNullOrEmpty()) {
                    Log.e("CustomizeViewModel", "❌ No layer found with positionCustom=$storageKey")
                    Log.e("CustomizeViewModel", "   Suggestion path: ${selection.path}")
                    Log.e("CustomizeViewModel", "   Available positionCustom values: ${_dataCustomize.value?.layerList?.map { it.positionCustom }}")

                    // 🎯 Special case for Miley
                    if (storageKey == 20 || selection.path.contains("21-18")) {
                        Log.e("CustomizeViewModel", "🎯🎯🎯 MILEY KEY=20 NOT FOUND! 🎯🎯🎯")
                        Log.e("CustomizeViewModel", "   Suggestion wants positionCustom=20")
                        Log.e("CustomizeViewModel", "   But customize doesn't have layer with positionCustom=20")
                        Log.e("CustomizeViewModel", "   This is why Miley item doesn't show in customize!")
                    }
                    return@forEach
                }

                // Nếu có nhiều layers cùng positionCustom, chọn layer KHÔNG phải body (positionNav != 0)
                val targetLayer = if (candidateLayers.size > 1) {
                    val nonBodyLayer = candidateLayers.find { it.positionNavigation != 0 }
                    if (nonBodyLayer != null) {
                        Log.d("CustomizeViewModel", "⚠️ Multiple layers with positionCustom=$storageKey, choosing non-body layer: positionNav=${nonBodyLayer.positionNavigation}")
                        nonBodyLayer
                    } else {
                        Log.d("CustomizeViewModel", "Using first layer with positionCustom=$storageKey")
                        candidateLayers.first()
                    }
                } else {
                    Log.d("CustomizeViewModel", "Found layer with positionCustom=$storageKey")
                    candidateLayers.first()
                }

                targetLayer
            }

            val layerIndex = _dataCustomize.value?.layerList?.indexOf(layer) ?: -1
            if (layerIndex < 0) {
                Log.e("CustomizeViewModel", "❌ Layer index not found")
                return@forEach
            }

            // 🎯 LOG: Chỉ log Miley matches
            if (layer.positionNavigation == 20) {
                Log.d("CustomizeViewModel", "🎯 MILEY MATCHED: positionCustom=${layer.positionCustom}, wants itemIndex=${selection.itemIndex}")
            }

            // Validate item index
            if (selection.itemIndex >= layer.layer.size) {
                Log.e("CustomizeViewModel", "❌ SUGGESTION-CUSTOMIZE MISMATCH!")
                Log.e("CustomizeViewModel", "   Suggestion itemIndex: ${selection.itemIndex}")
                Log.e("CustomizeViewModel", "   Customize layer.size: ${layer.layer.size}")
                Log.e("CustomizeViewModel", "   Layer positionNav: ${layer.positionNavigation}")
                Log.e("CustomizeViewModel", "   Suggestion path: ${selection.path}")

                // 🎯 LOG: Miley mismatch
                if (layer.positionNavigation == 20) {
                    Log.e("CustomizeViewModel", "🎯 MILEY MISMATCH: wants index ${selection.itemIndex}, has ${layer.layer.size} items")
                }
                Log.e("CustomizeViewModel", "   → Item không tồn tại trong Customize, fallback to item 0")

                // 📊 Generate detailed report
                val report = generateApiMismatchReport()
                Log.e("CustomizeViewModel", "📊 FULL REPORT:")
                report.lines().forEach { line ->
                    Log.e("CustomizeViewModel", line)
                }

                // Fallback to first item (index 0) instead of crashing
                val fallbackItem = layer.layer.firstOrNull()
                if (fallbackItem == null) {
                    Log.e("CustomizeViewModel", "❌ Layer has no items at all!")
                    return@forEach
                }

                val item = fallbackItem
                Log.d("CustomizeViewModel", "🔄 Using fallback item: index=0, path=${item.image}")

                // Set UI focus to fallback item (index 0 + offset)
                val rcvIndex = if (layerIndex == 0) 1 else 2  // 0 + offset for buttons
                setItemNavList(layer.positionNavigation, rcvIndex)

                // Use suggestion path (from thumbnail) instead of fallback item path
                val pathIndex = getPathIndexForLayer(layer.positionNavigation)
                _pathSelectedList.value[pathIndex] = selection.path
                _keySelectedItemList.value[layer.positionNavigation] = selection.path
                _isSelectedItemList.value[layer.positionNavigation] = true

                Log.d("CustomizeViewModel", "✅ Applied fallback with suggestion path: ${selection.path}")
                return@forEach
            }

            val item = layer.layer[selection.itemIndex]

            // 🎯 LOG: Miley success
            if (layer.positionNavigation == 20) {
                Log.d("CustomizeViewModel", "🎯 MILEY SUCCESS: index=${selection.itemIndex}, pathMatch=${item.image == selection.path}")
            }

            // 🎯 FIX: Tránh conflict cho positionNav=20 (chỉ khi backend chưa fix)
            // Kiểm tra xem có duplicate positionNav=20 không
            val duplicateCount = _dataCustomize.value?.layerList?.count { it.positionNavigation == 20 } ?: 0
            if (duplicateCount > 1 && layer.positionNavigation == 20 && _keySelectedItemList.value[20].isNotEmpty()) {
                Log.w("CustomizeViewModel", "⚠️ MILEY CONFLICT SKIP: key=$storageKey skipped (backend not fixed)")
                return@forEach
            }

            // ✅ Set keySelectedItemList (for tracking)
            _keySelectedItemList.value[layer.positionNavigation] = selection.path

            // 🎯 LOG: Miley conflict
            if (layer.positionNavigation == 20) {
                Log.d("CustomizeViewModel", "🎯 MILEY SET: key=$storageKey sets positionNav=20")
            }

            // Set selected state
            _isSelectedItemList.value[layer.positionNavigation] = true

            // Set color if applicable
            if (item.isMoreColors && item.listColor.isNotEmpty()) {
                val validColorIndex = selection.colorIndex.coerceIn(0, item.listColor.size - 1)
                _positionColorItemList.value[layer.positionNavigation] = validColorIndex
                _isShowColorList.value[layer.positionNavigation] = true
                Log.d("CustomizeViewModel", "Set color: positionNav=${layer.positionNavigation}, colorIndex=$validColorIndex")

                // ✅ CRITICAL FIX: Update color list to match selected item's colors
                // Rebuild color list from the selected item (not from layer.first())
                val colorList = ArrayList<ItemColorModel>()
                item.listColor.forEachIndexed { colorIndex, colorItem ->
                    colorList.add(ItemColorModel(
                        color = colorItem.color,
                        isSelected = (colorIndex == validColorIndex) // Set focus on preset color
                    ))
                }

                // Create new ArrayList to trigger StateFlow update
                val updatedColorItemNavList = ArrayList(_colorItemNavList.value)
                updatedColorItemNavList[layer.positionNavigation] = colorList
                _colorItemNavList.value = updatedColorItemNavList

                Log.d("CustomizeViewModel", "✅ Updated color list for positionNav=${layer.positionNavigation}, focused color=$validColorIndex, total colors=${colorList.size}")
            }

            //  CRITICAL FIX: Convert data model index → RecyclerView index
            // RecyclerView có None/Random buttons ở đầu, cần cộng offset:
            // - Layer đầu (Body): chỉ có Random button → offset +1
            // - Các layer khác: có None + Random buttons → offset +2
            val rcvIndex = if (layerIndex == 0) {
                selection.itemIndex + 1  // Body layer: Random button at index 0
            } else {
                selection.itemIndex + 2  // Other layers: None(0) + Random(1)
            }

            setItemNavList(layer.positionNavigation, rcvIndex)

            //  FIX: Trigger load ảnh cho item được focus từ suggestion
            // Nếu không có màu hoặc dùng màu đầu tiên, load ảnh base
            val finalPath = if (item.isMoreColors && item.listColor.isNotEmpty()) {
                val validColorIndex = selection.colorIndex.coerceIn(0, item.listColor.size - 1)
                item.listColor[validColorIndex].path
            } else {
                item.image
            }

            // Update pathSelectedList với ảnh đúng của item được focus
            val pathIndex = getPathIndexForLayer(layer.positionNavigation)

            // 🎯 LOG: Path conflict
            if (layer.positionNavigation == 20) {
                Log.w("CustomizeViewModel", "⚠️ MILEY PATH OVERWRITE: ${_pathSelectedList.value[pathIndex]} → $finalPath")
            }

            //  FIX DUPLICATE POSITION CUSTOM: Xóa các layers có cùng positionCustom
            // Tìm tất cả layers có cùng positionCustom với layer hiện tại
            val currentPositionCustom = layer.positionCustom
            val allLayers = _dataCustomize.value?.layerList ?: emptyList()
            allLayers.forEachIndexed { idx, otherLayer ->
                if (otherLayer.positionCustom == currentPositionCustom &&
                    otherLayer.positionNavigation != layer.positionNavigation) {
                    // Xóa path của layer duplicate
                    _pathSelectedList.value[idx] = ""
                    _keySelectedItemList.value[otherLayer.positionNavigation] = ""
                    _isSelectedItemList.value[otherLayer.positionNavigation] = false
                    Log.d("CustomizeViewModel", "🧹 PRESET CLEAR DUPLICATE: Cleared positionNav=${otherLayer.positionNavigation} (same positionCustom=$currentPositionCustom)")
                }
            }

            _pathSelectedList.value[pathIndex] = finalPath
        }

        //  Set initial navigation to body layer (first layer)
        val firstLayer = _dataCustomize.value?.layerList?.firstOrNull()
        if (firstLayer != null) {
            setPositionCustom(firstLayer.positionCustom)
            setPositionNavSelected(firstLayer.positionNavigation)
            Log.d("CustomizeViewModel", "Set initial position to body layer: positionCustom=${firstLayer.positionCustom}, positionNav=${firstLayer.positionNavigation}")
        }

        Log.d("CustomizeViewModel", "✅ Suggestion preset applied")
    }
    /**
     * Get suggestion background
     */
    fun getSuggestionBackground(): String? {
        return _suggestionBackground.value
    }

    /**
     * Check if has suggestion preset
     */
    fun hasSuggestionPreset(): Boolean {
        return _suggestionState.value != null
    }




    //----------------------------------------------------------------------------------------------------------------------
    // Setter suspend
    suspend fun setPositionColorItem(position: Int, newPosition: Int) {
        _positionColorItemList.value =
            _positionColorItemList.value.mapIndexed { index, oldPosition -> if (index == position) newPosition else oldPosition }
                .toCollection(ArrayList())
    }

    suspend fun setIsSelectedItem(position: Int) {
        _isSelectedItemList.value[position] = true
    }

    suspend fun setKeySelected(position: Int, newKey: String) {
        _keySelectedItemList.value = _keySelectedItemList.value
            .mapIndexed { index, oldKey -> if (index == position) newKey else oldKey }
            .toCollection(ArrayList())
    }

    suspend fun setPathSelected(position: Int, newPath: String) {
        _pathSelectedList.value =
            _pathSelectedList.value.mapIndexed { index, oldPath -> if (index == position) newPath else oldPath }
                .toCollection(ArrayList())
    }

    /**
     * ✅ FIX DUPLICATE POSITION CUSTOM:
     * Xóa tất cả paths của các layers có cùng positionCustom với layer hiện tại
     * Ví dụ: Đuôi A và Đuôi B cùng positionCustom=21 → Khi chọn Đuôi B, xóa path của Đuôi A
     */
    private suspend fun clearLayersWithSamePositionCustom(positionNavigation: Int) {
        val layerList = _dataCustomize.value?.layerList ?: return
        val currentLayer = layerList.find { it.positionNavigation == positionNavigation } ?: return
        val currentPositionCustom = currentLayer.positionCustom

        // Tìm tất cả layers có cùng positionCustom (trừ layer hiện tại)
        layerList.forEachIndexed { index, layer ->
            if (layer.positionCustom == currentPositionCustom && layer.positionNavigation != positionNavigation) {
                // Xóa path và reset state của layer này
                _pathSelectedList.value[index] = ""
                _keySelectedItemList.value[layer.positionNavigation] = ""
                _isSelectedItemList.value[layer.positionNavigation] = false

                Log.d("CustomizeViewModel", "🧹 CLEAR DUPLICATE: Cleared layer positionNav=${layer.positionNavigation} (same positionCustom=$currentPositionCustom)")
            }
        }
    }

    //----------------------------------------------------------------------------------------------------------------------
    // Bottom Navigation
    suspend fun setBottomNavigationList(bottomNavList: ArrayList<NavigationModel>) {
        _bottomNavigationList.value = bottomNavList
    }

    suspend fun setBottomNavigationListDefault() {
        val outputBottomNavigationList = arrayListOf<NavigationModel>()
        _dataCustomize.value!!.layerList.forEach { layerList ->
            outputBottomNavigationList.add(NavigationModel(imageNavigation = layerList.imageNavigation))
        }
        outputBottomNavigationList.first().isSelected = true
        _bottomNavigationList.value = outputBottomNavigationList
    }


    suspend fun setClickBottomNavigation(position: Int) {
        _bottomNavigationList.value = _bottomNavigationList.value
            .mapIndexed { index, model -> model.copy(isSelected = index == position) }
            .toCollection(ArrayList())
    }

    //----------------------------------------------------------------------------------------------------------------------
    //  Item Nav / Layer
    suspend fun addValueToItemNavList() {
        _dataCustomize.value!!.layerList.forEachIndexed { index, layer ->
            if (index == 0) {
                _itemNavList.value.add(createListItem(layer, true))
            } else {
                _itemNavList.value.add(createListItem(layer))
            }
        }

    }

    suspend fun setFocusItemNavDefault() {
        //  FIX: Chỉ set selected cho tab đầu tiên (body tab - positionNavigation = 0)
        // Các tab khác giữ nguyên trạng thái mặc định (NONE được chọn từ createListItem)

        // Tab 0 (body): chọn item thứ 2 (index 1) vì item 0 là RANDOM button
        if (_itemNavList.value.isNotEmpty() && _itemNavList.value[0].size > 1) {
            _itemNavList.value[0].forEachIndexed { index, item ->
                item.isSelected = (index == 1) // Chọn item đầu tiên thật sự, bỏ qua RANDOM button
            }
        }

        // Các tab khác (1, 2, 3...): giữ nguyên NONE được chọn (đã set trong createListItem)
        // Không cần thay đổi gì vì createListItem đã đặt NONE (index 0) là selected = true
    }

    suspend fun setItemNavList(positionNavigation: Int, position: Int) {
        _itemNavList.value[positionNavigation] = _itemNavList.value[positionNavigation]
                .mapIndexed { index, models -> models.copy(isSelected = index == position) }
                .toCollection(ArrayList())
    }

    suspend fun setClickFillLayer(item: ItemNavCustomModel, position: Int): String {
        val path = item.path
        setKeySelected(positionNavSelected.value, path)

        // 🎯 LOG: Click item cho Dammy
        if (positionSelected == 1) {
            Log.d("CustomizeViewModel", "🖱️ DAMMY CLICK: pos=$position, colors=${item.listImageColor.size}, nav=${positionNavSelected.value}")
        }

        val pathSelected = if (item.listImageColor.isEmpty()) {
            _positionColorItemList.value[positionNavSelected.value] = 0
            if (positionSelected == 1) Log.d("CustomizeViewModel", "🎯 DAMMY: No colors")
            path
        } else {
            val currentColorIndex = positionColorItemList.value[positionNavSelected.value]
            val safeColorIndex = currentColorIndex.coerceIn(0, item.listImageColor.size - 1)

            if (currentColorIndex != safeColorIndex) {
                Log.w("CustomizeViewModel", "⚠️ DAMMY: Color index reset $currentColorIndex→$safeColorIndex")
                _positionColorItemList.value[positionNavSelected.value] = safeColorIndex
            }

            val colorPath = item.listImageColor[safeColorIndex].path
            if (positionSelected == 1) Log.d("CustomizeViewModel", "🎯 DAMMY: Color[$safeColorIndex]")
            colorPath
        }

        val pathIndex = getPathIndexForLayer(positionNavSelected.value)

        // 🔍 LOG CHI TIẾT: Debug pathIndex calculation
        Log.d("CustomizeViewModel", "📍 setClickFillLayer:")
        Log.d("CustomizeViewModel", "   positionSelected: $positionSelected")
        Log.d("CustomizeViewModel", "   positionNavSelected: ${positionNavSelected.value}")
        Log.d("CustomizeViewModel", "   pathIndex returned: $pathIndex")
        Log.d("CustomizeViewModel", "   pathSelected: ${pathSelected.substringAfterLast("/")}")

        if (positionSelected == 1) {
            Log.d("CustomizeViewModel", "💾 DAMMY SAVE: nav=${positionNavSelected.value}→pathIndex=$pathIndex")
        }

        // ✅ FIX: Xóa các layers có cùng positionCustom trước khi set layer mới
        clearLayersWithSamePositionCustom(positionNavSelected.value)

        // 🎯 FIX: Save với pathIndex đã được fix
        if (pathIndex != -1) {
            setPathSelected(pathIndex, pathSelected)
            Log.d("CustomizeViewModel", "✅ SAVED: pathSelectedList[$pathIndex] = ${pathSelected.substringAfterLast("/")}")
            if (positionSelected == 1) {
                Log.d("CustomizeViewModel", "✅ DAMMY SAVED: pathIndex=$pathIndex")
            }
        } else {
            Log.e("CustomizeViewModel", "❌ Cannot save - positionNav=${positionNavSelected.value} not found")
        }

        setIsSelectedItem(positionNavSelected.value)
        setItemNavList(_positionNavSelected.value, position)

        // ✅ FIX: Rebuild colorItemNavList từ item mới để giữ màu đã chọn
        if (item.listImageColor.isNotEmpty()) {
            val currentColorIndex = positionColorItemList.value[positionNavSelected.value]
            val safeColorIndex = currentColorIndex.coerceIn(0, item.listImageColor.size - 1)

            // ✅ REBUILD colorList từ item MỚI
            val newColorList = ArrayList<ItemColorModel>()
            item.listImageColor.forEachIndexed { index, colorItem ->
                newColorList.add(ItemColorModel(
                    color = colorItem.color,
                    isSelected = (index == safeColorIndex)
                ))
            }
            _colorItemNavList.value[positionNavSelected.value] = newColorList

            // ✅ Cập nhật positionColorItemList
            if (currentColorIndex != safeColorIndex) {
                _positionColorItemList.value[positionNavSelected.value] = safeColorIndex
            }

            Log.d("CustomizeViewModel", "🎨 Rebuilt colorItemNavList: ${newColorList.size} colors, selected=$safeColorIndex")
        } else {
            // Item không có màu → clear colorItemNavList
            _colorItemNavList.value[positionNavSelected.value] = arrayListOf()
        }

        return pathSelected
    }

    suspend fun setClickRandomLayer(): Pair<String, Boolean> {
        val positionStartLayer = if (positionNavSelected.value == 0) 1 else 2
        val randomLayer = if (positionNavSelected.value == 0) {
            if (itemNavList.value[positionNavSelected.value].size == 1) {
                1
            } else {
                (positionStartLayer..<itemNavList.value[positionNavSelected.value].size).random()
            }
        } else {
            (positionStartLayer..<itemNavList.value[positionNavSelected.value].size).random()
        }

        var randomColor: Int? = null
        var isMoreColors = false

        if (itemNavList.value[positionNavSelected.value][positionStartLayer].listImageColor.isNotEmpty()) {
            isMoreColors = true
            randomColor = (0..<(itemNavList.value[positionNavSelected.value][positionStartLayer].listImageColor.size)).random()
        }

        var pathRandom = itemNavList.value[positionNavSelected.value][randomLayer].path
        setKeySelected(positionNavSelected.value, pathRandom)

        if (!isMoreColors) {
            setPositionColorItem(positionNavSelected.value, 0)  // ✅ SỬA: positionNavSelected thay vì positionCustom
        } else {
            pathRandom = itemNavList.value[positionNavSelected.value][randomLayer].listImageColor[randomColor!!].path
            setPositionColorItem(positionNavSelected.value, randomColor)  // ✅ SỬA: positionNavSelected thay vì positionCustom
        }

        // ✅ AUTO-DETECT: Tự động chọn pathIndex phù hợp với data structure
        val pathIndex = getPathIndexForLayer(positionNavSelected.value)

        // ✅ FIX: Xóa các layers có cùng positionCustom trước khi set layer mới
        clearLayersWithSamePositionCustom(positionNavSelected.value)

        // 🎯 FIX: Chỉ save khi pathIndex hợp lệ
        if (pathIndex != -1) {
            setPathSelected(pathIndex, pathRandom)
        } else {
            Log.e("CustomizeViewModel", "❌ RANDOM: Cannot save - positionNav=${positionNavSelected.value} not found")
        }

        setItemNavList(_positionNavSelected.value, randomLayer)

        // ✅ FIX: Rebuild colorItemNavList từ item được random (giống logic setClickFillLayer)
        if (isMoreColors) {
            val randomItem = itemNavList.value[positionNavSelected.value][randomLayer]
            if (randomItem.listImageColor.isNotEmpty()) {
                val safeColorIndex = randomColor!!.coerceIn(0, randomItem.listImageColor.size - 1)

                // Rebuild colorList từ item MỚI
                val newColorList = ArrayList<ItemColorModel>()
                randomItem.listImageColor.forEachIndexed { index, colorItem ->
                    newColorList.add(ItemColorModel(
                        color = colorItem.color,
                        isSelected = (index == safeColorIndex)
                    ))
                }
                _colorItemNavList.value[positionNavSelected.value] = newColorList

                if (randomColor != safeColorIndex) {
                    _positionColorItemList.value[positionNavSelected.value] = safeColorIndex
                }

                Log.d("CustomizeViewModel", "🎲 RANDOM: Rebuilt colorItemNavList: ${newColorList.size} colors, selected=$safeColorIndex")
            }
        }

        return pathRandom to isMoreColors
    }
    suspend fun setClickRandomFullLayer(): Boolean {
//        countRandom++
//        val isOutTurn = if (countRandom == 5) true else false

        // ✅ FIX DUPLICATE POSITION CUSTOM:
        // Step 1: Tạo map để track positionCustom đã được random
        val layerList = _dataCustomize.value?.layerList ?: return false
        val positionCustomMap = mutableMapOf<Int, Int>() // positionCustom -> positionNavigation đã chọn

        val colorCode =
            if (colorListMost.value.isNotEmpty()) _colorListMost.value[(0..<colorListMost.value.size).random()] else "#123456"

        for (i in 0 until _bottomNavigationList.value.size) {
            val minSize = if (i == 0) 1 else 2
            if (_itemNavList.value[i].size <= minSize) {
                continue
            }

            val currentLayer = layerList[i]
            val currentPositionCustom = currentLayer.positionCustom

            // ✅ Kiểm tra xem positionCustom này đã được random chưa
            if (positionCustomMap.containsKey(currentPositionCustom)) {
                // Đã có layer khác cùng positionCustom được random → Skip layer này
                Log.d("CustomizeViewModel", "🧹 RANDOM ALL SKIP: positionNav=$i (positionCustom=$currentPositionCustom already assigned)")

                // Xóa path của layer này để tránh hiển thị duplicate
                _pathSelectedList.value[i] = ""
                _keySelectedItemList.value[i] = ""
                _isSelectedItemList.value[i] = false
                continue
            }

            val randomLayer = (minSize..<_itemNavList.value[i].size).random()

            var randomColor: Int = 0

            val isMoreColors = if (_itemNavList.value[i][minSize].listImageColor.isNotEmpty()) {
                randomColor =
                    _itemNavList.value[i][randomLayer].listImageColor.indexOfFirst { it.color == colorCode }
                if (randomColor == -1) {
                    randomColor = (0..<_itemNavList.value[i][minSize].listImageColor.size).random()
                }
                true
            } else {
                false
            }
            _keySelectedItemList.value[i] =
                _itemNavList.value[i][randomLayer].path

            val pathItem = if (!isMoreColors) {
                _positionColorItemList.value[i] = 0
                _itemNavList.value[i][randomLayer].path
            } else {
                _positionColorItemList.value[i] = randomColor
                _itemNavList.value[i][randomLayer].listImageColor[randomColor].path
            }

            // ✅ FIX: Mỗi layer dùng index riêng = i (vị trí trong loop)
            _pathSelectedList.value[i] = pathItem

            // ✅ Đánh dấu positionCustom này đã được random
            positionCustomMap[currentPositionCustom] = i
            Log.d("CustomizeViewModel", "✅ RANDOM ALL SET: positionNav=$i (positionCustom=$currentPositionCustom)")

            setItemNavList(i, randomLayer)

            // ✅ FIX: Rebuild colorItemNavList từ item được random (giống logic setClickFillLayer)
            if (isMoreColors) {
                val randomItem = _itemNavList.value[i][randomLayer]
                if (randomItem.listImageColor.isNotEmpty()) {
                    val safeColorIndex = randomColor.coerceIn(0, randomItem.listImageColor.size - 1)

                    // Rebuild colorList từ item MỚI
                    val newColorList = ArrayList<ItemColorModel>()
                    randomItem.listImageColor.forEachIndexed { index, colorItem ->
                        newColorList.add(ItemColorModel(
                            color = colorItem.color,
                            isSelected = (index == safeColorIndex)
                        ))
                    }
                    _colorItemNavList.value[i] = newColorList

                    if (randomColor != safeColorIndex) {
                        _positionColorItemList.value[i] = safeColorIndex
                    }

                    Log.d("CustomizeViewModel", "🎲 RANDOM ALL: Rebuilt colorItemNavList[$i]: ${newColorList.size} colors, selected=$safeColorIndex")
                }
            }
        }
        return false
    }

    suspend fun setClickReset(): String {
        Log.d("CustomizeViewModel", "🔄 ════════════════════════════════════")
        Log.d("CustomizeViewModel", "🔄 RESET START - Character $positionSelected")
        Log.d("CustomizeViewModel", "🔄 ════════════════════════════════════")

        resetDataList()
        _bottomNavigationList.value.forEachIndexed { index, model ->
            val positionSelected = if (index == 0) 1 else 0
            setItemNavList(index, positionSelected)

            // ✅ FIX: Lấy màu trực tiếp từ data gốc (_dataCustomize) thay vì từ itemNavList
            val layerData = _dataCustomize.value?.layerList?.getOrNull(index)
            if (layerData != null) {
                // Lấy item default: layer 0 → item[1], các layer khác → item[0]
                val defaultItemFromSource = layerData.layer.getOrNull(positionSelected)

                Log.d("CustomizeViewModel", "🔄 Layer[$index]: posNav=${layerData.positionNavigation}, posCus=${layerData.positionCustom}, itemSelected=$positionSelected")
                Log.d("CustomizeViewModel", "   └─ Item path: ${defaultItemFromSource?.image?.substringAfterLast("/") ?: "null"}")
                Log.d("CustomizeViewModel", "   └─ isMoreColors: ${defaultItemFromSource?.isMoreColors}")
                Log.d("CustomizeViewModel", "   └─ listColor.size: ${defaultItemFromSource?.listColor?.size}")

                if (defaultItemFromSource != null && defaultItemFromSource.isMoreColors && defaultItemFromSource.listColor.isNotEmpty()) {
                    val newColorList = ArrayList<ItemColorModel>()
                    defaultItemFromSource.listColor.forEachIndexed { colorIndex, colorItem ->
                        newColorList.add(ItemColorModel(
                            color = colorItem.color,
                            isSelected = (colorIndex == 0)  // Reset về màu đầu tiên
                        ))
                    }
                    _colorItemNavList.value[index] = newColorList
                    _positionColorItemList.value[index] = 0

                    Log.d("CustomizeViewModel", "   └─ ✅ Rebuilt ${newColorList.size} colors (first: ${newColorList.firstOrNull()?.color})")
                } else {
                    _colorItemNavList.value[index] = arrayListOf()
                    Log.d("CustomizeViewModel", "   └─ ⚠️ No colors for this layer")
                }
            } else {
                _colorItemNavList.value[index] = arrayListOf()
                Log.e("CustomizeViewModel", "   └─ ❌ Layer data not found!")
            }
        }
        val pathDefault = _dataCustomize.value!!.layerList.first().layer.first().image

        // ✅ FIX: Body layer (first layer) lưu vào index 0
        _pathSelectedList.value[0] = pathDefault
        _keySelectedItemList.value[_dataCustomize.value!!.layerList.first().positionNavigation] = pathDefault
        _isSelectedItemList.value[_dataCustomize.value!!.layerList.first().positionNavigation] = true

        Log.d("CustomizeViewModel", "🔄 ════════════════════════════════════")
        Log.d("CustomizeViewModel", "🔄 RESET COMPLETE")
        Log.d("CustomizeViewModel", "🔄 Total colorItemNavList sizes: ${_colorItemNavList.value.map { it.size }}")
        Log.d("CustomizeViewModel", "🔄 ════════════════════════════════════")

        return pathDefault
    }


    //----------------------------------------------------------------------------------------------------------------------
// Color
    suspend fun setItemColorDefault() {
        for (i in 0 until _dataCustomize.value!!.layerList.size) {
            // Lấy đối tượng LayerModel đầu tiên trong danh sách con
            val currentLayer = _dataCustomize.value!!.layerList[i].layer.first()
            var firstIndex = true
            // Kiểm tra isMoreColors để thêm màu hoặc danh sách rỗng
            if (currentLayer.isMoreColors) {
                val colorList = ArrayList<ItemColorModel>()
                for (j in 0 until currentLayer.listColor.size) {
                    val color = currentLayer.listColor[j].color
                    if (firstIndex) {
                        colorList.add(ItemColorModel(color, true))
                    } else {
                        colorList.add(ItemColorModel(color))
                    }
                    firstIndex = false
                }
                _colorItemNavList.value.add(colorList)
            } else {
                _colorItemNavList.value.add(arrayListOf())
            }
        }
        val getAllColor = ArrayList<String>()
        _itemNavList.value.forEachIndexed { index, nav ->
            val position = if (index != 0) 2 else 1
            val itemNav = nav[position]
            itemNav.listImageColor.forEach { colorList ->
                getAllColor.add(colorList.color)
            }
        }
        setColorListMost(
            getAllColor
            .groupingBy { it }
            .eachCount()
            .filter { it.value > 3 }.keys
            .toCollection(ArrayList())
        )
    }

    suspend fun setColorItemNav(positionNavSelected: Int, position: Int) {
        _colorItemNavList.value[positionNavSelected] = _colorItemNavList.value[positionNavSelected]
            .mapIndexed { index, models -> models.copy(isSelected = index == position) }
            .toCollection(ArrayList())
    }

    suspend fun setClickChangeColor(position: Int): String {
        var pathColor = ""
        _positionColorItemList.value[positionNavSelected.value] = position
        // Đã chọn hình ảnh chưa
        if (_keySelectedItemList.value[positionNavSelected.value] != "") {
            // Duyệt qua từng item trong bộ phận
            for (item in _dataCustomize.value!!.layerList[positionNavSelected.value].layer) {
                if (item.image == _keySelectedItemList.value[positionNavSelected.value]) {
                    // ✅ FIX: Add bounds checking
                    if (position >= 0 && position < item.listColor.size) {
                        pathColor = item.listColor[position].path

                        // ✅ AUTO-DETECT: Tự động chọn pathIndex phù hợp với data structure
                        val pathIndex = getPathIndexForLayer(positionNavSelected.value)

                        // 🎯 FIX: Chỉ save khi pathIndex hợp lệ
                        if (pathIndex != -1) {
                            _pathSelectedList.value[pathIndex] = pathColor
                        } else {
                            Log.e("CustomizeViewModel", "❌ COLOR: Cannot save - positionNav=${positionNavSelected.value} not found")
                        }
                    } else {
                        android.util.Log.e("CustomizeViewModel", "❌ Color position out of bounds: $position, list size: ${item.listColor.size}")
                    }
                }
            }
        }
        setColorItemNav(positionNavSelected.value, position)
        return pathColor
    }

//----------------------------------------------------------------------------------------------------------------------
// Extension other

    suspend fun setImageViewList(frameLayout: FrameLayout) {
        // 🔧 HARDFIX Character 1: Tạo ImageView riêng cho Layer[24] (đặt đầu tiên - dưới cùng)
        if (positionSelected == 1) {
            val layer24ImageView = ImageView(frameLayout.context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            frameLayout.addView(layer24ImageView, 0)  // Thêm vào index 0 (dưới cùng, z-index thấp nhất)
            _layer24ImageView.value = layer24ImageView
            Log.d("CustomizeViewModel", "🔧 HARDFIX Miley: Created Layer24ImageView at index 0")
        }

        // ✅ FIX: Tạo ImageView riêng cho Body layer (đặt sau Layer24)
        val bodyImageView = ImageView(frameLayout.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        frameLayout.addView(bodyImageView)  // Thêm sau Layer24 (z-index cao hơn Layer24)
        _bodyImageView.value = bodyImageView

        // Tạo các ImageView cho các layer khác
        _imageViewList.value.addAll(addImageViewToLayout(_dataCustomize.value!!.layerList.size, frameLayout))
    }

    fun addImageViewToLayout(quantityLayer: Int, frameLayout: FrameLayout): ArrayList<ImageView> {
        val imageViewList = ArrayList<ImageView>()
        for (i in 0 until quantityLayer) {
            val imageView = ImageView(frameLayout.context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
                )
            }
            frameLayout.addView(imageView)
            imageViewList.add(imageView)
        }
        return imageViewList
    }

    fun createListItem(layers: LayerListModel, isBody: Boolean = false): ArrayList<ItemNavCustomModel> {
        val listItem = arrayListOf<ItemNavCustomModel>()
        val positionCustom = layers.positionCustom
        val positionNavigation = layers.positionNavigation
        if (isBody) {
            listItem.add(
                ItemNavCustomModel(
                    path = AssetsKey.RANDOM_LAYER,
                    positionCustom = positionCustom,
                    positionNavigation = positionNavigation
                )
            )
        } else {
            listItem.add(
                ItemNavCustomModel(
                    path = AssetsKey.NONE_LAYER,
                    positionCustom = positionCustom,
                    positionNavigation = positionNavigation,
                    isSelected = true
                )
            )
            listItem.add(
                ItemNavCustomModel(
                    path = AssetsKey.RANDOM_LAYER,
                    positionCustom = positionCustom,
                    positionNavigation = positionNavigation,
                )
            )
        }
        for (layer in layers.layer) {
            if (!layer.isMoreColors) {
                listItem.add(
                    ItemNavCustomModel(
                        path = layer.image,
                        positionCustom = positionCustom,
                        positionNavigation = positionNavigation
                    )
                )
            } else {
                val listItemColor = ArrayList<ItemColorImageModel>()

                for (colorModel in layer.listColor) {
                    listItemColor.add(
                        ItemColorImageModel(
                            color = colorModel.color,
                            path = colorModel.path
                        )
                    )
                }
                listItem.add(
                    ItemNavCustomModel(
                        path = layer.image,
                        positionCustom = positionCustom,
                        positionNavigation = positionNavigation,
                        isSelected = false,
                        listImageColor = listItemColor,
                    )
                )
            }
        }
        return listItem
    }

    fun saveImageFromView(context: Context, view: View): Flow<SaveState> = flow {
        emit(SaveState.Loading)
        val bitmap = BitmapHelper.createBimapFromView(view)
        MediaHelper.saveBitmapToInternalStorage(context, ValueKey.DOWNLOAD_ALBUM_BACKGROUND, bitmap).collect { state ->
            emit(state)
        }
    }.flowOn(Dispatchers.IO)

    fun checkDataInternet(context: Activity, action: (() -> Unit)) {
        if (!_isDataAPI.value) {
            action.invoke()
            return
        }
        InternetHelper.checkInternet(context) { result ->
            if (result == HandleState.SUCCESS) {
                action.invoke()
            } else {
                context.showToast(R.string.please_check_your_internet)
            }
        }
    }

    suspend fun resetDataList() {
        val quantityLayer = _dataCustomize.value!!.layerList.size
        val positionColorItemList = ArrayList<Int>(quantityLayer)
        val isSelectedItemList = ArrayList<Boolean>(quantityLayer)
        val keySelectedItemList = ArrayList<String>(quantityLayer)
        val isShowColorList = ArrayList<Boolean>(quantityLayer)

        // ✅ FIX: pathSelectedList size = số lượng layers
        // Mỗi layer có 1 slot riêng: Layer 0 → [0], Layer 1 → [1], ..., Layer 24 → [24]
        val pathSelectedList = ArrayList<String>(quantityLayer)

        repeat(quantityLayer) {
            positionColorItemList.add(0)
            isSelectedItemList.add(false)
            keySelectedItemList.add("")
            isShowColorList.add(true)
            pathSelectedList.add("")
        }

        setPositionColorItemList(positionColorItemList)
        setIsSelectedItemList(isSelectedItemList)
        setKeySelectedItemList(keySelectedItemList)
        setIsShowColorList(isShowColorList)
        setPathSelectedList(pathSelectedList)
    }

    //----------------------------------------------------------------------------------------------------------------------

    /**
     * 📊 REPORT: Tạo báo cáo API mismatch để gửi Backend
     */
    fun generateApiMismatchReport(): String {
        val report = StringBuilder()
        report.appendLine("🐛 API MISMATCH REPORT")
        report.appendLine("=".repeat(50))
        report.appendLine("Character: ${_dataCustomize.value?.dataName ?: "Unknown"}")
        report.appendLine("Timestamp: ${System.currentTimeMillis()}")
        report.appendLine()

        val preset = _suggestionState.value
        if (preset != null) {
            report.appendLine("📋 SUGGESTION DATA:")
            preset.layerSelections.forEach { (storageKey, selection) ->
                report.appendLine("  StorageKey: $storageKey")
                report.appendLine("  ItemIndex: ${selection.itemIndex}")
                report.appendLine("  ColorIndex: ${selection.colorIndex}")
                report.appendLine("  Path: ${selection.path}")

                // 🎯 Highlight Miley
                if (storageKey == 20 || selection.path.contains("miley", ignoreCase = true)) {
                    report.appendLine("  🎯 THIS IS MILEY!")
                }
                report.appendLine()
            }
        }

        report.appendLine("📋 CUSTOMIZE DATA:")
        _dataCustomize.value?.layerList?.forEachIndexed { index, layer ->
            report.appendLine("  Layer $index:")
            report.appendLine("    PositionNav: ${layer.positionNavigation}")
            report.appendLine("    PositionCustom: ${layer.positionCustom}")
            report.appendLine("    Items Count: ${layer.layer.size}")

            // 🎯 Chi tiết đầy đủ cho Miley
            if (layer.positionNavigation == 20) {
                report.appendLine("    🎯 MILEY LAYER - ALL ITEMS:")
                layer.layer.forEachIndexed { itemIdx, item ->
                    report.appendLine("      [$itemIdx]: ${item.image}")
                }
            } else {
                report.appendLine("    Items: ${layer.layer.take(3).map { it.image.substringAfterLast("/") }}")
            }
            report.appendLine()
        }

        return report.toString()
    }

    /**
     * 💾 EXPORT: Lưu báo cáo ra file để gửi Backend
     */
    fun saveReportToFile(context: android.content.Context) {
        try {
            val report = generateApiMismatchReport()
            val fileName = "api_mismatch_${System.currentTimeMillis()}.txt"
            val file = java.io.File(context.getExternalFilesDir(null), fileName)
            file.writeText(report)
            Log.i("CustomizeViewModel", "📁 Report saved: ${file.absolutePath}")
        } catch (e: Exception) {
            Log.e("CustomizeViewModel", "❌ Failed to save report: ${e.message}")
        }
    }

    /**
     * ✅ HELPER: Trả về pathIndex dựa trên layerIndex
     * Logic đúng (theo Miley): pathIndex = layerIndex (KHÔNG phải positionCustom)
     * - pathSelectedList[layerIndex] = path của layer đó
     * - Render vào ImageView[positionCustom]
     */
    fun getPathIndexForLayer(positionNavigation: Int): Int {
        // 🔍 LOG: Entry point
        Log.d("CustomizeViewModel", "🔍 getPathIndexForLayer($positionNavigation)")
        Log.d("CustomizeViewModel", "   positionSelected: $positionSelected")

        // ✅ PERFORMANCE: Use cached mapping instead of linear search
        val cache = _layerIndexCache.value
        val layerList = _dataCustomize.value?.layerList ?: return 0

        // 🎯 FIX CỨNG: Đối với Character 1 & 2 (Miley & Dammy), Layer[21] có posNav=20 trong data
        // → Fix cứng: khi request posNav=21, trả về Layer[21]
        if ((positionSelected == 1 || positionSelected == 2) && positionNavigation == 21) {
            Log.d("CustomizeViewModel", "   ✅ HARDFIX triggered for Character $positionSelected, posNav=21")
            // Tìm Layer[21] (index 21 trong layerList)
            if (layerList.size > 21) {
                Log.d("CustomizeViewModel", "🔧 HARDFIX Character $positionSelected: posNav=21 → Layer[21] (actual posNav=${layerList[21].positionNavigation}, posCus=${layerList[21].positionCustom})")
                Log.d("CustomizeViewModel", "   → Returning: 21")
                return 21  // Trả về layerIndex = 21
            } else {
                Log.e("CustomizeViewModel", "❌ HARDFIX failed: Layer[21] not found in layerList")
                return -1
            }
        } else {
            Log.d("CustomizeViewModel", "   ⏩ HARDFIX skipped (positionSelected=$positionSelected, posNav=$positionNavigation)")
        }

        // 🎯 FIX: Map positionNav bị lỗi sang positionNav đúng
        val actualPositionNav = when (positionNavigation) {
            21 -> {
                // Fallback: nếu không phải character 1, tìm layer có positionCustom=22
                val layer22 = layerList.find { it.positionCustom == 22 }
                if (layer22 != null) {
                    val layer22Index = layerList.indexOf(layer22)
                    return layer22Index  // Dùng layerIndex thay vì positionNav
                } else {
                    return -1
                }
            }
            else -> positionNavigation
        }

        // ✅ Use cache for O(1) lookup instead of O(n) search
        val layerIndex = cache[actualPositionNav] ?: -1
        if (layerIndex == -1) {
            return -1
        }

        // ✅ LUÔN dùng layerIndex (theo logic của Miley)
        // Vì Miley có positionCustom trùng nhau (Layer 0 và Layer 2 đều có positionCustom=1)
        // Nên pathIndex PHẢI = layerIndex để tránh conflict
        return layerIndex
    }

}