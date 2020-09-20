package com.minkiapps.livetranslator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huawei.hmf.tasks.Tasks
import com.huawei.hms.mlsdk.model.download.MLLocalModelManager
import com.huawei.hms.mlsdk.model.download.MLModelDownloadStrategy
import com.huawei.hms.mlsdk.model.download.MLRemoteModel
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslatorModel
import kotlinx.coroutines.*
import timber.log.Timber

class MainViewModel : ViewModel() {

    private val manager = MLLocalModelManager.getInstance()
    private val chineseModel = MLLocalTranslatorModel.Factory(ModelName.zh.name).create()
    private val englishModel = MLLocalTranslatorModel.Factory(ModelName.en.name).create()

    private val downLoadProgressData : MutableLiveData<DownLoadProgress> = MutableLiveData()
    private val modelReadyData : MutableLiveData<Boolean> = MutableLiveData()

    fun downLoadProgressLiveData() : LiveData<DownLoadProgress> = downLoadProgressData
    fun modelReadyLiveData() : LiveData<Boolean> = modelReadyData

    init {
        viewModelScope.launch {
            try {
                initModels()
            } catch (e : Exception) {
                Timber.e(e, "Failed to download language models")
            }
        }
    }

    private suspend fun initModels() = coroutineScope {
        val chinesModelAvailableTask = async(Dispatchers.IO) {
            val available = Tasks.await(manager.isModelExist(chineseModel))
            if(!available) {
                downloadModel(ModelName.zh, chineseModel)
            }
            downLoadProgressData.postValue(DownLoadProgress(ModelName.zh, 100, 100))
        }

        val englishModelAvailableTask = async(Dispatchers.IO) {
            val available = Tasks.await(manager.isModelExist(englishModel))
            if(!available) {
                downloadModel(ModelName.en, englishModel)
            }
            downLoadProgressData.postValue(DownLoadProgress(ModelName.en, 100, 100))
        }

        chinesModelAvailableTask.await()
        englishModelAvailableTask.await()
        modelReadyData.postValue(true)
    }

    private fun downloadModel(modelNameName : ModelName, model: MLRemoteModel) {
        Tasks.await(manager.downloadModel(
            model,
            MLModelDownloadStrategy.Factory().create()
        ) { alreadyDownLength, totalLength ->
            downLoadProgressData.postValue(DownLoadProgress(modelNameName, alreadyDownLength, totalLength))
        })
    }

    data class DownLoadProgress(val modelName : ModelName, val alreadyDownLength : Long, val totalLength : Long)

    enum class ModelName {
        zh, en
    }
}