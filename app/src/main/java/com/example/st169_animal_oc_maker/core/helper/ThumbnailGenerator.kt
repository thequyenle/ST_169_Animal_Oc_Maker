package com.example.st169_animal_oc_maker.core.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.st169_animal_oc_maker.data.suggestion.RandomState
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object ThumbnailGenerator {

    private const val THUMBNAIL_WIDTH = 400
    private const val THUMBNAIL_HEIGHT = 400

    /**
     * Generate thumbnail bitmap từ RandomState
     * @param context Context
     * @param randomState RandomState chứa paths của các layers
     * @param backgroundPath Path của background
     * @return Bitmap thumbnail hoặc null nếu có lỗi
     */
    suspend fun generateThumbnail(
        context: Context,
        randomState: RandomState,
        backgroundPath: String?
    ): Bitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(
                THUMBNAIL_WIDTH,
                THUMBNAIL_HEIGHT,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)

            // 1. Vẽ background nếu có
            if (!backgroundPath.isNullOrEmpty()) {
                val bgBitmap = loadBitmapFromPath(context, backgroundPath, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                bgBitmap?.let {
                    canvas.drawBitmap(it, 0f, 0f, null)
                }
            }

            // 2. Vẽ các layers theo thứ tự positionCustom
            val sortedSelections = randomState.layerSelections.toList()
                .sortedBy { it.first } // Sort by positionCustom

            Log.d("ThumbnailGenerator", "🎨 Drawing ${sortedSelections.size} layers...")

            for ((positionCustom, selection) in sortedSelections) {
                if (selection.path.isEmpty()) {
                    Log.d("ThumbnailGenerator", "⚠️ Layer $positionCustom: Empty path, skipping")
                    continue
                }

                Log.d("ThumbnailGenerator", "🖼️ Layer $positionCustom: Loading ${selection.path}")
                val layerBitmap = loadBitmapFromPath(context, selection.path, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)

                if (layerBitmap != null) {
                    canvas.drawBitmap(layerBitmap, 0f, 0f, null)
                    Log.d("ThumbnailGenerator", "✅ Layer $positionCustom: Drawn successfully (size: ${layerBitmap.width}x${layerBitmap.height})")
                } else {
                    Log.e("ThumbnailGenerator", "❌ Layer $positionCustom: Failed to load bitmap from ${selection.path}")
                }
            }

            Log.d("ThumbnailGenerator", "✅ Generated thumbnail successfully with ${sortedSelections.size} layers")
            bitmap

        } catch (e: Exception) {
            Log.e("ThumbnailGenerator", "❌ Error generating thumbnail: ${e.message}")
            null
        }
    }

    /**
     * Load bitmap từ path (hỗ trợ cả local và network)
     */
    private suspend fun loadBitmapFromPath(
        context: Context,
        path: String,
        width: Int,
        height: Int
    ): Bitmap? = suspendCancellableCoroutine { continuation ->
        try {
            Glide.with(context)
                .asBitmap()
                .load(path)
                .override(width, height)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        continuation.resume(resource)
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        Log.e("ThumbnailGenerator", "Failed to load: $path")
                        continuation.resume(null)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        // Do nothing
                    }
                })
        } catch (e: Exception) {
            Log.e("ThumbnailGenerator", "Error loading bitmap: ${e.message}")
            continuation.resumeWithException(e)
        }
    }

    /**
     * ✅ HACK: Load bitmap synchronously (public for SuggestionViewModel hack)
     */
    suspend fun loadBitmapSync(
        context: Context,
        path: String,
        width: Int,
        height: Int
    ): Bitmap? = loadBitmapFromPath(context, path, width, height)

    /**
     * Tạo thumbnail từ list paths (theo thứ tự)
     */
    suspend fun generateThumbnailFromPaths(
        context: Context,
        layerPaths: List<String>,
        backgroundPath: String? = null
    ): Bitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(
                THUMBNAIL_WIDTH,
                THUMBNAIL_HEIGHT,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)

            // Vẽ background
            if (!backgroundPath.isNullOrEmpty()) {
                val bgBitmap = loadBitmapFromPath(context, backgroundPath, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                bgBitmap?.let {
                    canvas.drawBitmap(it, 0f, 0f, null)
                }
            }

            // Vẽ layers
            for (path in layerPaths) {
                if (path.isEmpty()) continue
                val layerBitmap = loadBitmapFromPath(context, path, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                layerBitmap?.let {
                    canvas.drawBitmap(it, 0f, 0f, null)
                }
            }

            bitmap
        } catch (e: Exception) {
            Log.e("ThumbnailGenerator", "Error: ${e.message}")
            null
        }
    }
}