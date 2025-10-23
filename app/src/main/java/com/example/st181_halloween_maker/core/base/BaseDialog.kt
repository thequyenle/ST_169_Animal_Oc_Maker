package com.example.st181_halloween_maker.core.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.viewbinding.ViewBinding
import com.example.st181_halloween_maker.core.utils.SystemUtils


abstract class BaseDialog<VB : ViewBinding>(
        private val context: Context,
        private val gravity: Int = Gravity.CENTER,
        private val maxWidth: Boolean = false,
        private val maxHeight: Boolean = false,
) : Dialog(context) {
    lateinit var binding: VB
    abstract val layoutId: Int
    abstract val isCancel: Boolean
    abstract val isBack: Boolean


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemUtils.setLocale(context)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), layoutId, null, false)
        setContentView(binding.root)
        setCancelable(isBack)
        this.setCanceledOnTouchOutside(isCancel)
        val window = this.window!!
        window.setGravity(gravity)
        if (maxWidth && maxHeight) {
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        } else if (maxWidth && !maxHeight) {
            window.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        } else if (!maxWidth && !maxHeight) {
            window.setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        initView()
        initAction()

    }

    abstract fun initView()
    abstract fun initAction()
    abstract fun onDismissListener()

}