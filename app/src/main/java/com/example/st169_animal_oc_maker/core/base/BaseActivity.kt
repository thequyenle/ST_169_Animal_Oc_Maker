package com.animal.avatar.charactor.maker.core.base

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.animal.avatar.charactor.maker.R
import com.animal.avatar.charactor.maker.core.custom.StrokeTextView
import com.animal.avatar.charactor.maker.core.dialog.LoadingDialog
import com.animal.avatar.charactor.maker.core.dialog.NoInternetDialog
import com.animal.avatar.charactor.maker.core.extensions.checkPermissions
import com.animal.avatar.charactor.maker.core.extensions.goToSettings
import com.animal.avatar.charactor.maker.core.extensions.handleBack
import com.animal.avatar.charactor.maker.core.extensions.hideNavigation
import com.animal.avatar.charactor.maker.core.extensions.requestPermission
import com.animal.avatar.charactor.maker.core.extensions.select
import com.animal.avatar.charactor.maker.core.extensions.showToast
import com.animal.avatar.charactor.maker.core.utils.SystemUtils
import com.animal.avatar.charactor.maker.core.utils.SystemUtils.setLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import kotlin.let

abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {
    companion object {
        const val PERMISSION_REQUEST_CODE = 112
        const val STORAGE_PERMISSION_REQUEST_CODE = 113
    }

    lateinit var binding: T

    protected val loadingDialog: LoadingDialog by lazy {
        LoadingDialog(this)
    }

    protected val noInternetDialog: NoInternetDialog by lazy {
        NoInternetDialog(this)
    }

    protected abstract fun setViewBinding(): T

    protected abstract fun initView()

    protected abstract fun viewListener()

    open fun dataObservable() {

    }

    protected abstract fun initText()

    open fun initAds() {}

    override fun onCreate(@Nullable
                          savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initWindow()
        SystemUtils.setLocale(this)
        binding = setViewBinding()
        setContentView(binding.root)
        initView()
        viewListener()
        dataObservable()
        initText()
        initAds()
    }

    fun initWindow() {
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        )
//
//        window.decorView.systemUiVisibility =
//            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
    }

    override fun onResume() {
        super.onResume()
        initWindow()
       hideNavigation()
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
//        if (hasFocus) {
//            fullScreenImmersive(window)
//        }
        hideNavigation()
    }

    fun fullScreenImmersive(window: Window?) {
        window?.let {
            fullScreenImmersive(it.decorView)
        }
    }

    fun fullScreenImmersive(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            view.systemUiVisibility = uiOptions
        }
    }

    fun setGradientHeightTextColor(textView: TextView, startColor: Int, endColor: Int) {
        val paint = textView.paint
        val height = textView.textSize
        val textShader = LinearGradient(
            0f, 0f, 0f, height, intArrayOf(startColor, endColor), null, Shader.TileMode.CLAMP
        )
        textView.paint.shader = textShader
    }

    fun setGradientHeightTextColor(textView: TextView, startColor: Int, centerColor: Int, endColor: Int) {
        val paint = textView.paint
        val height = textView.textSize
        val textShader = LinearGradient(
            0f, 0f, 0f, height, intArrayOf(startColor, centerColor, endColor), null, Shader.TileMode.CLAMP
        )
        textView.paint.shader = textShader
    }

    fun setGradientHeightTextColor(
            textView: TextView, startColor: Int, centerColor: Int, centerColor2: Int, endColor: Int
    ) {
        val paint = textView.paint
        val height = textView.textSize
        val textShader = LinearGradient(
            0f, 0f, 0f, height, intArrayOf(startColor, centerColor, centerColor2, endColor), null, Shader.TileMode.CLAMP
        )
        textView.paint.shader = textShader
    }

    fun setGradientWidthTextColor(textView: TextView, startColor: Int, endColor: Int) {
        val paint = textView.paint
        val width = paint.measureText(textView.text.toString())
        val textShader = LinearGradient(
            0f, 0f, width, textView.textSize, intArrayOf(startColor, endColor), null, Shader.TileMode.CLAMP
        )

        textView.paint.shader = textShader
    }

    fun setGradientStrokeText(textGradient: TextView, textStroke: StrokeTextView) {
        setGradientHeightTextColor(textGradient, Color.parseColor("#FAA1DF"), Color.parseColor("#FD63A3"))
        textStroke.setStroke(1.5f, Color.WHITE, Paint.Join.ROUND, 5f)
        textStroke.select()
        textGradient.select()
    }

    fun dpToPx(dp: Int): Int {
        return (dp * this.resources.displayMetrics.density).toInt()
    }

    fun focusAndShowKeyboard(editText: EditText) {
        editText.requestFocus()
        editText.setSelection(editText.text.length) // Đặt con trỏ tại cuối nội dung
        val imm = editText.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT) // Hiển thị bàn phím
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        handleBack()
    }

    fun setGradientHeightTextColor(textView: TextView) {
        val paint = textView.paint
        val height = textView.textSize
        val textShader = LinearGradient(
            0f, 0f, 0f, height, intArrayOf(Color.parseColor("#0D8AFC"), Color.parseColor("#33F0B0")), null, Shader.TileMode.CLAMP
        )
        textView.paint.shader = textShader
    }

    suspend fun showLoading() {
        withContext(Dispatchers.Main) {
            if (loadingDialog.isShowing.not()) {
                setLocale(this@BaseActivity)
                loadingDialog.show()
            }
        }
    }


    suspend fun dismissLoading(isBlack: Boolean = false) {
        withContext(Dispatchers.Main) {
            if (loadingDialog.isShowing) {
                loadingDialog.dismiss()
                hideNavigation(isBlack)
            }
        }
    }

    /**
     * Show no internet dialog
     */
    fun showNoInternetDialog() {
        if (!noInternetDialog.isShowing) {
            noInternetDialog.show()
        }
    }

    /**
     * Dismiss no internet dialog
     */
    fun dismissNoInternetDialog() {
        if (noInternetDialog.isShowing) {
            noInternetDialog.dismiss()
        }
    }

    /**
     * Check if storage permission is granted based on Android version
     */
    protected fun hasStoragePermission(): Boolean {
        return checkPermissions(SystemUtils.storagePermission)
    }

    /**
     * Request storage permission based on Android version
     */
    protected fun requestStoragePermission() {
        requestPermission(SystemUtils.storagePermission, STORAGE_PERMISSION_REQUEST_CODE)
    }

    /**
     * Helper to execute download action with permission check
     */
    protected fun executeWithStoragePermission(onGranted: () -> Unit) {
        if (hasStoragePermission()) {
            onGranted()
        } else {
            pendingActionAfterPermission = onGranted
            requestStoragePermission()
        }
    }

    /**
     * Handle storage permission denied
     * Show dialog to go to Settings if permission is permanently denied
     */
    protected fun handleStoragePermissionDenied() {
        val permissions = SystemUtils.storagePermission
        val shouldShowRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions.any { shouldShowRequestPermissionRationale(it) }
        } else {
            false
        }

        if (!shouldShowRationale && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Permission permanently denied, show dialog to go to Settings
            goToSettings()
        } else {
            // Permission denied, just show toast
            showToast(R.string.denied_storage)
        }
    }

    // Variable to store pending action after permission granted
    protected var pendingActionAfterPermission: (() -> Unit)? = null
}