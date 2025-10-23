package com.example.st181_halloween_maker.core.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.dialog.LoadingDialog
import com.example.st181_halloween_maker.core.dialog.NoInternetDialog
import com.example.st181_halloween_maker.core.utils.KeyApp.INTENT_KEY
import com.example.st181_halloween_maker.core.utils.SystemUtils
import com.example.st181_halloween_maker.core.utils.SystemUtils.lastClickTime
import com.example.st181_halloween_maker.core.utils.SystemUtils.setLocale
import com.example.st181_halloween_maker.ui.home.HomeActivity
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.util.Locale
import kotlin.apply
import kotlin.io.use
import kotlin.jvm.java
import kotlin.let
import kotlin.text.capitalize
import kotlin.text.lowercase
import kotlin.text.repeat

internal fun View.show() {
    visibility = View.VISIBLE
}

internal fun View.hide() {
    visibility = View.INVISIBLE
}

// ----------------------------
// Visibility extensions
// ----------------------------
fun View.visible() { visibility = View.VISIBLE }
fun View.invisible() { visibility = View.INVISIBLE }
fun View.gone() { visibility = View.GONE }

// ----------------------------
// Toast extensions
// ----------------------------
fun Activity.showToast(message: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, ContextCompat.getString(this, message), duration).show()
}
fun Activity.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}



internal fun Activity.startIntent(targetActivity: Class<*>) {
    val intent = Intent(this, targetActivity)
    startActivity(intent)
}

internal fun Activity.startIntent(targetActivity: Class<*>, value: String) {
    val intent = Intent(this, targetActivity)
    intent.putExtra(INTENT_KEY, value)
    startActivity(intent)
}

internal fun Activity.startIntent(targetActivity: Class<*>, value: Int) {
    val intent = Intent(this, targetActivity)
    intent.putExtra(INTENT_KEY, value)
    startActivity(intent)
}


internal fun Activity.startIntent(targetActivity: Class<*>, key: String, value: String) {
    val intent = Intent(this, targetActivity)
    intent.putExtra(key, value)
    startActivity(intent)
}

internal fun Activity.startIntentAnim(targetActivity: Class<*>) {
    val intent = Intent(this, targetActivity)
    startActivity(intent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}

internal fun Activity.startIntentAnim(targetActivity: Class<*>, value: String) {
    val intent = Intent(this, targetActivity)
    intent.putExtra(INTENT_KEY, value)
    startActivity(intent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}

internal fun Activity.startIntentAnim(targetActivity: Class<*>, key: String, value: String) {
    val intent = Intent(this, targetActivity)
    intent.putExtra(key, value)
    startActivity(intent)
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}


internal fun View.select() {
    isSelected = true
}

internal fun upperFirstCharacter(str: String): String {
    return str.capitalize(Locale.ROOT)
}

internal fun convertToLowerCase(input: String): String {
    return input.lowercase()
}

internal fun formatDecimal(number: Double, decimalPlaces: Int): String {
    val pattern = "#." + "0".repeat(decimalPlaces)
    val decimalFormat = DecimalFormat(pattern)
    return decimalFormat.format(number)
}

internal fun dp2px(context: Context, dpVal: Int): Int {
    return (dpVal * context.resources.displayMetrics.density).toInt()
}

internal fun sp2px(spVal: Float): Int {
    val r = Resources.getSystem()
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spVal, r.displayMetrics).toInt()
}

//internal fun dpToPx(context: Context, dp: Float): Int {
//    val density = context.resources.displayMetrics.density
//    return (dp * density).toInt()
//}


fun pxToDp(context: Context, px: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, px.toFloat(), context.resources.displayMetrics
    ).toInt()
}

fun Activity.showSystemUI(white: Boolean) {
    if (white) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    } else {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        WindowCompat.setDecorFitsSystemWindows(window, false);
    } else {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }
}


fun Activity.hideSoftKeyboard() {
    currentFocus?.let {
        val inputMethodManager = ContextCompat.getSystemService(this, InputMethodManager::class.java)!!
        inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
    }
}

internal fun Activity.hideWindow() {
    window.setFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    )
    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
}

fun Activity.dLog(content: String) {
    Log.d("nmduc", content)
}

fun Activity.eLog(content: String) {
    Log.e("nmduc", content)
}


fun Activity.handleShare(context: Activity, bit: Bitmap) {
    val loading = LoadingDialog(context)
    setLocale(context)
    loading.show()
    var imageFile: File? = null
    var imageUri: Uri? = null
    val handleExceptionCoroutine = CoroutineExceptionHandler { _, throwable ->
        Log.d("nmduc", "error_load: ${throwable.message}")
    }
    CoroutineScope(SupervisorJob() + Dispatchers.IO + handleExceptionCoroutine).launch {
        val job1 = async {
            imageFile = saveBitmapToCache(bit)
            return@async true
        }
        val job2 = async(Dispatchers.Main) {
            if (job1.await()) {
                imageUri = FileProvider.getUriForFile(
                    context, "${context.packageName}.fileprovider", imageFile!!
                )
            }
            return@async true
        }
        launch(Dispatchers.Main) {
            if (job2.await()) {
                loading.dismiss()
                hideNavigation()
                shareImage(context, imageUri!!)
            }
        }
    }
}

internal fun Activity.saveBitmapToCache(bitmap: Bitmap): File {
    val cachePath = File(cacheDir, "images")
    cachePath.mkdirs()
    val file = File(cachePath, "shared_image_${System.currentTimeMillis()}.png")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return file
}

internal fun shareImage(context: Context, imageUri: Uri) {
    val arr = ArrayList<Uri>()
    arr.add(imageUri)

    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "*/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, arr)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Images"))
}


