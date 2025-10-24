package com.example.st181_halloween_maker.ui.background

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import com.example.st181_halloween_maker.R
import com.example.st181_halloween_maker.core.extensions.showToast
import com.example.st181_halloween_maker.core.helper.BitmapHelper
import com.example.st181_halloween_maker.core.helper.InternetHelper
import com.example.st181_halloween_maker.core.helper.MediaHelper
import com.example.st181_halloween_maker.core.utils.DataLocal
import com.example.st181_halloween_maker.core.utils.SaveState
import com.example.st181_halloween_maker.core.utils.key.ValueKey
import com.example.st181_halloween_maker.data.custom.CustomizeModel
import com.girlmaker.create.avatar.creator.model.BackGroundModel
import com.girlmaker.create.avatar.creator.model.BgType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class BackgroundViewModel : ViewModel() {
    private var isDataFromAPI = false

    // Lưu vị trí character hiện tại
    private var currentCharacterPosition = -1

    // Lưu list character data
    private var allCharacterData: List<CustomizeModel> = emptyList()

    fun getListBackground(context: Context): ArrayList<BackGroundModel> {
        val list = ArrayList<BackGroundModel>()

        // Thêm item "None" đầu tiên
        list.add(BackGroundModel("", false, BgType.NONE))

        // Load backgrounds using existing getBgAsset (local + remote)
        val bgPaths = DataLocal.getBgAsset(context)
        bgPaths.forEach { path ->
            list.add(BackGroundModel(path, false, BgType.NORMAL))
        }

        return list
    }

    fun isDataAPI(): Boolean {
        return isDataFromAPI
    }

    fun checkDataInternet(context: Context, onSuccess: () -> Unit) {
        if (InternetHelper.checkInternet(context)) {
            onSuccess()
        } else {
            (context as? Activity)?.showToast(R.string.please_check_your_internet)
        }
    }

    fun saveImageFromView(context: Context, view: View): Flow<SaveState> = flow {
        emit(SaveState.Loading)

        try {
            val bitmap = BitmapHelper.createBimapFromView(view)
            MediaHelper.saveBitmapToInternalStorage(
                context,
                ValueKey.DOWNLOAD_ALBUM,
                bitmap
            ).collect { state ->
                emit(state)
            }
        } catch (e: Exception) {
            emit(SaveState.Error(e))
        }
    }.flowOn(Dispatchers.IO)

    fun updateCharacterBackground(position: Int, backgroundPath: String) {
        currentCharacterPosition = position
        // Lưu thông tin background cho character
        // Có thể mở rộng thêm logic lưu vào SharedPreferences hoặc Database
    }
}
