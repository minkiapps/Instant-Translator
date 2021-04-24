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
import com.huawei.hms.mlsdk.tts.MLTtsLocalModel
import com.minkiapps.livetranslator.translation.mlLangModels
import com.minkiapps.livetranslator.utils.await
import kotlinx.coroutines.*
import timber.log.Timber
import kotlin.math.round

class MainViewModel : ViewModel() {

    private val manager = MLLocalModelManager.getInstance()

    private val modelDownLoadProgresses = HashMap<MLRemoteModel, Int>()

    private val downLoadProgressData : MutableLiveData<Int> = MutableLiveData()
    private val modelReadyData : MutableLiveData<Boolean> = MutableLiveData()

    fun downLoadProgressLiveData() : LiveData<Int> = downLoadProgressData
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
        if(BuildConfig.DEBUG) {
            Timber.d("Available TTS models: ${manager.getModels(MLTtsLocalModel::class.java).await()}")
            Timber.d("Available Translator models: ${manager.getModels(MLLocalTranslatorModel::class.java).await()}")
        }

        mlLangModels.map { m ->
            if(!manager.isModelExist(m.translatorModel).await()) {
                modelDownLoadProgresses[m.translatorModel] = 0
            }

            if(!manager.isModelExist(m.ttsModel).await()) {
                modelDownLoadProgresses[m.ttsModel] = 0
            }
        }

        val deferredList = modelDownLoadProgresses.map { m ->
            async(Dispatchers.IO) {
                try {
                    downloadRemoteModel(m.key)
                } catch (e : Exception) {
                    if(BuildConfig.DEBUG) {
                        Timber.e(e, "Failed to download ${m.key}")
                    } else {
                        throw e
                    }
                }

                onDownloadUpdateProgress(m.key, 100)
            }
        }

        deferredList.awaitAll()
        modelReadyData.postValue(true)
    }

    private fun downloadRemoteModel(model: MLRemoteModel) {
        Tasks.await(manager.downloadModel(
            model,
            MLModelDownloadStrategy.Factory().create()
        ) { alreadyDownLength, totalLength ->
            onDownloadUpdateProgress(model, round(alreadyDownLength * 1f / totalLength * 100).toInt())
            val progress = modelDownLoadProgresses.map { it.value }.sum() / modelDownLoadProgresses.size
            downLoadProgressData.postValue(progress)
        })
    }

    private fun onDownloadUpdateProgress(model: MLRemoteModel, progress : Int) {
        modelDownLoadProgresses[model] = progress
        val wholeProgress = modelDownLoadProgresses.map { it.value }.sum() / modelDownLoadProgresses.size
        downLoadProgressData.postValue(wholeProgress)
    }
}