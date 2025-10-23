package com.example.st181_halloween_maker.core.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.MediaStore.Audio.AudioColumns.IS_NOTIFICATION
import android.util.Log
import androidx.core.app.ShareCompat
import androidx.lifecycle.MutableLiveData
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewManagerFactory
import com.example.st181_halloween_maker.data.custom.CustomizeModel
import com.example.st181_halloween_maker.core.utils.KeyApp.COUNT_BACK
import com.example.st181_halloween_maker.core.utils.KeyApp.COUNT_BACK_KEY
import com.example.st181_halloween_maker.core.utils.KeyApp.FIRST_APP_1
import com.example.st181_halloween_maker.core.utils.KeyApp.FIRST_LANG
import com.example.st181_halloween_maker.core.utils.KeyApp.FIRST_LANG_KEY
import com.example.st181_halloween_maker.core.utils.KeyApp.FIRST_PERMISSION
import com.example.st181_halloween_maker.core.utils.KeyApp.FIRST_PERMISSION_KEY
import com.example.st181_halloween_maker.core.utils.KeyApp.IS_STORAGE
import com.example.st181_halloween_maker.core.utils.KeyApp.KEY_OPEN_ADS
import com.example.st181_halloween_maker.core.utils.KeyApp.NOTIFICATION_KEY
import com.example.st181_halloween_maker.core.utils.KeyApp.RATE
import com.example.st181_halloween_maker.core.utils.KeyApp.RATE_KEY
import com.example.st181_halloween_maker.core.utils.KeyApp.STORAGE_KEY
import java.lang.Void
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.apply
import kotlin.let
import kotlin.system.exitProcess
import kotlin.text.equals
import kotlin.text.isEmpty
import kotlin.text.isNotEmpty

object SystemUtils {

    private var myLocale: Locale? = null

