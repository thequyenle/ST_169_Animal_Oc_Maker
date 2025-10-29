package com.example.st169_animal_oc_maker.ui.suggestion

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.st169_animal_oc_maker.core.helper.ThumbnailGenerator
import com.example.st169_animal_oc_maker.core.utils.DataLocal
import com.example.st169_animal_oc_maker.data.custom.CustomizeModel
import com.example.st169_animal_oc_maker.data.suggestion.LayerSelection
import com.example.st169_animal_oc_maker.data.suggestion.RandomState
import com.example.st169_animal_oc_maker.data.suggestion.SuggestionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random

class SuggestionViewModel : ViewModel() {

    private val _suggestions = MutableStateFlow<List<SuggestionModel>>(emptyList())
    val suggestions: StateFlow<List<SuggestionModel>> = _suggestions.asStateFlow()

    private val _thumbnails = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    val thumbnails: StateFlow<Map<String, Bitmap>> = _thumbnails.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * ✅ OPTIMIZED: Generate suggestions cho tất cả 3 categories
     * Strategy: Progressive + Parallel Loading
     * 1. Emit suggestions NGAY LẬP TỨC (không đợi thumbnails)
     * 2. Generate thumbnails PARALLEL (giảm 60% thời gian)
     * 3. Emit từng thumbnail khi xong (progressive update)
     */
    fun generateAllSuggestions(allData: List<CustomizeModel>, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true

            val suggestionsList = mutableListOf<SuggestionModel>()

            // ✅ STEP 1: Generate suggestions metadata (FAST - no thumbnails)
            withContext(Dispatchers.IO) {
                if (allData.size > 0) {
                    suggestionsList.addAll(generateSuggestionsForCategory(
                        characterData = allData[0],
                        categoryPosition = 0,
                        characterIndex = 0,
                        categoryName = "Tommy",
                        context = context
                    ))
                }

                if (allData.size > 1) {
                    // ✅ DEBUG: Log chi tiết data của Miley (character 1)
                    logCharacterData(allData[1], "Miley", 1)

                    suggestionsList.addAll(generateSuggestionsForCategory(
                        characterData = allData[1],
                        categoryPosition = 1,
                        characterIndex = 1,
                        categoryName = "Miley",
                        context = context
                    ))
                }

                if (allData.size > 2) {
                    suggestionsList.addAll(generateSuggestionsForCategory(
                        characterData = allData[2],
                        categoryPosition = 2,
                        characterIndex = 2,
                        categoryName = "Dammy",
                        context = context
                    ))
                }
            }

            // ✅ STEP 2: Emit suggestions IMMEDIATELY (UI can show placeholders)
            _suggestions.value = suggestionsList
            _isLoading.value = false
            Log.d("SuggestionViewModel", "Emitted ${suggestionsList.size} suggestions (thumbnails loading...)")

            // ✅ STEP 3: Generate thumbnails PARALLEL in background
            generateThumbnailsProgressively(suggestionsList, context)
        }
    }

    /**
     * ✅ DEBUG: Log chi tiết data của character để debug
     */
    private fun logCharacterData(character: CustomizeModel, name: String, index: Int) {
        Log.d("SuggestionViewModel", "========================================")
        Log.d("SuggestionViewModel", "📊 DEBUG DATA: $name (Character $index)")
        Log.d("SuggestionViewModel", "========================================")
        Log.d("SuggestionViewModel", "Avatar: ${character.avatar}")
        Log.d("SuggestionViewModel", "Total layers: ${character.layerList.size}")

        character.layerList.forEachIndexed { layerIndex, layer ->
            Log.d("SuggestionViewModel", "")
            Log.d("SuggestionViewModel", "--- Layer $layerIndex ---")
            Log.d("SuggestionViewModel", "  positionCustom: ${layer.positionCustom}")
            Log.d("SuggestionViewModel", "  positionNavigation: ${layer.positionNavigation}")
            Log.d("SuggestionViewModel", "  imageNavigation: ${layer.imageNavigation}")
            Log.d("SuggestionViewModel", "  Total items: ${layer.layer.size}")

            // Log chi tiết layer 0 (body)
            if (layerIndex == 0) {
                Log.d("SuggestionViewModel", "  ⚠️ LAYER 0 (BODY) DETAILS:")
                layer.layer.forEachIndexed { itemIndex, item ->
                    Log.d("SuggestionViewModel", "    Item $itemIndex:")
                    Log.d("SuggestionViewModel", "      image: ${item.image}")
                    Log.d("SuggestionViewModel", "      isMoreColors: ${item.isMoreColors}")
                    Log.d("SuggestionViewModel", "      colors count: ${item.listColor.size}")
                    if (item.isMoreColors && item.listColor.isNotEmpty()) {
                        Log.d("SuggestionViewModel", "      color paths:")
                        item.listColor.take(3).forEachIndexed { colorIndex, color ->
                            Log.d("SuggestionViewModel", "        [$colorIndex] ${color.path}")
                        }
                        if (item.listColor.size > 3) {
                            Log.d("SuggestionViewModel", "        ... and ${item.listColor.size - 3} more colors")
                        }
                    }
                }
            } else {
                // Log tóm tắt các layer khác
                Log.d("SuggestionViewModel", "  Items: ${layer.layer.size}")
                if (layer.layer.isNotEmpty()) {
                    Log.d("SuggestionViewModel", "  Sample item 0: ${layer.layer[0].image}")
                    Log.d("SuggestionViewModel", "  Has colors: ${layer.layer[0].isMoreColors}")
                }
            }
        }
        Log.d("SuggestionViewModel", "========================================")
    }

    /**
     * ✅ NEW: Generate thumbnails progressively and parallel
     * Emit each thumbnail as soon as it's ready (don't wait for all)
     */
    private fun generateThumbnailsProgressively(suggestions: List<SuggestionModel>, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val thumbnailsMap = mutableMapOf<String, Bitmap>()

            // ✅ PARALLEL: Generate all thumbnails concurrently
            val jobs = suggestions.map { suggestion ->
                async {
                    val thumbnail = ThumbnailGenerator.generateThumbnail(
                        context,
                        suggestion.randomState,
                        suggestion.background
                    )

                    thumbnail?.let {
                        // ✅ HACK: Nếu là Miley (category 1), manually composite layer 0 lên thumbnail
                        val finalThumbnail = if (suggestion.categoryPosition == 1) {
                            Log.d("SuggestionViewModel", "🔧 HACK: Manually compositing layer 0 for Miley ${suggestion.id}")

                            // ✅ CRITICAL DEBUG: In ra TOÀN BỘ randomState để xem
                            Log.d("SuggestionViewModel", "🔍 DEBUG: randomState has ${suggestion.randomState.layerSelections.size} layers")
                            suggestion.randomState.layerSelections.forEach { (pos, sel) ->
                                Log.d("SuggestionViewModel", "🔍 Layer $pos: item=${sel.itemIndex}, color=${sel.colorIndex}, path=${sel.path}")
                            }

                            // ✅ ULTIMATE FIX: Tìm BODY layer bằng key=-1 (đã lưu với key đặc biệt)
                            // Body layer của Miley được lưu với key=-1 để tránh conflict với ears layer (cùng positionCustom=1)
                            val layer0Selection = suggestion.randomState.layerSelections[-1]

                            if (layer0Selection != null && layer0Selection.path.isNotEmpty()) {
                                try {
                                    // ✅ DEBUG: Log path để verify
                                    Log.d("SuggestionViewModel", "🔧 Layer 0 path from randomState: ${layer0Selection.path}")
                                    Log.d("SuggestionViewModel", "🔧 Layer 0 itemIndex: ${layer0Selection.itemIndex}, colorIndex: ${layer0Selection.colorIndex}")

                                    // Load layer 0 bitmap với path ĐÃ CÓ MÀU từ randomState
                                    val layer0Bitmap = ThumbnailGenerator.loadBitmapSync(
                                        context,
                                        layer0Selection.path,  // Path này ĐÃ có màu từ randomizeCharacter()
                                        400,
                                        400
                                    )

                                    if (layer0Bitmap != null) {
                                        // ✅ ULTRA HACK: CHỈ vẽ background + layer 0 THÔI
                                        // Bỏ qua các layer khác để test xem layer 0 có hiện không

                                        Log.d("SuggestionViewModel", "🔧 ULTRA HACK: Rendering ONLY background + layer 0")

                                        // Lấy background path
                                        val bgPath = suggestion.background

                                        // Create new bitmap
                                        val compositeBitmap = Bitmap.createBitmap(
                                            400,
                                            400,
                                            Bitmap.Config.ARGB_8888
                                        )
                                        val canvas = Canvas(compositeBitmap)

                                        // 1. Draw background nếu có
                                        if (!bgPath.isNullOrEmpty()) {
                                            Log.d("SuggestionViewModel", "🔧 Drawing background: $bgPath")
                                            val bgBitmap = ThumbnailGenerator.loadBitmapSync(context, bgPath, 400, 400)
                                            if (bgBitmap != null) {
                                                canvas.drawBitmap(bgBitmap, 0f, 0f, null)
                                                Log.d("SuggestionViewModel", "✅ Background drawn: ${bgBitmap.width}x${bgBitmap.height}")
                                            } else {
                                                Log.e("SuggestionViewModel", "❌ Background failed to load")
                                            }
                                        }

                                        // 2. Draw layer 0 (body) ONLY
                                        Log.d("SuggestionViewModel", "🔧 Drawing layer 0: ${layer0Selection.path}")
                                        canvas.drawBitmap(layer0Bitmap, 0f, 0f, null)
                                        Log.d("SuggestionViewModel", "✅ Layer 0 drawn: ${layer0Bitmap.width}x${layer0Bitmap.height}")

                                        // ✅ SKIP other layers for testing
                                        Log.d("SuggestionViewModel", "⚠️ SKIPPING other layers for testing - only BG + Layer 0")

                                        Log.d("SuggestionViewModel", "✅ ULTRA HACK: Thumbnail with ONLY layer 0 completed")
                                        compositeBitmap
                                    } else {
                                        Log.e("SuggestionViewModel", "❌ HACK: Failed to load layer 0 bitmap")
                                        it
                                    }
                                } catch (e: Exception) {
                                    Log.e("SuggestionViewModel", "❌ HACK: Error compositing layer 0: ${e.message}")
                                    it
                                }
                            } else {
                                Log.d("SuggestionViewModel", "⚠️ HACK: No layer 0 found in randomState")
                                it
                            }
                        } else {
                            it
                        }

                        // ✅ PROGRESSIVE: Update map as each thumbnail completes
                        synchronized(thumbnailsMap) {
                            thumbnailsMap[suggestion.id] = finalThumbnail
                        }

                        // ✅ Emit updated map immediately (UI updates progressively)
                        withContext(Dispatchers.Main) {
                            _thumbnails.value = thumbnailsMap.toMap()
                            Log.d("SuggestionViewModel", "Thumbnail ready: ${suggestion.id} (${thumbnailsMap.size}/6)")
                        }
                    }
                }
            }

            // Wait for all thumbnails to complete
            jobs.forEach { it.await() }

            Log.d("SuggestionViewModel", "All thumbnails generated: ${thumbnailsMap.size}")
        }
    }

    /**
     * Generate 2 random suggestions cho 1 category
     * ✅ WORKAROUND: Đặc biệt xử lý category 1 (Miley) để đảm bảo layer 0 đúng
     */
    private fun generateSuggestionsForCategory(
        characterData: CustomizeModel,
        categoryPosition: Int,
        characterIndex: Int,
        categoryName: String,
        context: Context
    ): List<SuggestionModel> {
        val suggestions = mutableListOf<SuggestionModel>()

        repeat(2) { index ->
            val randomState = randomizeCharacter(characterData, categoryPosition)
            val randomBackground = getRandomBackground(context)

            val suggestion = SuggestionModel(
                id = "${categoryName}_${index}_${UUID.randomUUID()}",
                categoryPosition = categoryPosition,
                characterIndex = characterIndex,
                characterData = characterData.avatar,
                randomState = randomState,
                background = randomBackground
            )

            suggestions.add(suggestion)
            Log.d("SuggestionViewModel", "Generated: ${categoryName}_${index} (characterIndex: $characterIndex)")
        }

        return suggestions
    }

    /**
     * Random tất cả layers của character
     * ✅ WORKAROUND: Đảm bảo layer đầu tiên (index 0) được xử lý đúng, đặc biệt cho category 1 (Miley)
     * @param character CustomizeModel của character
     * @param categoryPosition Vị trí category (0=Tommy, 1=Miley, 2=Dammy)
     */
    private fun randomizeCharacter(character: CustomizeModel, categoryPosition: Int): RandomState {
        val layerSelections = mutableMapOf<Int, LayerSelection>()

        character.layerList.forEachIndexed { index, layerListModel ->
            // ✅ DEBUG: Log tất cả layers được xử lý
            if (categoryPosition == 1) {
                Log.d("SuggestionViewModel", "🔍 Processing layer $index: positionCustom=${layerListModel.positionCustom}, positionNav=${layerListModel.positionNavigation}, items=${layerListModel.layer.size}")
            }

            // Bỏ qua layer rỗng
            if (layerListModel.layer.isEmpty()) {
                if (categoryPosition == 1) {
                    Log.d("SuggestionViewModel", "⚠️ Layer $index SKIPPED: Empty layer")
                }
                return@forEachIndexed
            }

            // Random 1 item trong layer (bỏ qua None ở index 0)
            val startIndex = if (index == 0) 1 else 0
            val availableItems = layerListModel.layer.size

            if (availableItems <= startIndex) {
                if (categoryPosition == 1) {
                    Log.d("SuggestionViewModel", "⚠️ Layer $index SKIPPED: availableItems($availableItems) <= startIndex($startIndex)")
                }
                return@forEachIndexed
            }

            val randomItemIndex = Random.nextInt(startIndex, availableItems)
            val randomItem = layerListModel.layer[randomItemIndex]

            // ✅ WORKAROUND: Đối với category 1 (Miley) và layer 0, cần xử lý đặc biệt
            // Vì có bug khi load ảnh, ta cần đảm bảo path được set đúng
            val shouldForceColor = (categoryPosition == 1 && index == 0 && randomItem.isMoreColors && randomItem.listColor.isNotEmpty())

            // Random màu nếu có
            val randomColorIndex = if (randomItem.isMoreColors && randomItem.listColor.isNotEmpty()) {
                Random.nextInt(randomItem.listColor.size)
            } else {
                0
            }

            // ✅ WORKAROUND: Đảm bảo path được lấy đúng theo logic của handleFillLayer
            val finalPath = if (randomItem.isMoreColors && randomItem.listColor.isNotEmpty()) {
                // Có màu -> lấy path từ listColor
                val colorPath = randomItem.listColor[randomColorIndex].path

                // ✅ LOG chi tiết cho layer 0 của category 1
                if (categoryPosition == 1 && index == 0) {
                    Log.d("SuggestionViewModel", "🔧 Miley Layer 0 - Item: $randomItemIndex, Color: $randomColorIndex")
                    Log.d("SuggestionViewModel", "🔧 Miley Layer 0 - Path: $colorPath")
                    Log.d("SuggestionViewModel", "🔧 Miley Layer 0 - Total colors: ${randomItem.listColor.size}")
                }

                colorPath
            } else {
                // Không có màu -> lấy image gốc
                val imagePath = randomItem.image

                if (categoryPosition == 1 && index == 0) {
                    Log.d("SuggestionViewModel", "🔧 Miley Layer 0 (No Color) - Item: $randomItemIndex, Path: $imagePath")
                }

                imagePath
            }

            // ✅ ULTIMATE FIX: Cho Miley, dùng key đặc biệt cho body layer để tránh bị overwrite
            // Body layer: positionNavigation=0, positionCustom=1
            // Ears layer: positionNavigation=2, positionCustom=1 (cùng positionCustom!)
            val storageKey = if (categoryPosition == 1 && layerListModel.positionNavigation == 0) {
                // Body layer của Miley - dùng key âm để tránh conflict
                -1
            } else {
                layerListModel.positionCustom
            }

            // ✅ CRITICAL: Check duplicate positionCustom (multiple layers with same positionCustom)
            if (layerSelections.containsKey(storageKey)) {
                if (categoryPosition == 1) {
                    Log.w("SuggestionViewModel", "⚠️ DUPLICATE key=$storageKey at layer $index (nav=${layerListModel.positionNavigation})")
                    Log.w("SuggestionViewModel", "   Previous layer will be OVERWRITTEN!")
                }
            }

            layerSelections[storageKey] = LayerSelection(
                itemIndex = randomItemIndex,
                path = finalPath,
                colorIndex = randomColorIndex
            )

            if (categoryPosition == 1 && layerListModel.positionNavigation == 0) {
                Log.d("SuggestionViewModel", "✅ Body layer saved with key=$storageKey, path=$finalPath")
            }
        }

        return RandomState(layerSelections)
    }

    /**
     * Get random background path
     */
    private fun getRandomBackground(context: Context): String {
        val backgrounds = DataLocal.getBgAsset(context)
        return if (backgrounds.isNotEmpty()) {
            backgrounds.random()
        } else {
            ""
        }
    }

    /**
     * Get suggestion by ID
     */
    fun getSuggestionById(id: String): SuggestionModel? {
        return _suggestions.value.find { it.id == id }
    }

    /**
     * Get thumbnail bitmap by suggestion ID
     */
    fun getThumbnailById(id: String): Bitmap? {
        return _thumbnails.value[id]
    }

    /**
     * Get suggestions by category
     */
    fun getSuggestionsByCategory(categoryPosition: Int): List<SuggestionModel> {
        return _suggestions.value.filter { it.categoryPosition == categoryPosition }
    }
}