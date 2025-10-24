package com.example.st181_halloween_maker.core.helper

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import com.example.st181_halloween_maker.core.utils.key.AssetsKey
import com.example.st181_halloween_maker.core.utils.key.ValueKey
import com.example.st181_halloween_maker.data.custom.ColorModel
import com.example.st181_halloween_maker.data.custom.CustomizeModel
import com.example.st181_halloween_maker.data.custom.LayerListModel
import com.example.st181_halloween_maker.data.custom.LayerModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList

object AssetHelper {
    // Read sub folder
    fun getSubfoldersAsset(context: Context, path: String): ArrayList<String> {
        val allData = context.assets.list(path)
        if (allData == null || allData.isEmpty()) {
            Log.e("nbhieu", "❌ Error: Cannot read asset path: $path")
            return arrayListOf()
        }
        val sortedData =
            MediaHelper.sortAsset(allData)?.map { "${AssetsKey.ASSET_MANAGER}/$path/$it" }
                ?.toCollection(ArrayList())
        return sortedData ?: arrayListOf()
    }

    // Read sub folder
    fun getSubfoldersNotDomainAsset(context: Context, path: String): ArrayList<String> {
        val allData = context.assets.list(path)
        if (allData == null || allData.isEmpty()) {
            Log.e("nbhieu", "❌ Error: Cannot read asset path: $path")
            return arrayListOf()
        }
        val sortedData = MediaHelper.sortAsset(allData)?.map { "${AssetsKey.DATA}/$it" }
            ?.toCollection(ArrayList())
        return sortedData ?: arrayListOf()
    }

