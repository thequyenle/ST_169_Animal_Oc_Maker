package com.example.st181_halloween_maker.ui.language

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.base.BaseActivity
import com.example.st181_halloween_maker.core.extensions.handleBack
import com.example.st181_halloween_maker.core.extensions.hide
import com.example.st181_halloween_maker.core.extensions.onSingleClick
import com.example.st181_halloween_maker.core.extensions.select
import com.example.st181_halloween_maker.core.extensions.show
import com.example.st181_halloween_maker.core.extensions.startIntentAnim
import com.example.st181_halloween_maker.core.utils.DataLocal.getLanguageList
import com.example.st181_halloween_maker.core.utils.KeyApp.INTENT_KEY
import com.example.st181_halloween_maker.core.utils.SystemUtils
import com.example.st181_halloween_maker.core.utils.SystemUtils.setFirstLang
import com.example.st181_halloween_maker.core.utils.SystemUtils.setPreLanguage
import com.example.st181_halloween_maker.databinding.ActivityLanguageBinding
import com.example.st181_halloween_maker.ui.home.HomeActivity
import com.example.st181_halloween_maker.ui.intro.IntroActivity
import com.girlmaker.create.avatar.creator.model.LanguageModel
import kotlin.code
import kotlin.system.exitProcess

class LanguageActivity : BaseActivity<ActivityLanguageBinding>() {
    private var listLanguage: ArrayList<LanguageModel> = arrayListOf()
    private var codeLang: String = ""
    private var isFirstAccess: Boolean = true
    private val languageAdapter by lazy {
        LanguageAdapter(this)
    }
    override fun setViewBinding(): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initData()
        initRcv()
    }

    override fun viewListener() {
        binding.apply {
            btnBack.onSingleClick{
                handleBack()
            }
            btnDone.onSingleClick {
                handleDoneFirst()
            }
            btnChangSetting.onSingleClick {
                handleChangeSetting()
            }
            txtLanguageStart.select()
            txtLanguageCenter.select()
        }

        // Handle RecyclerView item clicks - chỉ select, chưa lưu
        languageAdapter.onItemClick = { selected ->
            codeLang = selected.code
            // Chỉ select, không tự động lưu hay chuyển màn
            // Phải click btnDone hoặc btnChangSetting mới lưu và chuyển màn
        }
    }

    override fun initText() {

    }
    private fun initData(){
        binding.apply {
            val intent = intent.getStringExtra(INTENT_KEY)
            if(intent != null){
                isFirstAccess = false
                codeLang = SystemUtils.getPreLanguage(this@LanguageActivity)
                txtLanguageCenter.show()
                btnDone.hide()
                btnBack.show()
                btnChangSetting.hide()
            }else{
                isFirstAccess = true
                codeLang = SystemUtils.getPreLanguage(this@LanguageActivity)
                txtLanguageStart.show()
                btnDone.show()
                btnBack.hide()
                btnChangSetting.hide()
            }

            if(!SystemUtils.isFirstLang(this@LanguageActivity)){
                btnDone.hide()
                txtLanguageCenter.show()
                btnBack.show()
                btnChangSetting.show()
                txtLanguageStart.hide()
            }else{
                btnDone.show()
                txtLanguageStart.show()
                btnChangSetting.hide()
            }
        }
    }

    private fun initRcv(){
        binding.apply {

            listLanguage.clear()
            listLanguage.addAll(getLanguageList())

            val lang = SystemUtils.getPreLanguage(this@LanguageActivity)
            var selectedPosition = -1

            // Tìm và đưa ngôn ngữ đã lưu lên đầu danh sách
            for(i in listLanguage.indices){
                if(listLanguage[i].code == lang){
                    val matchingLanguage = listLanguage[i].copy()
                    listLanguage.removeAt(i)
                    listLanguage.add(0, matchingLanguage)
                    selectedPosition = 0
                    break
                }
            }

            // Nếu đã chọn ngôn ngữ rồi (vào từ Settings), hiện ngôn ngữ đã lưu ở trạng thái selected
            if(!SystemUtils.isFirstLang(this@LanguageActivity)){
                // Đã chọn ngôn ngữ rồi → hiện selected
                if(selectedPosition != -1){
                    listLanguage[0].activate = true
                    codeLang = lang
                }
            } else {
                // Lần đầu (onboarding) → reset tất cả về trạng thái chưa chọn
                codeLang = ""
                listLanguage.forEach { it.activate = false }
            }

            rcv.adapter = languageAdapter
            rcv.itemAnimator = null
            languageAdapter.submitList(listLanguage)

            // Scroll đến ngôn ngữ đã chọn nếu đã từng chọn ngôn ngữ
            if(selectedPosition != -1 && !SystemUtils.isFirstLang(this@LanguageActivity)){
                rcv.post {
                    rcv.smoothScrollToPosition(selectedPosition)
                }
            }
        }
    }

    private fun handleDoneFirst(){
        if(codeLang == ""){
            Toast.makeText(this, R.string.not_select_lang, Toast.LENGTH_SHORT).show()
        }else{
            setPreLanguage(this@LanguageActivity, codeLang)
            setFirstLang(this,false)

            startIntentAnim(IntroActivity::class.java)
            finishAffinity()
        }
    }

    private fun handleChangeSetting(){
        if(codeLang == ""){
            Toast.makeText(this, R.string.not_select_lang, Toast.LENGTH_SHORT).show()
        }else{
            setPreLanguage(this@LanguageActivity, codeLang)
            startIntentAnim(HomeActivity::class.java)
            finishAffinity()
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if(!SystemUtils.isFirstLang(this)){
            handleBack()
        }else{
            exitProcess(0)
        }
    }

}