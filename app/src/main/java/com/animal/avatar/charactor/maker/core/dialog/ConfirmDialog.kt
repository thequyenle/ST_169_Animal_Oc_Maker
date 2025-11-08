package com.animal.avatar.charactor.maker.core.dialog

import android.app.Activity
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
//quyen
import com.lvt.ads.util.Admob
//quyen
import com.animal.avatar.charactor.maker.R
import com.animal.avatar.charactor.maker.core.base.BaseDialog

import com.animal.avatar.charactor.maker.core.extensions.onSingleClick
import com.animal.avatar.charactor.maker.core.extensions.select
import com.animal.avatar.charactor.maker.databinding.DialogConfirmBinding

import kotlin.apply

class ConfirmDialog(val context: Activity, val title: Int, val description: Int, var checkExit: Boolean = false) :
    BaseDialog<DialogConfirmBinding>(context, maxWidth = false, maxHeight = false) {
    override val layoutId: Int = R.layout.dialog_confirm
    override val isCancel: Boolean = false
    override val isBack: Boolean = false

    var onNoClick: (() -> Unit)? = null
    var onYesClick: (() -> Unit)? = null
    var onDismissClick: (() -> Unit)? = null
    override fun initView() {
        initText()

        //quyen
        if (checkExit) {
            binding.nativeAds.visibility = View.VISIBLE
            Admob.getInstance().loadNativeAd(
                context,
                context.getString(R.string.native_dialog),
                binding.nativeAds,
                R.layout.ads_native_avg2_white
            )
        }
        //quyen

        // Cố định kích thước dialog là 316dp
        val widthInDp = 316
        val widthInPx = (widthInDp * context.resources.displayMetrics.density).toInt()

        window?.setLayout(
            widthInPx,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
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