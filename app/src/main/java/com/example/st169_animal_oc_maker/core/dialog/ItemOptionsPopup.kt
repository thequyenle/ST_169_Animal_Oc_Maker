package com.example.st169_animal_oc_maker.core.dialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import com.example.st169_animal_oc_maker.core.extensions.onSingleClick
import com.example.st169_animal_oc_maker.databinding.DialogItemOptionsBinding

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
            113.dpToPx(),
            126.dpToPx(),
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

    fun showAtLocation(itemRootView: View) {
        // Calculate position to show at center of the item (not the button)
        val location = IntArray(2)
        itemRootView.getLocationOnScreen(location)

        val popupWidth = 113.dpToPx()
        val popupHeight = 126.dpToPx()

        // Center popup in the middle of the item
        val x = location[0] + (itemRootView.width / 2) - (popupWidth / 2)
        val y = location[1] + (itemRootView.height / 2) - (popupHeight / 2)

        popupWindow?.showAtLocation(itemRootView, Gravity.NO_GRAVITY, x, y)
    }

    fun dismiss() {
        popupWindow?.dismiss()
    }

    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}