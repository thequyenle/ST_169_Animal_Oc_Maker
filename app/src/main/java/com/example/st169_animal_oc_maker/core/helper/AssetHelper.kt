package com.example.st169_animal_oc_maker.core.helper

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import com.example.st169_animal_oc_maker.core.utils.key.AssetsKey
import com.example.st169_animal_oc_maker.core.utils.key.ValueKey
import com.example.st169_animal_oc_maker.data.custom.ColorModel
import com.example.st169_animal_oc_maker.data.custom.CustomizeModel
import com.example.st169_animal_oc_maker.data.custom.LayerListModel
import com.example.st169_animal_oc_maker.data.custom.LayerModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.io.FileOutputStream
import java.util.ArrayList

object AssetHelper {
    // ✅ PERFORMANCE: Cache assets list to avoid repeated I/O
    private val assetListCache = mutableMapOf<String, ArrayList<String>>()

    // Read sub folder
    fun getSubfoldersAsset(context: Context, path: String): ArrayList<String> {
        // Check cache first
        assetListCache[path]?.let {
            Log.d("AssetHelper", "✅ Cache hit for: $path")
            return it
        }

        val allData = context.assets.list(path)
        if (allData == null || allData.isEmpty()) {
            Log.e("nbhieu", "❌ Error: Cannot read asset path: $path")
            return arrayListOf()
        }
        val sortedData =
            MediaHelper.sortAsset(allData)?.map { "${AssetsKey.ASSET_MANAGER}/$path/$it" }
                ?.toCollection(ArrayList())

        // Cache result
        val result = sortedData ?: arrayListOf()
        assetListCache[path] = result
        Log.d("AssetHelper", "📦 Cached assets list for: $path (${result.size} items)")
        return result
    }

