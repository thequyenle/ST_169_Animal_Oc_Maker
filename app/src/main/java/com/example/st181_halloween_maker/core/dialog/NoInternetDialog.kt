package com.example.st181_halloween_maker.core.dialog

import android.app.Activity
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseDialog

import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.databinding.DialogNoInternetBinding
import kotlin.apply

class NoInternetDialog (val context: Activity) : BaseDialog<DialogNoInternetBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_no_internet
    override val isCancel: Boolean = false
    override val isBack: Boolean = false
    var onOkClick: (() -> Unit)? = null
    var onDismissClick: (() -> Unit)? = null

    override fun initView() {
        binding.apply {
//            txtCheck.select()
        }
    }

    override fun initAction() {
        binding.btnOk.onSingleClick {
            onOkClick?.invoke()
        }
        binding.main.onSingleClick {
            onDismissClick?.invoke()
        }
    }

    override fun onDismissListener() {

    }
}