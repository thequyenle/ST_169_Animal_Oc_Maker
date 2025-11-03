package com.animal.avatar.charactor.maker.data.suggestion

import com.google.gson.Gson

/**
 * Model đại diện cho một suggestion (gợi ý trang phục)
 */
data class SuggestionModel(
    val id: String,
    val categoryPosition: Int,
    val characterIndex: Int,      // Index chính xác của character trong allData list
    val characterData: String,
    val randomState: RandomState,
    val background: String
)

/**
 * Lưu trữ trạng thái random của các layers
 * Key: positionCustom của layer
 * Value: LayerSelection chứa thông tin item được chọn
 */
data class RandomState(
    val layerSelections: Map<Int, LayerSelection>
) {
    /**
     * Convert sang JSON string để truyền qua Intent
     */
    fun toJson(): String {
        return Gson().toJson(this)
    }

    companion object {
        /**
         * Parse từ JSON string
         */
        fun fromJson(json: String): RandomState {
            return Gson().fromJson(json, RandomState::class.java)
        }
    }
}

/**
 * Thông tin về item được chọn trong một layer
 */
data class LayerSelection(
    val itemIndex: Int,      // Index của item trong layer
    val path: String,        // Path của image/color được chọn
    val colorIndex: Int      // Index của color nếu item có nhiều màu
)
