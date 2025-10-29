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

    private val _colorListMost = MutableStateFlow(arrayListOf<String>())
    val colorListMost = _colorListMost.asStateFlow()

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

        Log.d("CustomizeViewModel", "========================================")
        Log.d("CustomizeViewModel", "📊 APPLYING SUGGESTION PRESET")
        Log.d("CustomizeViewModel", "========================================")
        Log.d("CustomizeViewModel", "Preset has ${preset.layerSelections.size} layer selections")

        // ✅ LOG: In ra toàn bộ preset data
        preset.layerSelections.forEach { (key, sel) ->
            Log.d("CustomizeViewModel", "Preset layer key=$key: itemIndex=${sel.itemIndex}, colorIndex=${sel.colorIndex}, path=${sel.path}")
        }
        Log.d("CustomizeViewModel", "")

        // Apply each layer selection
        preset.layerSelections.forEach { (storageKey, selection) ->
            Log.d("CustomizeViewModel", "--- Processing storageKey=$storageKey ---")

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

            Log.d("CustomizeViewModel", "Found layer: index=$layerIndex, positionNav=${layer.positionNavigation}, positionCustom=${layer.positionCustom}")

            // Validate item index
            if (selection.itemIndex >= layer.layer.size) {
                Log.e("CustomizeViewModel", "❌ Invalid item index ${selection.itemIndex} for layer positionCustom=${layer.positionCustom} (storageKey=$storageKey)")
                return@forEach
            }

            val item = layer.layer[selection.itemIndex]
            Log.d("CustomizeViewModel", "Selected item: index=${selection.itemIndex}, path=${item.image}, isMoreColors=${item.isMoreColors}, colors=${item.listColor.size}")

            // ✅ FIX: Dùng layerIndex đã tính ở dòng 238
            _pathSelectedList.value[layerIndex] = selection.path
            _keySelectedItemList.value[layer.positionNavigation] = selection.path
            Log.d("CustomizeViewModel", "Set pathSelectedList[$layerIndex] = ${selection.path} (positionNav=${layer.positionNavigation}, positionCustom=${layer.positionCustom})")
            Log.d("CustomizeViewModel", "Set keySelectedItemList[${layer.positionNavigation}] = ${selection.path}")

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

            // ✅ CRITICAL FIX: Convert data model index → RecyclerView index
            // RecyclerView có None/Random buttons ở đầu, cần cộng offset:
            // - Layer đầu (Body): chỉ có Random button → offset +1
            // - Các layer khác: có None + Random buttons → offset +2
            val rcvIndex = if (layerIndex == 0) {
                selection.itemIndex + 1  // Body layer: Random button at index 0
            } else {
                selection.itemIndex + 2  // Other layers: None(0) + Random(1)
            }

            setItemNavList(layer.positionNavigation, rcvIndex)

            Log.d("CustomizeViewModel", "✅ Applied layer storageKey=$storageKey → positionCustom=${layer.positionCustom}, positionNav=${layer.positionNavigation}: dataIndex=${selection.itemIndex} → rcvIndex=$rcvIndex, color=${selection.colorIndex}")
        }

        // ✅ Set initial navigation to body layer (first layer)
        val firstLayer = _dataCustomize.value?.layerList?.firstOrNull()
        if (firstLayer != null) {
            setPositionCustom(firstLayer.positionCustom)
            setPositionNavSelected(firstLayer.positionNavigation)
            Log.d("CustomizeViewModel", "Set initial position to body layer: positionCustom=${firstLayer.positionCustom}, positionNav=${firstLayer.positionNavigation}")
        }

        Log.d("CustomizeViewModel", "Suggestion preset applied successfully")
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
        // ✅ FIX: Chỉ set selected cho tab đầu tiên (body tab - positionNavigation = 0)
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

        // ✅ LOG: Log chi tiết khi click item
        if (positionSelected == 1) {
            Log.d("CustomizeViewModel", "========================================")
            Log.d("CustomizeViewModel", "🖱️ setClickFillLayer - MILEY")
            Log.d("CustomizeViewModel", "========================================")
            Log.d("CustomizeViewModel", "Item position: $position")
            Log.d("CustomizeViewModel", "Item path: $path")
            Log.d("CustomizeViewModel", "Item colors count: ${item.listImageColor.size}")
            Log.d("CustomizeViewModel", "positionNavSelected: ${positionNavSelected.value}")
            Log.d("CustomizeViewModel", "positionCustom: ${positionCustom.value}")
        }

        val pathSelected = if (item.listImageColor.isEmpty()) {
            // ✅ No colors - reset color index to 0
            _positionColorItemList.value[positionNavSelected.value] = 0
            if (positionSelected == 1) {
                Log.d("CustomizeViewModel", "No colors - using base path: $path")
            }
            path
        } else {
            // ✅ FIX: Reset color index if current item has fewer colors than previous
            val currentColorIndex = positionColorItemList.value[positionNavSelected.value]
            val safeColorIndex = currentColorIndex.coerceIn(0, item.listImageColor.size - 1)

            // ✅ Log if index was out of bounds
            if (currentColorIndex != safeColorIndex) {
                android.util.Log.w("CustomizeViewModel", "⚠️ Color index out of bounds: $currentColorIndex, list size: ${item.listImageColor.size}, reset to: $safeColorIndex")
                // Reset to safe index
                _positionColorItemList.value[positionNavSelected.value] = safeColorIndex
            }

            val colorPath = item.listImageColor[safeColorIndex].path
            if (positionSelected == 1) {
                Log.d("CustomizeViewModel", "Has colors - using color[$safeColorIndex]: $colorPath")
            }
            colorPath
        }

        if (positionSelected == 1) {
            Log.d("CustomizeViewModel", "✅ Final pathSelected: $pathSelected")
        }

        // ✅ FIX: Mỗi layer dùng index riêng = vị trí trong layerList
        val layerIndex = _dataCustomize.value!!.layerList.indexOfFirst { it.positionNavigation == positionNavSelected.value }

        if (positionSelected == 1) {
            Log.d("CustomizeViewModel", "💾 SAVING PATH:")
            Log.d("CustomizeViewModel", "positionNavSelected=${positionNavSelected.value}, positionCustom=${positionCustom.value}")
            Log.d("CustomizeViewModel", "→ layerIndex=$layerIndex")
            Log.d("CustomizeViewModel", "→ pathSelectedList[$layerIndex] = $pathSelected")
            Log.d("CustomizeViewModel", "========================================")
        }

        setPathSelected(layerIndex, pathSelected)

        setIsSelectedItem(positionNavSelected.value)
        setItemNavList(_positionNavSelected.value, position)
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

        // ✅ FIX: Mỗi layer dùng index riêng = vị trí trong layerList
        val layerIndex = _dataCustomize.value!!.layerList.indexOfFirst { it.positionNavigation == positionNavSelected.value }
        setPathSelected(layerIndex, pathRandom)

        setItemNavList(_positionNavSelected.value, randomLayer)
        if (isMoreColors) {
            setColorItemNav(positionNavSelected.value, randomColor!!)
        }
        return pathRandom to isMoreColors
    }
    suspend fun setClickRandomFullLayer(): Boolean {
//        countRandom++
//        val isOutTurn = if (countRandom == 5) true else false

        val colorCode =
            if (colorListMost.value.isNotEmpty()) _colorListMost.value[(0..<colorListMost.value.size).random()] else "#123456"
        for (i in 0 until _bottomNavigationList.value.size) {
            val minSize = if (i == 0) 1 else 2
            if (_itemNavList.value[i].size <= minSize) {
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

            setItemNavList(i, randomLayer)
            if (isMoreColors) {
                setColorItemNav(i, randomColor)
            }
        }
        return false
    }

    suspend fun setClickReset(): String {
        resetDataList()
        _bottomNavigationList.value.forEachIndexed { index, model ->
            val positionSelected = if (index == 0) 1 else 0
            setItemNavList(index, positionSelected)
            setColorItemNav(index, 0)
        }
        val pathDefault = _dataCustomize.value!!.layerList.first().layer.first().image

        // ✅ FIX: Body layer (first layer) lưu vào index 0
        _pathSelectedList.value[0] = pathDefault
        _keySelectedItemList.value[_dataCustomize.value!!.layerList.first().positionNavigation] = pathDefault
        _isSelectedItemList.value[_dataCustomize.value!!.layerList.first().positionNavigation] = true
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

                        // ✅ FIX: Mỗi layer dùng index riêng = vị trí trong layerList
                        val layerIndex = _dataCustomize.value!!.layerList.indexOfFirst { it.positionNavigation == positionNavSelected.value }
                        _pathSelectedList.value[layerIndex] = pathColor
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
        // ✅ FIX: Tạo ImageView riêng cho Body layer (index 0, đặt đầu tiên)
        val bodyImageView = ImageView(frameLayout.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        frameLayout.addView(bodyImageView, 0)  // Thêm vào index 0 (dưới cùng)
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

}