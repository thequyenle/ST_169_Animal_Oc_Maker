package com.example.st169_animal_oc_maker.data.custom

data class ItemNavCustomModel(
    val path: String,
    val positionCustom: Int,
    val positionNavigation: Int,
    var isSelected: Boolean = false,
    val listImageColor: ArrayList<ItemColorImageModel> = arrayListOf()
)