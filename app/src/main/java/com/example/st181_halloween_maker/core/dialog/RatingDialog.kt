package com.example.st181_halloween_maker.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.willy.ratingbar.ScaleRatingBar
import com.example.st181_halloween_maker.R

class RatingDialog(
    context: Context,
    private val onRatingSubmitted: ((Int) -> Unit)? = null,
    private val onDismiss: (() -> Unit)? = null
) : Dialog(context) {

    private var selectedRating = 0

    private lateinit var imvAvtRate: AppCompatImageView
    private lateinit var tv1: AppCompatTextView
    private lateinit var tv2: AppCompatTextView
    private lateinit var btnVote: AppCompatButton
    private lateinit var btnCancel: AppCompatTextView
    private lateinit var ratingBar: ScaleRatingBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_rating)

        // Make dialog non-cancelable (cannot close by clicking outside)
        setCancelable(false)
        setCanceledOnTouchOutside(false)

        // Set dialog properties
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        // Apply fullscreen using DialogHelper
       // DialogHelper.applyFullscreenToDialog(this)

        initViews()
        setupInitialState()
        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        // Don't reset to initial state here to prevent flickering
        // Initial state is already set in setupInitialState() called from onCreate()
    }

    private fun resetToInitialState() {
        // Reset rating
        selectedRating = 0
        ratingBar.rating = 0f

        // Reset icon và text về mặc định
        imvAvtRate.setImageResource(R.drawable.ic_ask)
        tv1.text = context.getString(R.string.do_you_like_the_app)
        tv2.text = context.getString(R.string.let_us_know_your_experience)

        // Disable button vote and set visual feedback
        btnVote.isEnabled = false
        btnVote.alpha = 0.5f
    }

    private fun initViews() {
        imvAvtRate = findViewById(R.id.imvAvtRate)
        tv1 = findViewById(R.id.tv1)
        tv2 = findViewById(R.id.tv2)
        btnVote = findViewById(R.id.btnVote)
        btnCancel = findViewById(R.id.btnCancel)
        ratingBar = findViewById(R.id.ratingBar)
    }

    private fun setupInitialState() {
        // Set icon mặc định là ic_ask (emoji hỏi)
        imvAvtRate.setImageResource(R.drawable.ic_ask)

        // Set text mặc định
        tv1.text = context.getString(R.string.do_you_like_the_app)
        tv2.text = context.getString(R.string.let_us_know_your_experience)

        // Disable button vote ban đầu and set visual feedback
        btnVote.isEnabled = false
        btnVote.alpha = 0.5f

        // Reset rating bar về 0 (tất cả sao empty)
        ratingBar.rating = 0f
        selectedRating = 0
    }

    private fun setupListeners() {
        // Rating bar change listener
        // Note: srb_clearRatingEnabled="false" in XML prevents clicking same star to deselect
        // But still allows swiping left to reach 0 stars
        ratingBar.setOnRatingChangeListener { ratingBarView, rating, fromUser ->
            Log.d("RatingDialog", "Rating changed: rating=$rating, fromUser=$fromUser, selectedRating=$selectedRating")

            if (fromUser) {
                val newRating = rating.toInt()
                Log.d("RatingDialog", "New rating: $newRating, Previous: $selectedRating")

                // If somehow the same rating comes through, ignore it
                if (newRating == selectedRating) {
                    Log.d("RatingDialog", "Same rating detected, ignoring")
                    return@setOnRatingChangeListener
                }

                // Update selected rating
                selectedRating = newRating
                Log.d("RatingDialog", "Updating UI for rating: $selectedRating")

                // Update UI based on new rating
                if (selectedRating == 0) {
                    // Show zero star state when swiping left to 0
                    Log.d("RatingDialog", "Showing zero star state")
                    imvAvtRate.setImageResource(R.drawable.ic_ask)
                    tv1.text = context.getString(R.string.do_you_like_the_app)
                    tv2.text = context.getString(R.string.let_us_know_your_experience)
                    btnVote.isEnabled = false
                    btnVote.alpha = 0.5f
                } else {
                    Log.d("RatingDialog", "Showing $selectedRating star state")
                    updateUIForRating(selectedRating)
                }
            }
        }

        // Vote button click
        btnVote.setOnClickListener {
            if (selectedRating > 0) {
                onRatingSubmitted?.invoke(selectedRating)
                dismiss()
            }
        }

        // Cancel button click
        btnCancel.setOnClickListener {
            dismiss()
        }

        // Set dismiss listener
        setOnDismissListener {
            onDismiss?.invoke()
        }
    }

    private fun updateUIForRating(rating: Int) {
        when (rating) {
            1 -> {
                imvAvtRate.setImageResource(R.drawable.ic_1star)
                tv1.text = context.getString(R.string.rating_oh_no)
                tv2.text = context.getString(R.string.rating_feedback_request)
            }
            2 -> {
                imvAvtRate.setImageResource(R.drawable.ic_2star)
                tv1.text = context.getString(R.string.rating_oh_no)
                tv2.text = context.getString(R.string.rating_feedback_request)
            }
            3 -> {
                imvAvtRate.setImageResource(R.drawable.ic_3star)
                tv1.text = context.getString(R.string.rating_oh_no)
                tv2.text = context.getString(R.string.rating_feedback_request)
            }
            4 -> {
                imvAvtRate.setImageResource(R.drawable.ic_4star)
                tv1.text = context.getString(R.string.rating_love_you)
                tv2.text = context.getString(R.string.rating_thanks)
            }
            5 -> {
                imvAvtRate.setImageResource(R.drawable.ic_5star)
                tv1.text = context.getString(R.string.rating_love_you)
                tv2.text = context.getString(R.string.rating_thanks)
            }
        }

        // Enable button vote and restore full opacity
        btnVote.isEnabled = true
        btnVote.alpha = 1.0f
    }

    companion object {
        /**
         * Hiển thị dialog rating
         * @param context Context
         * @param onRatingSubmitted Callback khi user submit rating (1-5)
         * @param onDismiss Callback khi dialog bị đóng
         */
        fun show(
            context: Context,
            onRatingSubmitted: ((Int) -> Unit)? = null,
            onDismiss: (() -> Unit)? = null
        ): RatingDialog {
            val dialog = RatingDialog(context, onRatingSubmitted, onDismiss)
            dialog.show()
            return dialog
        }
    }
}