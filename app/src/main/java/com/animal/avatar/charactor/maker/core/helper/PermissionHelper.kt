package com.animal.avatar.charactor.maker.core.helper

import android.Manifest
import android.os.Build

object PermissionHelper {

    val storagePermission = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> emptyArray() // Android 13+: No permission needed, uses MediaStore API

        else -> arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE // Android 8-12: Need write permission
        )
    }

    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }

    val cameraPermission = arrayOf(Manifest.permission.CAMERA)
}