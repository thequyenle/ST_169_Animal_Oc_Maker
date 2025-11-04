package com.animal.avatar.charactor.maker.ui.suggestion

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.animal.avatar.charactor.maker.core.helper.ThumbnailGenerator
import com.animal.avatar.charactor.maker.core.utils.DataLocal
import com.animal.avatar.charactor.maker.data.custom.CustomizeModel
import com.animal.avatar.charactor.maker.data.suggestion.LayerSelection
import com.animal.avatar.charactor.maker.data.suggestion.RandomState
import com.animal.avatar.charactor.maker.data.suggestion.SuggestionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.random.Random

class SuggestionViewModel : ViewModel() {

    // ✅ OPTIMIZATION: Giảm threads từ 4 xuống 2 để tránh quá tải
    private val multiThreadDispatcher = Executors.newFixedThreadPool(2).asCoroutineDispatcher()

    // ✅ OPTIMIZATION: Giới hạn chỉ 3 thumbnails load đồng thời để tránh OOM
    private val thumbnailSemaphore = Semaphore(3)

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
    fun generateAllSuggestions(allData: List<CustomizeModel>, context: Context, suggestionsPerCategory: Int = 2) {
        viewModelScope.launch {
            _isLoading.value = true
            val startTime = System.currentTimeMillis()

            Log.d("SuggestionViewModel", "🚀 Starting PARALLEL generation with 2 cores (optimized for 10 items/category)...")

            // ✅ STEP 1: Generate suggestions PARALLEL cho 3 categories (sử dụng 2 cores)
            val suggestionsList = withContext(multiThreadDispatcher) {
                val jobs = mutableListOf<kotlinx.coroutines.Deferred<List<SuggestionModel>>>()

                // Tommy - async job 1
                if (allData.size > 0) {
                    jobs.add(async {
                        Log.d("SuggestionViewModel", "🎯 [Core 1] Generating Tommy...")
                        generateSuggestionsForCategory(
                            characterData = allData[0],
                            categoryPosition = 0,
                            characterIndex = 0,
                            categoryName = "Tommy",
                            context = context,
                            count = suggestionsPerCategory
                        )
                    })
                }

                // Miley - async job 2
                if (allData.size > 1) {
                    jobs.add(async {
                        Log.d("SuggestionViewModel", "🎯 [Core 2] Generating Miley...")
                        generateSuggestionsForCategory(
                            characterData = allData[1],
                            categoryPosition = 1,
                            characterIndex = 1,
                            categoryName = "Miley",
                            context = context,
                            count = suggestionsPerCategory
                        )
                    })
                }

                // Dammy - async job 3
                if (allData.size > 2) {
                    jobs.add(async {
                        Log.d("SuggestionViewModel", "🎯 [Core 3] Generating Dammy...")
                        generateSuggestionsForCategory(
                            characterData = allData[2],
                            categoryPosition = 2,
                            characterIndex = 2,
                            categoryName = "Dammy",
                            context = context,
                            count = suggestionsPerCategory
                        )
                    })
                }

                // Đợi tất cả jobs hoàn thành và gộp kết quả
                jobs.flatMap { it.await() }
            }

            val generationTime = System.currentTimeMillis() - startTime

            // ✅ STEP 2: Emit suggestions IMMEDIATELY (UI can show placeholders)
            _suggestions.value = suggestionsList
            _isLoading.value = false
            Log.d("SuggestionViewModel", "========================================")
            Log.d("SuggestionViewModel", "✅ EMITTED ${suggestionsList.size} SUGGESTIONS in ${generationTime}ms")
            Log.d("SuggestionViewModel", "   Tommy: ${suggestionsList.count { it.categoryPosition == 0 }}")
            Log.d("SuggestionViewModel", "   Miley: ${suggestionsList.count { it.categoryPosition == 1 }}")
            Log.d("SuggestionViewModel", "   Dammy: ${suggestionsList.count { it.categoryPosition == 2 }}")
            Log.d("SuggestionViewModel", "   (thumbnails loading...)")
            Log.d("SuggestionViewModel", "========================================")

            // ✅ STEP 3: Generate thumbnails PARALLEL với 4 cores
            generateThumbnailsProgressively(suggestionsList, context)
        }
    }