    // Read file txt -> json -> T
    inline fun <reified T> readJsonAsset(context: Context, path: String): T? {
        return try {
            val json = context.assets.open(path).bufferedReader().use { it.readText() }
            Gson().fromJson(json, T::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Read file txt -> json -> list
    inline fun <reified T> readTextToJsonAssets(context: Context, path: String): ArrayList<T> {
        return try {
            val json = context.assets.open(path).bufferedReader().use { it.readText() }
            val type = object : TypeToken<ArrayList<T>>() {}.type
            Gson().fromJson(json, type) ?: arrayListOf()
        } catch (e: Exception) {
            e.printStackTrace()
            arrayListOf()
        }
    }

    // Read file -> bitmap
    fun getBitmapFromAsset(context: Context, fileName: String): Bitmap? {
        return try {
            context.assets.open(fileName).use { input ->
                android.graphics.BitmapFactory.decodeStream(input)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // file asset -> internal
    fun copyAssetToInternal(context: Context, fileName: String): File? {
        return try {
            val outFile = File(context.filesDir, fileName)
            outFile.parentFile?.mkdirs()

            if (!outFile.exists()) {
                context.assets.open(fileName).use { input ->
                    FileOutputStream(outFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            outFile
        } catch (e: Exception) {
            Log.e("nbhieu", "Copy asset failed: ${e.message}")
            null
        }
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------

    fun getDataFromAsset(context: Context): ArrayList<CustomizeModel> {
        val start = System.currentTimeMillis()
        val customList = ArrayList<CustomizeModel>()
        val assetManager = context.assets

        // "character_1, character_2,..."
        val characterList = assetManager.list(AssetsKey.DATA)
        if (characterList == null || characterList.isEmpty()) {
            Log.e("nbhieu", "❌ Error: Cannot read asset data folder - characterList is null or empty!")
            return arrayListOf()
        }

        val sortedCharacter = MediaHelper.sortAsset(characterList)
        if (sortedCharacter.isNullOrEmpty()) {
            Log.e("nbhieu", "❌ Error: sortedCharacter is null or empty!")
            return arrayListOf()
        }

        Log.d(
            "nbhieu",
            "----------------------------------------------------------------------------------"
        )

        sortedCharacter.forEach {
            Log.d("nbhieu", "sortedCharacter: $it")
        }

        Log.d(
            "nbhieu",
            "----------------------------------------------------------------------------------"
        )

        sortedCharacter.forEachIndexed { indexCharacter, character ->
            val layerListModelList = ArrayList<LayerListModel>()
            Log.d("nbhieu", "indexCharacter: $indexCharacter")
            // "1.30, 2.4, 3.1, 4.22,..."
            val layer = assetManager.list("${AssetsKey.DATA}/${character}")
            if (layer == null || layer.isEmpty()) {
                Log.e("nbhieu", "❌ Error: Cannot read layers for character: $character")
                return arrayListOf()
            }

            val sortedLayer =
                MediaHelper.sortAsset(layer)?.toCollection(ArrayList()) ?: arrayListOf()

            if (sortedLayer.isEmpty()) {
                Log.e("nbhieu", "❌ Error: sortedLayer is empty for character: $character")
                return arrayListOf()
            }

            val avatar = "${AssetsKey.DATA_ASSET}${character}/${sortedLayer.last()}"
            sortedLayer.removeAt(sortedLayer.size - 1)
            Log.d("nbhieu", "avatar: $avatar")

            Log.d(
                "nbhieu",
                "----------------------------------------------------------------------------------"
            )

            for (i in 0 until sortedLayer.size) {
                // Tách 1 và 30 (1.30)
                val position = sortedLayer[i].split(AssetsKey.SPLIT_LAYER)
                val positionCustom = position[0].toInt() - 1
                val positionNavigation = position[1].toInt() - 1

                // Lấy folder màu hoặc lấy ảnh nếu không có màu, lấy ảnh navigation
                // data/character_1/1.30
                val folderOrImageList =
                    assetManager.list("${AssetsKey.DATA}/${character}/${sortedLayer[i]}")

                if (folderOrImageList == null || folderOrImageList.isEmpty()) {
                    Log.e("nbhieu", "❌ Error: Cannot read folder contents for ${character}/${sortedLayer[i]}")
                    continue
                }

                val folderOrImageSortedList =
                    MediaHelper.sortAsset(folderOrImageList)?.toCollection(ArrayList())
                        ?: arrayListOf()

                if (folderOrImageSortedList.isEmpty()) {
                    Log.e("nbhieu", "❌ Error: folderOrImageSortedList is empty for ${character}/${sortedLayer[i]}")
                    continue
                }

                // ← THÊM LOG NÀY
                Log.d("nbhieu", "==============================================")
                Log.d("nbhieu", "Character: $character")
                Log.d("nbhieu", "Layer folder: ${sortedLayer[i]}")
                Log.d("nbhieu", "Items inside: ${folderOrImageSortedList.joinToString()}")
                Log.d("nbhieu", "First item: ${folderOrImageSortedList.firstOrNull()}")

                // ✅ Verify nav.png exists (should be last item after sorting)
                val lastItem = folderOrImageSortedList.last()
                if (lastItem != AssetsKey.NAVIGATION_IMAGE_PNG) {
                    Log.w("nbhieu", "⚠️ Warning: Expected nav.png but found '$lastItem' in ${character}/${sortedLayer[i]}")
                }

                //Lấy navigation
                val navigationImage =
                    "${AssetsKey.DATA_ASSET}${character}/${sortedLayer[i]}/${lastItem}"
                folderOrImageSortedList.removeAt(folderOrImageSortedList.size - 1)

                // Check if list is empty after removing nav.png
                if (folderOrImageSortedList.isEmpty()) {
                    Log.e("nbhieu", "❌ Error: No files left after removing nav.png for ${character}/${sortedLayer[i]}")
                    continue
                }

                // Nếu không có folder -> không có màu
                val layer = if (AssetsKey.FIRST_IMAGE.any { it in folderOrImageSortedList[0] }) {
                    Log.d("nbhieu", "→ Detected: NO COLOR")

                    getDataNoColor(character, folderOrImageSortedList, sortedLayer[i])
                } else {
                    Log.d("nbhieu", "→ Detected: HAS COLOR")

                    getDataColor(assetManager, character, folderOrImageSortedList, sortedLayer[i])
                }
                val layerListModel =
                    LayerListModel(positionCustom, positionNavigation, navigationImage, layer)
                layerListModelList.add(layerListModel)
            }
            layerListModelList.sortBy { it.positionNavigation }
            customList.add(CustomizeModel(character, avatar, layerListModelList))
            Log.d(
                "nbhieu",
                "----------------------------------------------------------------------------------"
            )
        }

        // ✅ Only save to file if customList is not empty
        if (customList.isEmpty()) {
            Log.e("nbhieu", "❌ CRITICAL ERROR: customList is empty! Not saving to internal storage.")
            Log.e("nbhieu", "This means asset loading completely failed. Check errors above.")
        } else {
            MediaHelper.writeListToFile(context, ValueKey.DATA_FILE_INTERNAL, customList)
            customList.forEach {
                Log.d("nbhieu", "customList: ${it}")
            }
            Log.d("nbhieu", "✅ Successfully loaded ${customList.size} character(s) from assets")
        }

        Log.d("nbhieu", "count time: ${System.currentTimeMillis() - start}")
        return customList
    }

    private fun getDataNoColor(
        character: String,
        filesList: List<String>,
        folder: String
    ): ArrayList<LayerModel> {
        val layerPath = ArrayList<LayerModel>()
        for (fileName in filesList) {
            // file:///android_asset/nuggts/ + nuggts1 + body + 1.png
            layerPath.add(
                LayerModel(
                    image = "${AssetsKey.DATA_ASSET}$character/$folder/$fileName",
                    isMoreColors = false,
                    listColor = arrayListOf()
                )
            )
        }
        return layerPath
    }

    private fun getDataColor(
        assetManager: AssetManager, character: String, folderList: List<String>, folder: String
    ): ArrayList<LayerModel> {
        try {
            val colorNames = folderList.map { "#$it" }
            val fileList = folderList.map { colorFolder ->
                assetManager.list("${AssetsKey.DATA}/$character/$folder/$colorFolder")
                    ?.let { MediaHelper.sortAsset(it) }
                    ?.map { "${AssetsKey.DATA_ASSET}$character/$folder/$colorFolder/$it" }
                    ?: emptyList()
            }

            // ✅ FIX: Kiểm tra xem có folder màu nào rỗng không
            if (fileList.any { it.isEmpty() }) {
                Log.e("nbhieu", "❌ Error: $character/$folder có folder màu rỗng!")
                return arrayListOf()
            }

            // ✅ FIX: Lấy số file ít nhất để tránh IndexOutOfBoundsException
            val minFileCount = fileList.minOfOrNull { it.size } ?: 0

            if (minFileCount == 0) {
                Log.e("nbhieu", "❌ Error: $character/$folder - Tất cả folder màu đều rỗng!")
                return arrayListOf()
            }

            // ✅ Log cảnh báo nếu số file không đồng nhất
            fileList.forEachIndexed { index, list ->
                if (list.size != minFileCount) {
                    Log.w("nbhieu", "⚠️ Warning: $character/$folder - Folder màu '${folderList[index]}' có ${list.size} files, khác với min $minFileCount files")
                }
            }

            // ✅ Chỉ loop đến minFileCount để tránh crash
            val colorList = Array(minFileCount) { index ->
                Array(folderList.size) { folderIndex ->
                    ColorModel(
                        color = colorNames[folderIndex],
                        path = fileList[folderIndex][index]
                    )
                }.toCollection(ArrayList())
            }.toCollection(ArrayList())

            return (0 until minFileCount).map { index ->
                LayerModel(
                    image = fileList[0][index],
                    isMoreColors = true,
                    listColor = colorList[index]
                )
            }.toCollection(ArrayList())

        } catch (e: Exception) {
            Log.e("nbhieu", "❌ Exception in getDataColor: $character/$folder - ${e.message}")
            e.printStackTrace()
            return arrayListOf()
        }
    }
}