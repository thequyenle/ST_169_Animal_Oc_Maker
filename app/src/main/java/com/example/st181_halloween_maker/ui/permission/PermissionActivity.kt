package com.example.st181_halloween_maker.ui.permission

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil.setContentView
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseActivity
import com.example.st181_halloween_maker.core.extensions.checkPermissions
import com.example.st181_halloween_maker.core.extensions.goToSettings
import com.example.st181_halloween_maker.core.extensions.gone
import com.example.st181_halloween_maker.core.extensions.hide
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.extensions.requestPermission
import com.example.st181_halloween_maker.core.extensions.show
import com.example.st181_halloween_maker.core.extensions.startIntentAnim
import com.example.st181_halloween_maker.core.utils.KeyApp.NOTIFICATION_PERMISSION_CODE
import com.example.st181_halloween_maker.core.utils.KeyApp.STORAGE_PERMISSION_CODE
import com.example.st181_halloween_maker.core.utils.SystemUtils
import com.example.st181_halloween_maker.core.utils.SystemUtils.isNotificationPermission
import com.example.st181_halloween_maker.core.utils.SystemUtils.isStoragePermission
import com.example.st181_halloween_maker.core.utils.SystemUtils.notificationPermission
import com.example.st181_halloween_maker.core.utils.SystemUtils.setNotificationPermission
import com.example.st181_halloween_maker.core.utils.SystemUtils.setStoragePermission
import com.example.st181_halloween_maker.core.utils.SystemUtils.storagePermission
import com.example.st181_halloween_maker.databinding.ActivityPermissionBinding
import com.example.st181_halloween_maker.ui.home.HomeActivity

class PermissionActivity : BaseActivity<ActivityPermissionBinding>() {
    override fun setViewBinding(): ActivityPermissionBinding {
        return ActivityPermissionBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initData()
    }

    override fun viewListener() {
        binding.apply {
            switchPermission.onSingleClick {
                if (checkPermissions(storagePermission)) {
                    Toast.makeText(this@PermissionActivity, R.string.granted_storage, Toast.LENGTH_SHORT).show()
                } else {
                    if (isStoragePermission(this@PermissionActivity) >= 2 && !checkPermissions(storagePermission)) {
                        goToSettings()
                    }else{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermission(storagePermission, STORAGE_PERMISSION_CODE)
                        }
                    }
                }

            }

            switchNotification.onSingleClick {
                if (checkPermissions(notificationPermission)) {
                    Toast.makeText(
                        this@PermissionActivity, getString(R.string.granted_notification), Toast.LENGTH_SHORT
                    ).show()
                } else {
                    if (isNotificationPermission(this@PermissionActivity) >= 2) {
                        goToSettings()
                    }else{
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermission(notificationPermission, NOTIFICATION_PERMISSION_CODE)
                        }
                    }
                }

            }

            txtContinue.onSingleClick(1500) {
                startIntentAnim(HomeActivity::class.java)
                SystemUtils.setFirstPermission(this@PermissionActivity, false)
                finishAffinity()
            }

            btnBack.hide()
            btnSettings.hide()
        }
    }

    override fun initText() {
        binding.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                txtPer.text = TextUtils.concat(
                    changeColor(
                        this@PermissionActivity, resources.getString(R.string.allow), R.color.brown, R.font.londrina_solid_regular
                    ), " ", changeColor(
                        this@PermissionActivity, resources.getString(R.string.app_name), R.color.brown, R.font.londrina_solid_regular
                    ), " ",

                    changeColor(
                        this@PermissionActivity, resources.getString(R.string.to_access_13), R.color.brown, R.font.londrina_solid_regular
                    )
                )
            } else {
                txtPer.text = TextUtils.concat(
                    changeColor(
                        this@PermissionActivity, resources.getString(R.string.allow), R.color.brown, R.font.londrina_solid_regular
                    ), " ", changeColor(
                        this@PermissionActivity, resources.getString(R.string.app_name), R.color.brown, R.font.londrina_solid_regular
                    ), " ", changeColor(
                        this@PermissionActivity, resources.getString(R.string.to_access), R.color.brown, R.font.londrina_solid_regular
                    )
                )
            }
        }
    }
    private fun initData() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            binding.layoutNotifi.show()
            binding.layoutStorage.gone()
        } else {
            binding.layoutStorage.show()
            binding.layoutNotifi.gone()
        }
    }

    private fun changeColor(
            context: Context,
            text: String,
            color: Int,
            fontfamily: Int,
    ): SpannableString {
        val spannableString = SpannableString(text)
        spannableString.setSpan(
            ForegroundColorSpan(context.getColor(color)), 0, text.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        val font = ResourcesCompat.getFont(context, fontfamily)
        val typefaceSpan = CustomTypefaceSpan("", font)
        spannableString.setSpan(
            typefaceSpan, 0, text.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannableString
    }

    class CustomTypefaceSpan(private val family: String, private val typeface: Typeface?) : TypefaceSpan(family) {

        override fun updateDrawState(ds: TextPaint) {
            applyCustomTypeFace(ds, typeface)
        }

        override fun updateMeasureState(paint: TextPaint) {
            applyCustomTypeFace(paint, typeface)
        }

        private fun applyCustomTypeFace(paint: Paint, tf: Typeface?) {
            if (tf != null) {
                paint.typeface = tf
            } else {
                paint.typeface = Typeface.DEFAULT
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                binding.switchPermission.setImageResource(R.drawable.sw_on)
                Toast.makeText(this, R.string.granted_storage, Toast.LENGTH_SHORT).show()
            } else {
                binding.switchPermission.setImageResource(R.drawable.sw_off)
                setStoragePermission(this, (isStoragePermission(this) + 1))
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.switchNotification.setImageResource(R.drawable.sw_on)
                Toast.makeText(this, R.string.granted_notification, Toast.LENGTH_SHORT).show()
            } else {
                R.drawable.sw_off
                setNotificationPermission(this, (isNotificationPermission(this) + 1))
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
        if (checkPermissions(storagePermission)) {
            binding.switchPermission.setImageResource(R.drawable.sw_on)
            setStoragePermission(this, 0)
        } else {
            binding.switchPermission.setImageResource(R.drawable.sw_off)
        }
        if (checkPermissions(notificationPermission)) {
            binding.switchNotification.setImageResource(R.drawable.sw_on)
            setNotificationPermission(this, 0)
        } else {
            binding.switchNotification.setImageResource(R.drawable.sw_off)
        }
    }

}