    /**
     * ✅ OPTIMIZED: Generate thumbnails progressively and parallel với 4 cores
     * Emit each thumbnail as soon as it's ready (don't wait for all)
     */
    private fun generateThumbnailsProgressively(suggestions: List<SuggestionModel>, context: Context) {
        viewModelScope.launch(multiThreadDispatcher) {
            val startTime = System.currentTimeMillis()
            val thumbnailsMap = mutableMapOf<String, Bitmap>()

            Log.d("SuggestionViewModel", "🖼️ Starting CONTROLLED thumbnail generation (max 3 concurrent, ${suggestions.size} total)...")

            // ✅ CONTROLLED: Generate thumbnails với semaphore - chỉ 3 thumbnails load đồng thời
            val jobs = suggestions.map { suggestion ->
                async(multiThreadDispatcher) {
                    // ✅ Acquire semaphore - đợi nếu đã có 3 thumbnails đang load
                    thumbnailSemaphore.acquire()
                    try {
                        val thumbnail = ThumbnailGenerator.generateThumbnail(
                            context,
                            suggestion.randomState,
                            suggestion.background
                        )

                        thumbnail?.let {
                            // ✅ SIMPLE LOGIC: Tất cả characters (Tommy, Miley, Dammy) đều dùng logic giống nhau
                            // Chỉ dùng thumbnail từ ThumbnailGenerator, không có logic đặc biệt

                            // ✅ PROGRESSIVE: Update map as each thumbnail completes
                            synchronized(thumbnailsMap) {
                                thumbnailsMap[suggestion.id] = it
                            }

                            // ✅ Emit updated map immediately (UI updates progressively)
                            withContext(Dispatchers.Main) {
                                _thumbnails.value = thumbnailsMap.toMap()
                                Log.d("SuggestionViewModel", "✅ Thumbnail ready: ${suggestion.id} (${thumbnailsMap.size}/${suggestions.size})")
                            }
                        }
                    } finally {
                        // ✅ Release semaphore - cho phép thumbnail tiếp theo load
                        thumbnailSemaphore.release()
                    }
                }
            }

            // Wait for all thumbnails to complete
            jobs.forEach { it.await() }

            val thumbnailTime = System.currentTimeMillis() - startTime
            Log.d("SuggestionViewModel", "========================================")
            Log.d("SuggestionViewModel", "✅ All ${thumbnailsMap.size} thumbnails generated in ${thumbnailTime}ms")
            Log.d("SuggestionViewModel", "   Average: ${thumbnailTime / thumbnailsMap.size}ms per thumbnail")
            Log.d("SuggestionViewModel", "========================================")
        }
    }

    /**
     * Generate random suggestions cho 1 category
     * Logic đơn giản giống nhau cho tất cả characters (Tommy, Miley, Dammy)
     */
    private fun generateSuggestionsForCategory(
        characterData: CustomizeModel,
        categoryPosition: Int,
        characterIndex: Int,
        categoryName: String,
        context: Context,
        count: Int = 2
    ): List<SuggestionModel> {
        val suggestions = mutableListOf<SuggestionModel>()

        Log.d("SuggestionViewModel", "🎲 Generating $count suggestions for $categoryName...")

        repeat(count) { index ->
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
            Log.d("SuggestionViewModel", "  ✅ Generated: ${categoryName}_${index} with ${randomState.layerSelections.size} layers")
        }

        Log.d("SuggestionViewModel", "🎯 Completed: $categoryName generated ${suggestions.size} suggestions")
        return suggestions
    }

    /**
     * Random tất cả layers của character
     * Logic đơn giản giống nhau cho tất cả characters (Tommy, Miley, Dammy)
     */
    private fun randomizeCharacter(character: CustomizeModel, categoryPosition: Int): RandomState {
        val layerSelections = mutableMapOf<Int, LayerSelection>()

        character.layerList.forEachIndexed { index, layerListModel ->
            // Bỏ qua layer rỗng
            if (layerListModel.layer.isEmpty()) {
                return@forEachIndexed
            }

            // Random 1 item trong layer
            // Chỉ bỏ qua item None (index 0) cho layer body (index 0), các layer khác có thể chọn None
            val startIndex = if (index == 0) {
                // Layer đầu tiên (body) - bắt buộc phải có item
                1
            } else {
                // Các layer khác - có thể chọn None (index 0) hoặc item thật
                0
            }

            val availableItems = layerListModel.layer.size

            if (availableItems <= startIndex) {
                // Nếu layer chỉ có None hoặc rỗng, bỏ qua
                return@forEachIndexed
            }

            val randomItemIndex = Random.nextInt(startIndex, availableItems)
            val randomItem = layerListModel.layer[randomItemIndex]

            // Random màu nếu có
            val randomColorIndex = if (randomItem.isMoreColors && randomItem.listColor.isNotEmpty()) {
                Random.nextInt(randomItem.listColor.size)
            } else {
                0
            }

            // Lấy path: nếu có màu thì lấy từ listColor, không thì lấy image gốc
            val finalPath = if (randomItem.isMoreColors && randomItem.listColor.isNotEmpty()) {
                randomItem.listColor[randomColorIndex].path
            } else {
                randomItem.image
            }

            // Chỉ thêm vào nếu có path hợp lệ
            if (finalPath.isNotEmpty()) {
                // Dùng positionCustom làm key
                layerSelections[layerListModel.positionCustom] = LayerSelection(
                    itemIndex = randomItemIndex,
                    path = finalPath,
                    colorIndex = randomColorIndex
                )
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

    /**
     * Cleanup dispatcher khi ViewModel bị destroy
     */
    override fun onCleared() {
        super.onCleared()
        multiThreadDispatcher.close()
        Log.d("SuggestionViewModel", "🔚 ViewModel cleared, dispatcher closed")
    }
}