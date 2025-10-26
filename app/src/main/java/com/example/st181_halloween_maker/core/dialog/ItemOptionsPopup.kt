package com.example.st181_halloween_maker.core.dialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.databinding.DialogItemOptionsBinding

class ItemOptionsPopup(private val context: Context) {
    private var popupWindow: PopupWindow? = null
    private lateinit var binding: DialogItemOptionsBinding

    var onDeleteClick: (() -> Unit)? = null
    var onShareClick: (() -> Unit)? = null
    var onDownloadClick: (() -> Unit)? = null

    init {
        setupPopup()
    }

    private fun setupPopup() {
        binding = DialogItemOptionsBinding.inflate(LayoutInflater.from(context))

        popupWindow = PopupWindow(
            binding.root,
            126.dpToPx(),
            113.dpToPx(),
            true
        ).apply {
            isOutsideTouchable = true
            isFocusable = true
            elevation = 8f
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.apply {
            btnDelete.onSingleClick {
                onDeleteClick?.invoke()
                dismiss()
            }

            btnShare.onSingleClick {
                onShareClick?.invoke()
                dismiss()
            }

            btnDownload.onSingleClick {
                onDownloadClick?.invoke()
                dismiss()
            }
        }
    }

    fun showAtLocation(anchorView: View) {
        // Calculate position to show at center of the anchor view
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        val x = location[0] + (anchorView.width / 2) - (126.dpToPx() / 2)
        val y = location[1] + (anchorView.height / 2) - (113.dpToPx() / 2)

        popupWindow?.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y)
    }

    fun dismiss() {
        popupWindow?.dismiss()
    }

    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}
