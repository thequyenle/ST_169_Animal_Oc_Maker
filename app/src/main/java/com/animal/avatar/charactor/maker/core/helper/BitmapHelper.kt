package com.animal.avatar.charactor.maker.core.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.core.graphics.createBitmap
import androidx.exifinterface.media.ExifInterface
import java.io.File

object BitmapHelper {
    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream).also {
                inputStream?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("nbhieu", "uriToBitmap: ${e.message}")
            null
        }
    }

    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun convertPathsToBitmaps(context: Context, paths: List<String>): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        paths.forEachIndexed { index, path ->
            val uri = Uri.fromFile(File(path))
            var bitmap = uriToBitmap(context, uri)
            if (bitmap != null) {
                val exif = ExifInterface(path)
                val orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                )

                if (orientation != ExifInterface.ORIENTATION_ROTATE_90 && orientation != ExifInterface.ORIENTATION_ROTATE_180 && orientation != ExifInterface.ORIENTATION_ROTATE_270) {
                    bitmaps.add(bitmap)
                } else {
                    bitmap = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
                        else -> bitmap
                    }

                    bitmaps.add(bitmap)
                }

            }
        }
        return bitmaps
    }

    @Throws(OutOfMemoryError::class)
    fun createBimapFromView(view: View): Bitmap {
        try {
            val output = createBitmap(view.width, view.height)
            val canvas = Canvas(output)
            view.draw(canvas)
            return output
        } catch (error: OutOfMemoryError) {
            throw error
        }
    }

    /**
     * ✅ NEW: Crop transparent edges from bitmap
     * Removes all transparent/white borders around the actual content
     * @param bitmap Source bitmap with transparent edges
     * @return Cropped bitmap without transparent borders
     */
    fun cropTransparentEdges(bitmap: Bitmap): Bitmap {
        var minX = bitmap.width
        var minY = bitmap.height
        var maxX = 0
        var maxY = 0

        // Find bounds of non-transparent content
        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val pixel = bitmap.getPixel(x, y)
                val alpha = (pixel shr 24) and 0xFF

                // Check if pixel is not fully transparent (alpha > threshold)
                if (alpha > 10) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    if (y > maxY) maxY = y
                }
            }
        }

        // If no content found, return original
        if (maxX < minX || maxY < minY) {
            return bitmap
        }

        // Crop to content bounds
        val width = maxX - minX + 1
        val height = maxY - minY + 1

        return try {
            Bitmap.createBitmap(bitmap, minX, minY, width, height)
        } catch (e: Exception) {
            Log.e("BitmapHelper", "cropTransparentEdges failed: ${e.message}")
            bitmap
        }
    }

    /**
     * ✅ NEW: Create bitmap from ImageView specifically
     * More efficient than capturing entire layout
     */
    fun createBitmapFromImageView(view: View): Bitmap {
        return createBimapFromView(view)
    }
}