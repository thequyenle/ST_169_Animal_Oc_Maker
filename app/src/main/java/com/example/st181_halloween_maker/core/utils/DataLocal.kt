package com.example.st181_halloween_maker.core.utils


import android.content.Context
import com.example.st181_halloween_maker.R
import com.girlmaker.create.avatar.creator.model.IntroModel
import com.girlmaker.create.avatar.creator.model.LanguageModel

import com.example.st181_halloween_maker.core.utils.KeyApp.ASSET_MANAGER
import com.example.st181_halloween_maker.core.utils.KeyApp.AVATAR_ASSET
import com.example.st181_halloween_maker.core.utils.KeyApp.AVATAR_STICKER_ASSET
import com.example.st181_halloween_maker.core.utils.KeyApp.BG_ASSET
import com.facebook.shimmer.Shimmer

import kotlin.collections.forEach
import kotlin.collections.sortedWith
import kotlin.text.toIntOrNull

object DataLocal {

    val shimmer =
        Shimmer.AlphaHighlightBuilder().setDuration(1800).setBaseAlpha(0.7f).setHighlightAlpha(0.6f)
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT).setAutoStart(true).build()
    fun getLanguageList(): ArrayList<LanguageModel> {
        return arrayListOf(
            LanguageModel("hi", "Hindi", R.drawable.ic_flag_hindi),
            LanguageModel("es", "Spanish", R.drawable.ic_flag_spanish),
            LanguageModel("fr", "French", R.drawable.ic_flag_french),
            LanguageModel("en", "English", R.drawable.ic_flag_english),
            LanguageModel("pt", "Portuguese", R.drawable.ic_flag_portugeese),
            LanguageModel("in", "Indonesian", R.drawable.ic_flag_indo),
            LanguageModel("de", "German", R.drawable.ic_flag_germani),
        )
    }

    val itemIntroList = listOf(
        IntroModel(R.drawable.intro1, R.string.title_1),
        IntroModel(R.drawable.intro2, R.string.title_2),
        IntroModel(R.drawable.intro3, R.string.title_3),
    )
    // sắp xếp danh sách file asset theo thứ tự số data1 - ...
    private fun sortAsset(listFiles: Array<String>?): List<String>? {
        val sortedFiles = listFiles?.sortedWith(compareBy { fileName ->
            val matchResult = Regex("\\d+").find(fileName)
            matchResult?.value?.toIntOrNull() ?: Int.MAX_VALUE
        })
        return sortedFiles
    }

    fun getAvatarStickerAsset(context: Context): ArrayList<String> {
        val assetManager = context.assets
        val allAvatar = assetManager.list(AVATAR_STICKER_ASSET)
        val sortedAvatar = sortAsset(allAvatar)
        val returnAvatar = kotlin.collections.ArrayList<String>()
        sortedAvatar!!.forEach {
            returnAvatar.add("$ASSET_MANAGER/$AVATAR_STICKER_ASSET/$it")
        }

        return returnAvatar
    }
    fun getStickerByList(context: Context, index: Int): List<String> {
        val folder = "sticker/${index + 1}"
        val list = arrayListOf<String>()
        try {
            val files = context.assets.list(folder)
            files?.forEach {
                list.add("$folder/$it")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }



    fun getAvatarAsset(context: Context): ArrayList<String> {
        val assetManager = context.assets
        val allAvatar = assetManager.list(AVATAR_ASSET)
        val sortedAvatar = sortAsset(allAvatar)
        val returnAvatar = kotlin.collections.ArrayList<String>()
        sortedAvatar!!.forEach {
            returnAvatar.add("$ASSET_MANAGER/$AVATAR_ASSET/$it")
        }

        return returnAvatar
    }
    fun getBgAsset(context: Context): ArrayList<String> {
        val assetManager = context.assets
        val allBg = assetManager.list(BG_ASSET)
        val sortedBg = sortAsset(allBg)
        val returnBg = kotlin.collections.ArrayList<String>()
        sortedBg!!.forEach {
            returnBg.add("$ASSET_MANAGER/$BG_ASSET/$it")
        }

        val i = 0
        for(i in 24 .. 95) {
            returnBg.add("https://lvtglobal.site/public/app/HandsomeManCreator/bg/" + i + ".png")
        }

        return returnBg
    }



//    fun getLayerAsset(context: Context): ArrayList<CustomizeModel> {
//        val list: ArrayList<CustomizeModel> = arrayListOf()
//        val assetManager = context.assets
//        val allJjk = assetManager.list(JJK)
//        val allAvatar = getAvatarAsset(context)
//        val sortedJjk = sortAsset(allJjk)
//        sortedJjk?.let {
//            sortedJjk.forEachIndexed { indexJjk, jjk ->
//                val handsomeJjk = CustomizeModel(jjk, allAvatar[indexJjk])
//
//                val folderLayer = assetManager.list("$JJK/$jjk")
//
//                folderLayer?.let {
//                    folderLayer.forEachIndexed { indexLayer, layer ->
//                        when(layer){
//                            BODY -> {
//                                handsomeJjk.body = getFileLayer(jjk, layer, assetManager)
//                            }
//                            FACE -> {
//                                handsomeJjk.face = getFileLayer(jjk, layer, assetManager)
//                            }
//                            BODY_PAINT -> {
//                                handsomeJjk.body_paint = getFileLayer(jjk, layer, assetManager)
//                            }
//                            FACE_PAINT_1 -> {
//                                handsomeJjk.face_paint_1 = getFileLayer(jjk, layer, assetManager)
//                            }
//                            FACE_PAINT_2 -> {
//                                handsomeJjk.face_paint_2 = getFileLayer(jjk, layer, assetManager)
//                            }
//                            EYES -> {
//                                handsomeJjk.eyes = getFileLayer(jjk, layer, assetManager)
//                            }
//                            EYEBROW -> {
//                                handsomeJjk.eyebrow = getFileLayer(jjk, layer, assetManager)
//                            }
//                            BACK_EARS -> {
//                                handsomeJjk.back_ears = getFileLayer(jjk, layer, assetManager)
//                            }
//                            FRONT_EARS -> {
//                                handsomeJjk.front_ears = getFileLayer(jjk, layer, assetManager)
//                            }
//                            NOSE -> {
//                                handsomeJjk.nose = getFileLayer(jjk, layer, assetManager)
//                            }
//                            MOUTH -> {
//                                handsomeJjk.mouth = getFileLayer(jjk, layer, assetManager)
//                            }
//                            FRONT_HAIR -> {
//                                handsomeJjk.front_hair = getFileLayer(jjk, layer, assetManager)
//                            }
//                            BACK_HAIR -> {
//                                handsomeJjk.back_hair = getFileLayer(jjk, layer, assetManager)
//                            }
//                            TOP_HAIR -> {
//                                handsomeJjk.top_hair = getFileLayer(jjk, layer, assetManager)
//                            }
//                            BOTTOM_HAIR -> {
//                                handsomeJjk.bottom_hair = getFileLayer(jjk, layer, assetManager)
//                            }
//                            SIDE_HAIR -> {
//                                handsomeJjk.side_hair = getFileLayer(jjk, layer, assetManager)
//                            }
//                            SHIRT -> {
//                                handsomeJjk.shirt = getFileLayer(jjk, layer, assetManager)
//                            }
//                            JACKET -> {
//                                handsomeJjk.jacket = getFileLayer(jjk, layer, assetManager)
//                            }
//                            PAINT -> {
//                                handsomeJjk.paint = getFileLayer(jjk, layer, assetManager)
//                            }
//                            SOCKS -> {
//                                handsomeJjk.socks = getFileLayer(jjk, layer, assetManager)
//                            }
//                            SHOE -> {
//                                handsomeJjk.shoe = getFileLayer(jjk, layer, assetManager)
//                            }
//                            EARRINGS -> {
//                                handsomeJjk.earrings = getFileLayer(jjk, layer, assetManager)
//                            }
//                            NECKLACE -> {
//                                handsomeJjk.necklace = getFileLayer(jjk, layer, assetManager)
//                            }
//                            GLASSES -> {
//                                handsomeJjk.glasses = getFileLayer(jjk, layer, assetManager)
//                            }
//                            HAT -> {
//                                handsomeJjk.hat = getFileLayer(jjk, layer, assetManager)
//                            }
//                            HORN -> {
//                                handsomeJjk.horn = getFileLayer(jjk, layer, assetManager)
//                            }
//                            CLIP_HAIR -> {
//                                handsomeJjk.clip_hair = getFileLayer(jjk, layer, assetManager)
//                            }
//                            TABLE -> {
//                                handsomeJjk.table = getFileLayer(jjk, layer, assetManager)
//                            }
//                            PEN -> {
//                                handsomeJjk.pen = getFileLayer(jjk, layer, assetManager)
//                            }
//                            BOOK -> {
//                                handsomeJjk.book = getFileLayer(jjk, layer, assetManager)
//                            }
//                            BAG -> {
//                                handsomeJjk.bag = getFileLayer(jjk, layer, assetManager)
//                            }
//                            PET -> {
//                                handsomeJjk.pet = getFileLayer(jjk, layer, assetManager)
//                            }
//                            TAIL -> {
//                                handsomeJjk.tail = getFileLayer(jjk, layer, assetManager)
//                            }
//                            OTHER -> {
//                                handsomeJjk.other = getFileLayer(jjk, layer, assetManager)
//                            }
//                            BACK_EFFECT -> {
//                                handsomeJjk.back_effect = getFileLayer(jjk, layer, assetManager)
//                            }
//
//                        }
//                    }
//                }
//                list.add(handsomeJjk)
//            }
//        }
//        return list
//    }
//
//    private fun getFileLayer(jjk: String, layer: String, assetManager: AssetManager): ArrayList<LayerModel> {
//        val layerPath = kotlin.collections.ArrayList<LayerModel>()
//        val fileLayer = assetManager.list("${JJK}/$jjk/$layer")
//        fileLayer?.let {
//            if (fileLayer[0] == JJK_FIRST_PNG || fileLayer[0] == JJK_FIRST_JPG || fileLayer[0] == JJK_FIRST_WEBP) {
//                val sortedFile = sortAsset(fileLayer)
//                sortedFile?.let {
//                    layerPath.addAll(getDataNoColor(jjk, sortedFile, layer))
//                }
//            } else {
//                layerPath.addAll(getDataColor(assetManager, jjk, fileLayer, layer))
//            }
//        }
//        return layerPath
//    }
//
//    private fun getDataNoColor(jjk: String, filesList: List<String>, folder: String): ArrayList<LayerModel> {
//        val layerPath = kotlin.collections.ArrayList<LayerModel>()
//        for (fileName in filesList) {
//            // file:///android_asset/nuggts/ + nuggts1 + body + data1.png
//            layerPath.add(LayerModel(image = "$JJK_ASSET$jjk/$folder/$fileName", isMoreColors = false, listColor = arrayListOf()))
//        }
//        return layerPath
//    }
//
//    fun getDataColor(assetManager: AssetManager, jjk: String, folderList: Array<String>?, folder: String): ArrayList<LayerModel> {
//        val colorNames = folderList!!.map { "#$it" }
//        val fileList = folderList.map { colorFolder ->
//            assetManager.list("$JJK/$jjk/$folder/$colorFolder")?.let { sortAsset(it) }?.map { "$JJK_ASSET$jjk/$folder/$colorFolder/$it" } ?: emptyList()
//        }
//
//        // Khởi tạo danh sách màu và ghép danh sách file theo index
//        val colorList = Array(fileList.first().size) { index ->
//            Array(folderList.size) { folderIndex ->
//                ColorModel(color = colorNames[folderIndex], path = fileList[folderIndex][index])
//            }.toCollection(kotlin.collections.ArrayList())
//        }.toCollection(kotlin.collections.ArrayList())
//
//
//        return fileList.first().mapIndexed { index, file ->
//            LayerModel(image = file, isMoreColors = true, listColor = colorList[index])
//        }.toCollection(kotlin.collections.ArrayList())
//
//    }
//
//    fun getDataAPI(dataList: ArrayList<Dream>): ArrayList<CustomizeModel> {
//        val allDataAPI: ArrayList<CustomizeModel> = arrayListOf()
//        dataList.forEachIndexed { index, data ->
//            val dataGirl = CustomizeModel(name = data.girlName, avatar = "")
//            if(!isFailBaseURL){
//                dataGirl.avatar = "$BASE_URL$SUB_DOMAIN/${data.girlName}/${AVATAR}.png"
//            }else{
//                dataGirl.avatar = "$BASE_URL_PREVENTIVE$SUB_DOMAIN/${data.girlName}/${AVATAR}.png"
//            }
//            data.partsGirl.forEachIndexed { index, pathData ->
//                when(pathData.parts){
//                    BODY -> {dataGirl.body = getData(pathData,BODY)}
//                    FACE -> {dataGirl.face = getData(pathData,FACE)}
//                    BODY_PAINT -> {dataGirl.body_paint = getData(pathData,BODY_PAINT)}
//                    FACE_PAINT_1 -> {dataGirl.face_paint_1 = getData(pathData,FACE_PAINT_1)}
//                    FACE_PAINT_2 -> {dataGirl.face_paint_2 = getData(pathData,FACE_PAINT_2)}
//                    EYES -> {dataGirl.eyes = getData(pathData,EYES)}
//                    EYEBROW -> {dataGirl.eyebrow = getData(pathData,EYEBROW)}
//                    BACK_EARS -> {dataGirl.back_ears = getData(pathData,BACK_EARS)}
//                    FRONT_EARS -> {dataGirl.front_ears = getData(pathData,FRONT_EARS)}
//                    NOSE -> {dataGirl.nose = getData(pathData,NOSE)}
//                    MOUTH -> {dataGirl.mouth = getData(pathData,MOUTH)}
//                    FRONT_HAIR -> {dataGirl.front_hair = getData(pathData,FRONT_HAIR)}
//                    BACK_HAIR -> {dataGirl.back_hair = getData(pathData,BACK_HAIR)}
//                    TOP_HAIR -> {dataGirl.top_hair = getData(pathData,TOP_HAIR)}
//                    BOTTOM_HAIR -> {dataGirl.bottom_hair = getData(pathData,BOTTOM_HAIR)}
//                    SIDE_HAIR -> {dataGirl.side_hair = getData(pathData,SIDE_HAIR)}
//                    SHIRT -> {dataGirl.shirt = getData(pathData,SHIRT)}
//                    JACKET -> {dataGirl.jacket = getData(pathData,JACKET)}
//                    PAINT -> {dataGirl.paint = getData(pathData,PAINT)}
//                    SOCKS -> {dataGirl.socks = getData(pathData,SOCKS)}
//                    SHOE -> {dataGirl.shoe = getData(pathData,SHOE)}
//                    EARRINGS -> {dataGirl.earrings = getData(pathData,EARRINGS)}
//                    NECKLACE -> {dataGirl.necklace = getData(pathData,NECKLACE)}
//                    GLASSES -> {dataGirl.glasses = getData(pathData,GLASSES)}
//                    HAT -> {dataGirl.hat = getData(pathData,HAT)}
//                    HORN -> {dataGirl.horn = getData(pathData,HORN)}
//                    CLIP_HAIR -> {dataGirl.clip_hair = getData(pathData,CLIP_HAIR)}
//                    TABLE -> {dataGirl.table = getData(pathData,TABLE)}
//                    PEN -> {dataGirl.pen = getData(pathData,PEN)}
//                    BOOK -> {dataGirl.book = getData(pathData,BOOK)}
//                    BAG -> {dataGirl.bag = getData(pathData,BAG)}
//                    PET -> {dataGirl.pet = getData(pathData,PET)}
//                    TAIL -> {dataGirl.tail = getData(pathData,TAIL)}
//                    OTHER -> {dataGirl.other = getData(pathData,OTHER)}
//                    BACK_EFFECT -> {dataGirl.back_effect = getData(pathData,BACK_EFFECT)}
//
//                }
//            }
//            allDataAPI.add(dataGirl)
//        }
//        allDataAPI.forEach {
//            Log.d("nmduc","avatar:${it.avatar}")
//        }
//        return allDataAPI
//    }
//
//    private fun getData(partData: PartAPI, layer: String): ArrayList<LayerModel> {
//        return if (partData.colorArray != "" || partData.colorArray.isNotEmpty()) {
//            getDataAPIColor(partData, layer)
//        } else {
//            getDataAPINoColor(partData, layer)
//        }
//    }
//
//    private fun getDataAPINoColor(part: PartAPI, layer: String): ArrayList<LayerModel> {
//        val layerPath = kotlin.collections.ArrayList<LayerModel>()
//        for (i in 1..part.quantity) {
//            if (!isFailBaseURL) {
//                layerPath.add(
//                    LayerModel(
//                        "$BASE_URL$SUB_DOMAIN/${part.position}/${layer}/$i.png", false, arrayListOf()
//                    )
//                )
//            } else {
//                layerPath.add(
//                    LayerModel(
//                        "$BASE_URL_PREVENTIVE$SUB_DOMAIN/${part.position}/${layer}/$i.png", false, arrayListOf()
//                    )
//                )
//            }
//
//        }
//        return layerPath
//    }
//
//    private fun getDataAPIColor(part: PartAPI, layer: String): ArrayList<LayerModel> {
//        val layerPath = kotlin.collections.ArrayList<LayerModel>()
//        val getColorCode: ArrayList<String> = arrayListOf()
//        getColorCode.addAll(part.colorArray.split(","))
//        val colorCode: ArrayList<String> = arrayListOf()
//        getColorCode.forEach {
//            colorCode.add("#${it}")
//        }
//
//        for (i in 1..part.quantity) {
//            val listColor: ArrayList<ColorModel> = arrayListOf()
//            for (j in 0 until colorCode.size) {
//                if (!isFailBaseURL) {
//                    listColor.add(ColorModel(colorCode[j], "$BASE_URL$SUB_DOMAIN/${part.position}/${layer}/${getColorCode[j]}/$i.png")
//                    )
//                } else {
//                    listColor.add(ColorModel(colorCode[j], "$BASE_URL_PREVENTIVE$SUB_DOMAIN/${part.position}/${layer}/${getColorCode[j]}/$i.png")
//                    )
//                }
//
//            }
//            layerPath.add(LayerModel(listColor[0].path, true, listColor))
//        }
//        return layerPath
//    }



}