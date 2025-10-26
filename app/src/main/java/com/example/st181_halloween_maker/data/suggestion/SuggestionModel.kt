package com.example.st181_halloween_maker.data.suggestion

import com.google.gson.Gson

data class SuggestionModel(
    val id: String,
    val categoryPosition: Int,  // 0=Tommy, 1=Miley, 2=Dammy
    val characterData: String,  // Avatar path
    val randomState: RandomState,
    val background: String
) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): SuggestionModel? {
            return try {
                Gson().fromJson(json, SuggestionModel::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class RandomState(
    val layerSelections: Map<Int, LayerSelection>  // positionCustom -> selection
) {
    fun toJson(): String = Gson().toJson(this)

    companion object {
        fun fromJson(json: String): RandomState? {
            return try {
                Gson().fromJson(json, RandomState::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}

data class LayerSelection(
    val itemIndex: Int,         // Index trong layer.layer list
    val path: String,           // Path của item đã chọn
    val colorIndex: Int = 0     // Index của màu (nếu có màu)
)