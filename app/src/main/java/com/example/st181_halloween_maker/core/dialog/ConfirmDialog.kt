package com.example.st181_halloween_maker.core.dialog

import android.app.Activity
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseDialog

import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.extensions.select
import com.example.st181_halloween_maker.databinding.DialogConfirmBinding

import kotlin.apply

class ConfirmDialog(val context: Activity, val title: Int, val description: Int) :
    BaseDialog<DialogConfirmBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_confirm
    override val isCancel: Boolean = false
    override val isBack: Boolean = false

    var onNoClick: (() -> Unit)? = null
    var onYesClick: (() -> Unit)? = null
    var onDismissClick: (() -> Unit)? = null
    override fun initView() {
        initText()
    }

    override fun initAction() {
        binding.apply {
            btnNo.onSingleClick {
                onNoClick?.invoke()
            }
            btnYes.onSingleClick {
                onYesClick?.invoke()
            }
            main.onSingleClick {
                onDismissClick?.invoke()
            }
        }
    }

    override fun onDismissListener() {

    }

    private fun initText() {
        binding.apply {
            txtTitle.text = ContextCompat.getString(context, title)
            txtDescription.text = ContextCompat.getString(context, description)
            txtDescription.select()
            txtYes.select()
        }
    }
    private fun setGradientHeightTextColor(textView: TextView) {
        val paint = textView.paint
        val height = textView.textSize
        val textShader = LinearGradient(
            0f, 0f, 0f, height, intArrayOf(Color.parseColor("#0D8AFC"), Color.parseColor("#33F0B0")), null, Shader.TileMode.CLAMP
        )
        textView.paint.shader = textShader
    }
}