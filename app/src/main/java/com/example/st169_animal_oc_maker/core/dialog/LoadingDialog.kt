package com.animal.avatar.charactor.maker.core.dialog

import android.app.Activity
import com.animal.avatar.charactor.maker.R
import com.animal.avatar.charactor.maker.core.base.BaseDialog
import com.animal.avatar.charactor.maker.databinding.DialogLoadingBinding


class LoadingDialog(val context: Activity) : BaseDialog<DialogLoadingBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_loading
    override val isCancel: Boolean = false
    override val isBack: Boolean = false
    override fun initView() {
//        binding.txtDescription.select()
    }

    override fun initAction() {

    }

    override fun onDismissListener() {

    }
}