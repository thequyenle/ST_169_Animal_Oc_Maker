package com.example.st181_halloween_maker.core.dialog

import android.app.Activity
import android.widget.Toast
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseDialog2

import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.databinding.MpDialogBinding
import kotlin.apply
import kotlin.text.toInt

class RateDialog(context: Activity) : BaseDialog2<MpDialogBinding>(context, false) {
    var i = 0
    private lateinit var onPress: OnPress
    override fun getContentView(): Int = R.layout.mp_dialog
    interface OnPress {
        fun send(rate: Float)
        fun rating()
        fun cancel()
        fun later()
    }

    override fun initView() {
        binding.apply {

        }
    }

    fun init(onPress: OnPress?) {
        this.onPress = onPress!!
    }

    override fun bindView() {
        binding.btnCancel.onSingleClick {
            dismiss()
            onPress.cancel()
        }
        binding.btnVote.setOnClickListener {
            when (i) {
                0 -> {
                    Toast.makeText(
                        context,
                        context.getText(R.string.rate_us_0),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    onPress.rating()
                }
            }
        }
        binding.ll1.setOnRatingChangeListener { ratingBar, rating, fromUser ->
            i = rating.toInt()
            when (i) {
                0 -> {
                    setView(R.string.zero_star_title, R.string.zero_star, R.drawable.ic_rate_rero)
                }

                1 -> {
                    setView(R.string.one_start_title, R.string.one_start, R.drawable.ic_rate_one)
                }

                2 -> {
                    setView(R.string.two_start_title, R.string.two_start, R.drawable.ic_rate_three)
                }

                3 -> {
                    setView(R.string.three_start_title, R.string.three_start, R.drawable.ic_rate_two)
                }

                4 -> {
                    setView(R.string.four_start_title, R.string.four_start, R.drawable.ic_rate_four)
                }

                5 -> {
                    setView(R.string.five_start_title, R.string.five_start, R.drawable.ic_rate_five)
                }
            }
        }
    }

    fun setView(tv1: Int, tv2: Int, img: Int) {
        binding.tv1.text = (context.resources.getString(tv1))
        binding.tv2.text = (context.resources.getString(tv2))
        binding.imvAvtRate.setImageResource(img)
    }
}