package com.example.st169_animal_oc_maker.core.helper

import android.content.Context
import android.os.Build
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

/**
 * ✅ PERFORMANCE OPTIMIZATION: Custom Glide configuration for low-end devices
 *
 * ⚠️ NOTE: This module is currently DISABLED because kapt plugin causes SSL error during build.
 * The @GlideModule annotation won't be processed without kapt.
 *
 * TO ENABLE:
 * 1. Fix SSL/certificate issue on your network
 * 2. Uncomment in app/build.gradle:
 *    - id 'kotlin-kapt'
 *    - kapt 'com.github.bumptech.glide:compiler:4.16.0'
 * 3. Rebuild project
 *
 * GOOD NEWS: Other optimizations (image size limiting, cache, monitoring) still work!
 *
 * Tối ưu hóa Glide để chạy tốt trên thiết bị yếu:
 * - Giảm memory cache size
 * - Giới hạn bitmap quality
 * - Tăng disk cache cho reuse
 */
@GlideModule
class MyGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // 🔍 Detect RAM size
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        activityManager.getMemoryInfo(memoryInfo)

        val totalRamGB = memoryInfo.totalMem / (1024 * 1024 * 1024).toFloat()
        val isLowRamDevice = totalRamGB < 2.5f || Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1

        // 📊 Memory Cache - Adaptive based on device RAM
        val memoryCacheSizeBytes = if (isLowRamDevice) {
            1024 * 1024 * 15  // 15MB for low-end devices (≤2GB RAM or Android ≤8.1)
        } else {
            1024 * 1024 * 30  // 30MB for normal devices
        }
        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes.toLong()))

        // 💾 Disk Cache - Larger to reduce repeated decoding
        val diskCacheSizeBytes = 1024 * 1024 * 150  // 150MB
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes.toLong()))

        // 🎨 Bitmap Quality - Use RGB_565 for low-end devices (50% memory vs ARGB_8888)
        if (isLowRamDevice) {
            builder.setDefaultRequestOptions(
                RequestOptions()
                    .format(DecodeFormat.PREFER_RGB_565)  // 2 bytes/pixel vs 4 bytes
            )
        }

        // 📝 Log configuration
        android.util.Log.d("MyGlideModule", "Glide configured: RAM=${String.format("%.1f", totalRamGB)}GB, " +
                "isLowRam=$isLowRamDevice, memCache=${memoryCacheSizeBytes/1024/1024}MB")
    }

    override fun isManifestParsingEnabled(): Boolean {
        // Disable manifest parsing for faster app startup
        return false
    }
}