    val storagePermission = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)

        else -> arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        emptyArray()
    }

    var FIRST_ACCESS = false

    var lastClickTime = 0L

    fun setLocale(context: Context) {
        val language = getPreLanguage(context)
        if (language.isEmpty()) {
            val config = Configuration()
            val locale = Locale.getDefault()
            Locale.setDefault(locale)
            config.setLocale(locale)
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        } else {
            changeLang(language, context)
        }
    }

    fun changeLang(lang: String, context: Context) {
        if (lang.equals("", ignoreCase = true)) return
        myLocale = Locale(lang)
        saveLocale(context, lang)
        Locale.setDefault(myLocale!!)
        val config = Configuration()
        config.setLocale(myLocale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun saveLocale(context: Context, lang: String) {
        setPreLanguage(context, lang)
    }

    fun setPreLanguage(context: Context, language: String) {
        if (language.isNotEmpty()) {
            val preferences: SharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
            val editor = preferences.edit()
            editor.putString("KEY_LANGUAGE", language)
            editor.apply()
        }
    }

    fun getPreLanguage(context: Context): String {
        val preferences: SharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        return preferences.getString("KEY_LANGUAGE", "en") ?: "en"
    }

    fun isFirstLang(context: Context): Boolean {
        val preferences: SharedPreferences = context.getSharedPreferences(FIRST_LANG, Context.MODE_PRIVATE)
        return preferences.getBoolean(FIRST_LANG_KEY, true)
    }
    fun getFirstLang(context: Context): Boolean {
        val preferences: SharedPreferences =
                context.getSharedPreferences(FIRST_LANG, Context.MODE_PRIVATE)
        return preferences.getBoolean(FIRST_LANG_KEY, true)
    }

    fun setFirstApp(context: Context, isFirstAccess: Boolean) {
        val preferences: SharedPreferences = context.getSharedPreferences(FIRST_APP_1, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(FIRST_APP_1, isFirstAccess)
        editor.apply()
    }
    fun saveAdsConfig(context: Context, openAds: Boolean) {
        val prefs = context.getSharedPreferences(KeyApp.PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putBoolean(KEY_OPEN_ADS, openAds)
            apply()
        }
    }

    // Hàm để lấy và sử dụng config
    fun useAdsConfig(context: Context): Boolean {
        val prefs = context.getSharedPreferences(KeyApp.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_OPEN_ADS, true) // false là giá trị mặc định nếu chưa có
    }

    fun getFirstApp(context: Context): Boolean {
        val preferences: SharedPreferences = context.getSharedPreferences(FIRST_APP_1, Context.MODE_PRIVATE)
        return preferences.getBoolean(FIRST_APP_1, false)
    }

    fun setFirstLang(context: Context, isFirstAccess: Boolean) {
        val preferences: SharedPreferences = context.getSharedPreferences(FIRST_LANG, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(FIRST_LANG_KEY, isFirstAccess)
        editor.apply()
    }

    fun getFirstPermission(context: Context): Boolean {
        val preferences: SharedPreferences = context.getSharedPreferences(FIRST_PERMISSION, Context.MODE_PRIVATE)
        return preferences.getBoolean(FIRST_PERMISSION_KEY, true)
    }


    fun isFirstPermission(context: Context): Boolean {
        val preferences: SharedPreferences = context.getSharedPreferences(FIRST_PERMISSION, Context.MODE_PRIVATE)
        return preferences.getBoolean(FIRST_PERMISSION_KEY, true)
    }

    fun setFirstPermission(context: Context, isFirstAccess: Boolean) {
        val preferences: SharedPreferences = context.getSharedPreferences(FIRST_PERMISSION, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(FIRST_PERMISSION_KEY, isFirstAccess)
        editor.apply()
    }

    fun isRate(context: Context): Boolean {
        val preferences: SharedPreferences = context.getSharedPreferences(RATE, Context.MODE_PRIVATE)
        return preferences.getBoolean(RATE_KEY, false)
    }

    fun setRate(context: Context, isFirstAccess: Boolean) {
        val preferences: SharedPreferences = context.getSharedPreferences(RATE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(RATE_KEY, isFirstAccess)
        editor.apply()
    }

    fun setCountBack(context: Context, countBack: Int) {
        val preferences: SharedPreferences = context.getSharedPreferences(COUNT_BACK, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(COUNT_BACK_KEY, countBack)
        editor.apply()
    }

    fun isCountBack(context: Context): Int {
        val preferences: SharedPreferences = context.getSharedPreferences(COUNT_BACK, Context.MODE_PRIVATE)
        return preferences.getInt(COUNT_BACK_KEY, 0)
    }

    fun Activity.shareApp() {
        ShareCompat.IntentBuilder.from(this).setType("text/plain").setChooserTitle("Chooser title")
            .setText("http://play.google.com/store/apps/details?id=" + (this).packageName).startChooser()
    }

    fun Activity.policy() {
        val url = "https://sites.google.com/view/girl-maker-avatar-creator/"
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(url)
        startActivity(i)
    }

    fun reviewApp(context: Activity, isBackPress: Boolean) {
        val manager = ReviewManagerFactory.create(context)
        val request = manager.requestReviewFlow();
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                Log.e("ReviewInfo", "" + reviewInfo.toString())
                val flow = (context as Activity?)?.let { manager.launchReviewFlow(it, reviewInfo) }
                flow?.addOnCompleteListener { task2: Task<Void> ->
                    if (isBackPress) {
                        exitProcess(0)
                    }
                }
            } else {
                if (isBackPress) {
                    exitProcess(0)
                }
            }
        }
    }
    fun getStoragePermission(context: Context): Int {
        val preferences: SharedPreferences = context.getSharedPreferences(IS_STORAGE, Context.MODE_PRIVATE)
        return preferences.getInt(STORAGE_KEY, 0)
    }
    fun isStoragePermission(context: Context): Int {
        val preferences: SharedPreferences = context.getSharedPreferences(IS_STORAGE, Context.MODE_PRIVATE)
        return preferences.getInt(STORAGE_KEY, 0)
    }

    fun setStoragePermission(context: Context, count: Int) {
        val preferences: SharedPreferences = context.getSharedPreferences(IS_STORAGE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(STORAGE_KEY, count)
        editor.apply()
    }


    fun isNotificationPermission(context: Context): Int {
        val preferences: SharedPreferences = context.getSharedPreferences(IS_NOTIFICATION, Context.MODE_PRIVATE)
        return preferences.getInt(NOTIFICATION_KEY, 0)
    }

    fun setNotificationPermission(context: Context, count: Int) {
        val preferences: SharedPreferences = context.getSharedPreferences(IS_NOTIFICATION, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(NOTIFICATION_KEY, count)
        editor.apply()
    }

    private val shimmer =
            Shimmer.AlphaHighlightBuilder().setDuration(1800).setBaseAlpha(0.7f).setHighlightAlpha(0.6f).setDirection(Shimmer.Direction.LEFT_TO_RIGHT).setAutoStart(true)
                .build()

    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(shimmer)
    }

    @SuppressLint("SimpleDateFormat")
    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return dateFormat.format(Date())
    }

    var CURRENT_DATE = ""
    var All_DATA : ArrayList<CustomizeModel> = arrayListOf()



    var isConnectInternet = MutableLiveData<Boolean>()
    var isDataAPI = false
    var isFailBaseURL: Boolean = false
    var isCallDataAlready = false


    val avatarList: ArrayList<String> = arrayListOf()
    var isLoadFailAllAPI: Boolean = true



}