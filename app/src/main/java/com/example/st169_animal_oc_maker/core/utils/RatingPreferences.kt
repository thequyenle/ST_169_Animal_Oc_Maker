package com.example.st169_animal_oc_maker.core.utils

import android.content.Context
import android.util.Log

/**
 * Manages rating-related preferences with low coupling
 * Can be reused across multiple activities
 */
class RatingPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Check if user has already rated the app
     */
    fun isRated(): Boolean {
        val rated = prefs.getBoolean(KEY_RATED, false)
        Log.d("RatingPreferences", "isRated() = $rated")
        return rated
    }

    /**
     * Set the rating status
     */
    fun setRated(rated: Boolean) {
        Log.d("RatingPreferences", "setRated($rated)")
        prefs.edit().putBoolean(KEY_RATED, rated).apply()
    }

    /**
     * Get the current back press count
     */
    fun getBackPressCount(): Int {
        val count = prefs.getInt(KEY_BACK_PRESS_COUNT, 0)
        Log.d("RatingPreferences", "getBackPressCount() = $count")
        return count
    }

    /**
     * Increment and return the back press count
     */
    fun incrementBackPressCount(): Int {
        val currentCount = getBackPressCount()
        val newCount = currentCount + 1
        prefs.edit().putInt(KEY_BACK_PRESS_COUNT, newCount).apply()
        Log.d("RatingPreferences", "incrementBackPressCount() from $currentCount to $newCount")
        return newCount
    }

    /**
     * Check if the current back press count is odd number
     */
    fun isOddBackPress(): Boolean {
        val count = getBackPressCount()
        return count % 2 == 1
    }

    companion object {
        private const val PREFS_NAME = "AppSettings"
        private const val KEY_RATED = "is_rated"
        private const val KEY_BACK_PRESS_COUNT = "back_press_count"
    }
}