internal fun Activity.handleBack() {
    finish()
    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
}

//fun Activity.handleMusic(view: ImageView, pref: SharedPrefsHelper) {
//    if (statusSound) {
//        mediaPlayer?.stop()
//        mediaPlayer = null
//        statusSound = false
//        pref.setSettings(MUSIC, false)
//        view.setImageResource(R.drawable.ic_music_off)
//    } else {
//        mediaPlayer = MediaPlayer.create(this, R.raw.theme)
//        mediaPlayer?.start()
//        safeSetVolume(mediaPlayer, 0.3f)
//        statusSound = true
//        pref.setSettings(MUSIC, true)
//        mediaPlayer?.isLooping = true
//        view.setImageResource(R.drawable.ic_music_on)
//    }
//}

//fun Activity.initMusic(view: ImageView) {
//    if (statusSound) {
//        view.setImageResource(R.drawable.ic_music_on)
//    } else {
//        view.setImageResource(R.drawable.ic_music_off)
//    }
//}

//fun Activity.stopActivity() {
//    if (!isFromOtherScreen) {
//        mediaPlayer?.pause()
//    } else {
//        isFromOtherScreen = false
//    }
//}
//
//fun Activity.restartActivity() {
//    if (statusSound) {
//        mediaPlayer?.start()
//    }
//}

internal fun Activity.hideNavigation(isBlack: Boolean = false) {
    window.setFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
    )
    if (!isBlack) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    } else {
        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }
}

//@SuppressLint("ClickableViewAccessibility")
//@RequiresApi(Build.VERSION_CODES.O)
//fun View.setOnClickListenerWithVibration(
//    vibrationDuration: Long = 100,
//    onClick: (View, Float, Float) -> Unit
//) {
//    var touchX = 0f
//    var touchY = 0f
//    this.setOnTouchListener { _, event ->
//        if (event.action == MotionEvent.ACTION_DOWN) {
//            touchX = event.x
//            touchY = event.y
//        }
//        false
//    }
//    this.setOnClickListener {
//        if (isVibrate) {
//            val vibrator = this.context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
//            vibrator?.let {
//                if (it.hasVibrator()) {
//                    val effect = VibrationEffect.createOneShot(
//                        vibrationDuration,
//                        VibrationEffect.DEFAULT_AMPLITUDE
//                    )
//                    it.vibrate(effect)
//                }
//            }
//        }
//        if (isEffect) {
//            val effect = MediaPlayer.create(context, R.raw.click_fail)
//            effect.start()
//            effect.setOnCompletionListener {
//                it.release()
//            }
//        }
//        onClick(it, touchX, touchY)
//    }
//
//}

fun Activity.handleComeBackHome(address: Activity) {
    val intent = Intent(address, HomeActivity::class.java)
    startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
}

fun View.onSingleClick(time: Long = 500, action: (View) -> Unit) {
    this.setOnClickListener {
        if (System.currentTimeMillis() - lastClickTime >= time) {
            action(it)
            lastClickTime = System.currentTimeMillis()
        }
    }
}
fun Activity.checkInternetEvent(action: (() -> Unit), dialog: (((NoInternetDialog) -> Unit))) {
    Log.d("nmduc", "isConnectInternet: ${SystemUtils.isConnectInternet.value}")
    Log.d("nmduc", "isDataAPI: ${SystemUtils.isDataAPI}")

    if (SystemUtils.isConnectInternet.value != true && SystemUtils.isDataAPI) {

        val noInternetDialog = NoInternetDialog(this)
        setLocale(this)
        noInternetDialog.show()
        dialog.invoke(noInternetDialog)
        noInternetDialog.onOkClick = {
            noInternetDialog.dismiss()
            hideNavigation(true)
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            startActivity(intent)
        }
        noInternetDialog.onDismissClick = {
            noInternetDialog.dismiss()
            hideNavigation(true)
        }
    } else {
        action.invoke()
    }
}

fun Activity.checkInternetEvent1(action: (() -> Unit), dialog: (((NoInternetDialog) -> Unit))) {
    Log.d("nmduc", "isConnectInternet: ${SystemUtils.isConnectInternet.value}")
    Log.d("nmduc", "isDataAPI: ${SystemUtils.isDataAPI}")

    if (SystemUtils.isConnectInternet.value != true && SystemUtils.isDataAPI) {

        val noInternetDialog = NoInternetDialog(this)
        setLocale(this)
        noInternetDialog.show()
        dialog.invoke(noInternetDialog)
        noInternetDialog.onOkClick = {
            noInternetDialog.dismiss()
            hideNavigation(false)
            val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            startActivity(intent)
        }
        noInternetDialog.onDismissClick = {
            noInternetDialog.dismiss()
            hideNavigation(false)
        }
    } else {
        action.invoke()
    }
}

//@SuppressLint("ClickableViewAccessibility")
//fun View.onSingleClick(action: (View) -> Unit) {
//    this.setOnClickListener {
//        if (System.currentTimeMillis() - lastClickTime >= 500) {
//            animateScaleEffect()
//            lastClickTime = System.currentTimeMillis()
//            action(it)
//        }
//    }
//}
//
//@SuppressLint("ClickableViewAccessibility")
//fun View.onSingleClickOut(time: Int = 500, action: (View) -> Unit) {
//    this.setOnClickListener {
//        if (System.currentTimeMillis() - lastClickTime >= time) {
//            if (isEffect) {
//                val effect = MediaPlayer.create(context, R.raw.touch)
//                effect.start()
//                effect.setOnCompletionListener {
//                    it.release()
//                }
//            }
//            animateScaleOutEffect()
//            lastClickTime = System.currentTimeMillis()
//            action(it)
//        }

//    }
//}