package com.example.st181_halloween_maker.core.extensions

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.ConnectivityManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.example.st181_halloween_maker.data.model.DataModel
import com.example.st181_halloween_maker.core.utils.KeyApp.DATA_DEFAULT
import com.example.st181_halloween_maker.core.utils.KeyApp.DOWNLOAD_ALBUM
import com.example.st181_halloween_maker.core.utils.KeyApp.HALLOWEEN_AVAILABLE_FILE
import com.example.st181_halloween_maker.core.utils.KeyApp.PICK_IMAGE_REQUEST_CODE
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.apply
import kotlin.collections.filter
import kotlin.collections.forEach
import kotlin.collections.sortedByDescending
import kotlin.io.extension
import kotlin.jvm.Throws
import kotlin.text.lowercase

internal fun Activity.openImagePicker() {
    val intent = Intent(Intent.ACTION_PICK)
    intent.type = "image/*"
    startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
}


internal fun Activity.saveBitmapToExternalStorage(bitmap: Bitmap) {
    val resolver = contentResolver
    val imageCollection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } else {
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    }

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "image_${System.currentTimeMillis()}.png")
        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/$DOWNLOAD_ALBUM")
        } else {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), DOWNLOAD_ALBUM
            )
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val filePath = File(directory, "image_${System.currentTimeMillis()}.png").absolutePath
            put(MediaStore.Images.Media.DATA, filePath)
        }
    }

    val imageUri = resolver.insert(imageCollection, contentValues)
    if (imageUri != null) {
        try {
            val outputStream: OutputStream? = resolver.openOutputStream(imageUri)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
fun isInternetAvailable(context: Context): Boolean {
    val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    return activeNetwork?.isConnectedOrConnecting == true
}
fun saveBitmapToInternalStorage1(context: Context, bitmap: Bitmap, fileName: String): String? {
    return try {
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
fun Activity.saveBitmapToInternalStorage(album: String, bitmap: Bitmap): String? {
    val name = generateRandomFileName()

    return try {
        val directory = File(filesDir, album)
        if (!directory.exists()) {
            directory.mkdir()
        }

        val file = File(directory, name)

        val fileOutputStream = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)

        fileOutputStream.flush()
        fileOutputStream.close()

        file.absolutePath

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}


fun getImageInternal(context: Context, album: String): ArrayList<String> {
    val imagePaths = ArrayList<String>()
    val targetDir = File(context.filesDir, album)

    if (targetDir.exists() && targetDir.isDirectory) {
        targetDir.listFiles()?.filter { isImageFile(it) }                // Chỉ lấy file ảnh
            ?.sortedByDescending { it.lastModified() } // Sắp xếp giảm dần theo thời gian
            ?.forEach { file ->
                imagePaths.add(file.absolutePath)
            }
    }
    return imagePaths
}


private fun isImageFile(file: File): Boolean {
    val imageExtensions = listOf("jpg", "jpeg", "png", "bmp", "webp")
    val extension = file.extension.lowercase()
    return file.isFile && imageExtensions.contains(extension)
}

@Throws(OutOfMemoryError::class)
internal fun createBimapFromView(view: View): Bitmap {
    try {
        val output = Bitmap.createBitmap(
            view.width, view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(output)
        view.draw(canvas)
        return output
    } catch (error: OutOfMemoryError) {
        throw error
    }
}
fun writeListToFile(context: Context, fileName: String, list: ArrayList<DataModel>) {
    try {
        val json = Gson().toJson(list)
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
            output.write(json.toByteArray())
        }
        Log.d("CustomizeActivity", "File saved to: ${context.filesDir}/$fileName")
    } catch (e: Exception) {
        Log.e("CustomizeActivity", "❌ Lỗi ghi file: ${e.message}")
    }
}

fun readListFromFile(context: Context, fileName: String): ArrayList<DataModel> {
    return try {
        val file = context.getFileStreamPath(fileName)
        if (!file.exists()) return arrayListOf()

        val json = context.openFileInput(fileName).bufferedReader().use { it.readText() }
        val type = object : TypeToken<ArrayList<DataModel>>() {}.type
        Gson().fromJson(json, type) ?: arrayListOf()
    } catch (e: Exception) {
        e.printStackTrace()
        arrayListOf()
    }
}
fun readDataDefaultAssets(context: Context): ArrayList<DataModel> {
    return try {
        val json = context.assets.open("$DATA_DEFAULT/$HALLOWEEN_AVAILABLE_FILE").bufferedReader().use { it.readText() }
        val type = object : TypeToken<ArrayList<DataModel>>() {}.type
        Gson().fromJson(json, type) ?: arrayListOf()
    } catch (e: Exception) {
        e.printStackTrace()
        arrayListOf()
    }
}





