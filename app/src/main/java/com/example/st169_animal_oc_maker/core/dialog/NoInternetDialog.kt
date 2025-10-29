package com.example.st169_animal_oc_maker.core.dialog

import android.app.Activity
import com.example.st169_animal_oc_maker.R
import com.example.st169_animal_oc_maker.core.base.BaseDialog

import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.databinding.DialogNoInternetBinding
import kotlin.apply

class NoInternetDialog (val context: Activity) : BaseDialog<DialogNoInternetBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_no_internet
    override val isCancel: Boolean = false  // Disable back button
    override val isBack: Boolean = false     //  Disable back gesture
    var onOkClick: (() -> Unit)? = null

    override fun initView() {
        //  FIX: Make dialog non-cancelable - only closable via OK button
        setCancelable(false)

        binding.apply {
//            txtCheck.select()
        }
    }

    override fun initAction() {
        binding.btnOk.onSingleClick {
            onOkClick?.invoke()
            dismiss()
        }

        //  REMOVED: binding.main.onSingleClick - no longer allow closing by clicking outside
        // User must click OK button to close
    }

    override fun onDismissListener() {

    }
}