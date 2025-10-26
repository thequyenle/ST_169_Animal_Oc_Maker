package com.example.st181_halloween_maker.ui.suggestion

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.st181_halloween_maker.core.helper.ThumbnailGenerator
import com.example.st181_halloween_maker.core.utils.DataLocal
import com.example.st181_halloween_maker.data.custom.CustomizeModel
import com.example.st181_halloween_maker.data.suggestion.LayerSelection
import com.example.st181_halloween_maker.data.suggestion.RandomState
import com.example.st181_halloween_maker.data.suggestion.SuggestionModel
import kotlinx.coroutines.Dispatchers
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
     * Generate suggestions cho tất cả 3 categories
     * @param allData List data từ DataViewModel
     * @param context Context để load backgrounds và generate thumbnails
     */
    fun generateAllSuggestions(allData: List<CustomizeModel>, context: Context) {
        viewModelScope.launch {
            _isLoading.value = true

            val suggestionsList = mutableListOf<SuggestionModel>()
            val thumbnailsMap = mutableMapOf<String, Bitmap>()

            withContext(Dispatchers.IO) {
                // Tommy (data1 - position 0)
                if (allData.size > 0) {
                    val tommySuggestions = generateSuggestionsForCategory(allData[0], 0, "Tommy", context)
                    suggestionsList.addAll(tommySuggestions)

                    // Generate thumbnails for Tommy
                    tommySuggestions.forEach { suggestion ->
                        val thumbnail = ThumbnailGenerator.generateThumbnail(
                            context,
                            suggestion.randomState,
                            suggestion.background
                        )
                        thumbnail?.let {
                            thumbnailsMap[suggestion.id] = it
                        }
                    }
                }

                // Miley (data2 - position 1)
                if (allData.size > 1) {
                    val mileySuggestions = generateSuggestionsForCategory(allData[1], 1, "Miley", context)
                    suggestionsList.addAll(mileySuggestions)

                    // Generate thumbnails for Miley
                    mileySuggestions.forEach { suggestion ->
                        val thumbnail = ThumbnailGenerator.generateThumbnail(
                            context,
                            suggestion.randomState,
                            suggestion.background
                        )
                        thumbnail?.let {
                            thumbnailsMap[suggestion.id] = it
                        }
                    }
                }

                // Dammy (data3 - position 2)
                if (allData.size > 2) {
                    val dammySuggestions = generateSuggestionsForCategory(allData[2], 2, "Dammy", context)
                    suggestionsList.addAll(dammySuggestions)

                    // Generate thumbnails for Dammy
                    dammySuggestions.forEach { suggestion ->
                        val thumbnail = ThumbnailGenerator.generateThumbnail(
                            context,
                            suggestion.randomState,
                            suggestion.background
                        )
                        thumbnail?.let {
                            thumbnailsMap[suggestion.id] = it
                        }
                    }
                }
            }

            _suggestions.value = suggestionsList
            _thumbnails.value = thumbnailsMap
            _isLoading.value = false

            Log.d("SuggestionViewModel", "Generated ${suggestionsList.size} suggestions with ${thumbnailsMap.size} thumbnails")
        }
    }

    /**
     * Generate 2 random suggestions cho 1 category
     */
    private fun generateSuggestionsForCategory(
        characterData: CustomizeModel,
        categoryPosition: Int,
        categoryName: String,
        context: Context
    ): List<SuggestionModel> {
        val suggestions = mutableListOf<SuggestionModel>()

        repeat(2) { index ->
            val randomState = randomizeCharacter(characterData)
            val randomBackground = getRandomBackground(context)

            val suggestion = SuggestionModel(
                id = "${categoryName}_${index}_${UUID.randomUUID()}",
                categoryPosition = categoryPosition,
                characterData = characterData.avatar,
                randomState = randomState,
                background = randomBackground
            )

            suggestions.add(suggestion)
            Log.d("SuggestionViewModel", "Generated: ${categoryName}_${index}")
        }

        return suggestions
    }

    /**
     * Random tất cả layers của character
     */
    private fun randomizeCharacter(character: CustomizeModel): RandomState {
        val layerSelections = mutableMapOf<Int, LayerSelection>()

        character.layerList.forEachIndexed { index, layerListModel ->
            // Bỏ qua layer rỗng
            if (layerListModel.layer.isEmpty()) return@forEachIndexed

            // Random 1 item trong layer (bỏ qua None ở index 0)
            val startIndex = if (index == 0) 1 else 0
            val availableItems = layerListModel.layer.size

            if (availableItems <= startIndex) return@forEachIndexed

            val randomItemIndex = Random.nextInt(startIndex, availableItems)
            val randomItem = layerListModel.layer[randomItemIndex]

            // Random màu nếu có
            val randomColorIndex = if (randomItem.isMoreColors && randomItem.listColor.isNotEmpty()) {
                Random.nextInt(randomItem.listColor.size)
            } else {
                0
            }

            val finalPath = if (randomItem.isMoreColors && randomItem.listColor.isNotEmpty()) {
                randomItem.listColor[randomColorIndex].path
            } else {
                randomItem.image
            }

            layerSelections[layerListModel.positionCustom] = LayerSelection(
                itemIndex = randomItemIndex,
                path = finalPath,
                colorIndex = randomColorIndex
            )
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