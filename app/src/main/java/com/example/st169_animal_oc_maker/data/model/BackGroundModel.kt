package com.girlmaker.create.avatar.creator.model

data class BackGroundModel(
        val path: String,
        var isSelected: Boolean= false,
        val type: BgType = BgType.NORMAL
)
enum class BgType {
    NONE, NORMAL
}
