package com.example.st181_halloween_maker.data.custom

data class TotalCustomizeModel (
    // Danh sách Navigation bottom
    var bottomNavigationList: ArrayList<NavigationModel> = arrayListOf(),
    // Danh sách layer & item
    var itemNavigationList: ArrayList<ItemNavCustomModel> = arrayListOf(),
    // Danh sách màu
    var colorItemNavList: ArrayList<ItemColorModel> = arrayListOf(),
    // Trạng thái chọn item/màu
    var isSelectedItemList: ArrayList<Boolean> = arrayListOf(),
    var isShowColorList: ArrayList<Boolean> = arrayListOf(),
    // Key + Path đã chọn
    var keySelectedItemList: ArrayList<String> = arrayListOf(),
    var pathSelectedList: ArrayList<String> = arrayListOf(),
)