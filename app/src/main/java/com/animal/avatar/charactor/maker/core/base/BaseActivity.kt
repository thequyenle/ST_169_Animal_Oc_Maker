package com.animal.avatar.charactor.maker.core.base

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity

import androidx.viewbinding.ViewBinding
import com.animal.avatar.charactor.maker.R
import com.animal.avatar.charactor.maker.core.extensions.handleBackLeftToRight
import com.animal.avatar.charactor.maker.core.extensions.hideNavigation
import com.animal.avatar.charactor.maker.core.helper.LanguageHelper
import com.animal.avatar.charactor.maker.core.helper.SharePreferenceHelper
import com.animal.avatar.charactor.maker.core.helper.SoundHelper
import com.animal.avatar.charactor.maker.dialog.WaitingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.lazy


abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {

    private var _binding: T? = null
    val binding get() = _binding!!

    protected abstract fun setViewBinding(): T

    protected abstract fun initView()

    protected abstract fun viewListener()

    open fun dataObservable() {}

    open fun initText() {}

    protected abstract fun initActionBar()

    open fun initAds() {}

    protected val loadingDialog: WaitingDialog by lazy {
        WaitingDialog(this)
    }
    protected val sharePreference: SharePreferenceHelper by lazy {
        SharePreferenceHelper(this)
    }

    protected lateinit var toast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.setLocale(this)
        _binding = setViewBinding()
        setContentView(binding.root)
        setUpUI()

    }

    private fun setUpUI() {
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
        initView()
        if (!SoundHelper.isSoundNotNull(R.raw.touch)) {
            SoundHelper.loadSound(this, R.raw.touch)
        }
        initAds()
        dataObservable()
        viewListener()
        initActionBar()
        initText()
    }

    override fun onResume() {
        super.onResume()
        hideNavigation()
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        handleBackLeftToRight()
    }

    suspend fun showLoading() {
        withContext(Dispatchers.Main) {
            if (loadingDialog.isShowing.not()) {
                LanguageHelper.setLocale(this@BaseActivity)
                loadingDialog.show()
            }
        }
    }


    suspend fun dismissLoading(isBlack: Boolean = false) {
        withContext(Dispatchers.Main) {
            if (loadingDialog.isShowing) {
                loadingDialog.dismiss()
                hideNavigation(isBlack)
            }
        }
    }

    fun showToast(content: Any) {
        if (toast != null){
            toast.cancel()
        }
        val contentString = when (content) {
            is String -> content
            is Int -> getString(content)
            else -> {""}
        }
        toast = Toast.makeText(this, contentString, Toast.LENGTH_SHORT)
        toast.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