    // Read sub folder
    fun getSubfoldersNotDomainAsset(context: Context, path: String): ArrayList<String> {
        // Check cache first (with different key prefix)
        val cacheKey = "nodomain_$path"
        assetListCache[cacheKey]?.let {
            Log.d("AssetHelper", "✅ Cache hit for: $cacheKey")
            return it
        }

        val allData = context.assets.list(path)
        if (allData == null || allData.isEmpty()) {
            Log.e("nbhieu", "❌ Error: Cannot read asset path: $path")
            return arrayListOf()
        }
        val sortedData = MediaHelper.sortAsset(allData)?.map { "${AssetsKey.DATA}/$it" }
            ?.toCollection(ArrayList())

        // Cache result
        val result = sortedData ?: arrayListOf()
        assetListCache[cacheKey] = result
        Log.d("AssetHelper", "📦 Cached assets list for: $cacheKey (${result.size} items)")
        return result
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

    // ✅ PERFORMANCE: Cache helper function
    private fun getCachedAssetList(assetManager: AssetManager, path: String, cacheKey: String): Array<String>? {
        // Check cache first
        val cached = assetListCache[cacheKey]
        if (cached != null) {
            Log.d("AssetHelper", "✅ Cache hit: $cacheKey")
            return cached.map { it.removePrefix("${AssetsKey.ASSET_MANAGER}/") }.toTypedArray()
        }

        // Cache miss - load from assets
        val result = assetManager.list(path)
        if (result != null && result.isNotEmpty()) {
            // Store in cache
            val cacheValue = result.map { "${AssetsKey.ASSET_MANAGER}/$it" }.toCollection(ArrayList())
            assetListCache[cacheKey] = cacheValue
            Log.d("AssetHelper", "📦 Cached: $cacheKey (${result.size} items)")
        }
        return result
    }

    suspend fun getDataFromAsset(context: Context): ArrayList<CustomizeModel> = coroutineScope {
        val start = System.currentTimeMillis()
        var cacheHits = 0
        var cacheMisses = 0

        val customList = ArrayList<CustomizeModel>()
        val assetManager = context.assets

        // ✅ OPTIMIZATION 1: Cache character list
        val characterList = getCachedAssetList(assetManager, AssetsKey.DATA, "characters")
        if (characterList == null || characterList.isEmpty()) {
            Log.e("nbhieu", "❌ Error: Cannot read asset data folder - characterList is null or empty!")
            return@coroutineScope arrayListOf()
        }
        if (assetListCache.containsKey("characters")) cacheHits++ else cacheMisses++

        val sortedCharacter = MediaHelper.sortAsset(characterList)
        if (sortedCharacter.isNullOrEmpty()) {
            Log.e("nbhieu", "❌ Error: sortedCharacter is null or empty!")
            return@coroutineScope arrayListOf()
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

            // ✅ OPTIMIZATION 2: Cache layer list for each character
            val cacheKey = "character_${character}_layers"
            val layer = getCachedAssetList(assetManager, "${AssetsKey.DATA}/${character}", cacheKey)
            if (layer == null || layer.isEmpty()) {
                Log.e("nbhieu", "❌ Error: Cannot read layers for character: $character")
                return@coroutineScope arrayListOf()
            }
            if (assetListCache.containsKey(cacheKey)) cacheHits++ else cacheMisses++

            val sortedLayer =
                MediaHelper.sortAsset(layer)?.toCollection(ArrayList()) ?: arrayListOf()

            if (sortedLayer.isEmpty()) {
                Log.e("nbhieu", "❌ Error: sortedLayer is empty for character: $character")
                return@coroutineScope arrayListOf()
            }

            val avatar = "${AssetsKey.DATA_ASSET}${character}/${sortedLayer.last()}"
            sortedLayer.removeAt(sortedLayer.size - 1)
            Log.d("nbhieu", "avatar: $avatar")

            Log.d(
                "nbhieu",
                "----------------------------------------------------------------------------------"
            )

            // 🚀 PARALLEL LOADING: Load all layers in parallel using async
            val layerLoadStart = System.currentTimeMillis()
            val layerModels = sortedLayer.mapIndexed { i, layerName ->
                async {
                    loadSingleLayer(
                        assetManager,
                        character,
                        layerName,
                        i
                    )
                }
            }.awaitAll().filterNotNull()

            layerListModelList.addAll(layerModels)
            val layerLoadEnd = System.currentTimeMillis()
            Log.d("nbhieu", "🚀 Loaded ${layerModels.size} layers in parallel: ${layerLoadEnd - layerLoadStart}ms")
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

        val loadTime = System.currentTimeMillis() - start
        Log.d("nbhieu", "count time: ${loadTime}ms")
        Log.d("AssetHelper", "📊 CACHE STATS: Hits=$cacheHits, Misses=$cacheMisses, Hit Rate=${if (cacheHits + cacheMisses > 0) (cacheHits * 100 / (cacheHits + cacheMisses)) else 0}%")
        return@coroutineScope customList
    }

    /**
     * 🚀 Load a single layer (used for parallel loading)
     */
    private fun loadSingleLayer(
        assetManager: AssetManager,
        character: String,
        layerName: String,
        layerIndex: Int
    ): LayerListModel? {
        try {
            // Tách 1 và 30 (1.30)
            val position = layerName.split(AssetsKey.SPLIT_LAYER)
            val positionCustom = position[0].toInt() - 1
            val positionNavigation = position[1].toInt() - 1

            // ✅ OPTIMIZATION 3: Cache layer contents
            val layerCacheKey = "layer_${character}_${layerName}"
            val folderOrImageList = synchronized(assetListCache) {
                getCachedAssetList(
                    assetManager,
                    "${AssetsKey.DATA}/${character}/${layerName}",
                    layerCacheKey
                )
            }

            if (folderOrImageList == null || folderOrImageList.isEmpty()) {
                Log.e("nbhieu", "❌ Error: Cannot read folder contents for ${character}/${layerName}")
                return null
            }

            val folderOrImageSortedList =
                MediaHelper.sortAsset(folderOrImageList)?.toCollection(ArrayList())
                    ?: arrayListOf()

            if (folderOrImageSortedList.isEmpty()) {
                Log.e("nbhieu", "❌ Error: folderOrImageSortedList is empty for ${character}/${layerName}")
                return null
            }

            Log.d("nbhieu", "==============================================")
            Log.d("nbhieu", "Character: $character")
            Log.d("nbhieu", "Layer folder: ${layerName}")
            Log.d("nbhieu", "Items inside: ${folderOrImageSortedList.joinToString()}")
            Log.d("nbhieu", "First item: ${folderOrImageSortedList.firstOrNull()}")

            // ✅ Verify nav.png exists (should be last item after sorting)
            val lastItem = folderOrImageSortedList.last()
            if (lastItem != AssetsKey.NAVIGATION_IMAGE_PNG) {
                Log.w("nbhieu", "⚠️ Warning: Expected nav.png but found '$lastItem' in ${character}/${layerName}")
            }

            //Lấy navigation
            val navigationImage =
                "${AssetsKey.DATA_ASSET}${character}/${layerName}/${lastItem}"
            folderOrImageSortedList.removeAt(folderOrImageSortedList.size - 1)

            // Check if list is empty after removing nav.png
            if (folderOrImageSortedList.isEmpty()) {
                Log.e("nbhieu", "❌ Error: No files left after removing nav.png for ${character}/${layerName}")
                return null
            }

            // Nếu không có folder -> không có màu
            val layer = if (AssetsKey.FIRST_IMAGE.any { it in folderOrImageSortedList[0] }) {
                Log.d("nbhieu", "→ Detected: NO COLOR")
                getDataNoColor(character, folderOrImageSortedList, layerName)
            } else {
                Log.d("nbhieu", "→ Detected: HAS COLOR")
                synchronized(assetListCache) {
                    getDataColor(assetManager, character, folderOrImageSortedList, layerName, 0, 0)
                }
            }

            return LayerListModel(positionCustom, positionNavigation, navigationImage, layer)
        } catch (e: Exception) {
            Log.e("nbhieu", "❌ Error loading layer ${layerName}: ${e.message}")
            return null
        }
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
        assetManager: AssetManager, character: String, folderList: List<String>, folder: String,
        cacheHits: Int = 0, cacheMisses: Int = 0
    ): ArrayList<LayerModel> {
        try {
            val colorNames = folderList.map { "#$it" }

            // ✅ OPTIMIZATION 4: Cache color folder contents
            val fileList = folderList.map { colorFolder ->
                val colorCacheKey = "color_${character}_${folder}_${colorFolder}"
                val cachedList = getCachedAssetList(
                    assetManager,
                    "${AssetsKey.DATA}/$character/$folder/$colorFolder",
                    colorCacheKey
                )

                cachedList?.let { MediaHelper.sortAsset(it) }
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