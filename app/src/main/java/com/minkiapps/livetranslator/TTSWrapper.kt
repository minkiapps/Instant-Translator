package com.minkiapps.livetranslator

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Pair
import com.huawei.hms.mlsdk.tts.*
import com.minkiapps.livetranslator.translation.Translation
import timber.log.Timber
import kotlin.collections.HashMap

class TTSWrapper {

    private lateinit var mlCNTtsEngine: MLTtsEngine
    private lateinit  var mlENTtsEngine: MLTtsEngine
    private var ttsListener: TTSListener? = null

    private val engineMap : HashMap<String, MLTtsEngine> = HashMap()

    private val handler = Handler(Looper.getMainLooper())

    @Synchronized
    fun speak(translation: Translation, text : String) {

    }

    fun speakEnglish(text: String) {
        mlENTtsEngine.speak(text, MLTtsEngine.QUEUE_FLUSH)
    }

    fun speakChinese(text: String) {
        mlCNTtsEngine.speak(text, MLTtsEngine.QUEUE_FLUSH)
    }

    private fun createCNTtsEngine() {
        val mlConfigs = MLTtsConfig()
            .setLanguage(MLTtsConstants.TTS_ZH_HANS)
            .setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_ZH)
            .setSpeed(1f)
            .setVolume(1f)
        val callback: MLTtsCallback = object : MLTtsCallback {
            override fun onError(taskId: String, err: MLTtsError) {
                if (ttsListener != null) {
                    handler.post { ttsListener?.onError(err) }
                }
            }

            override fun onWarn(taskId: String, warn: MLTtsWarn) {
                Timber.w("TaskId: $taskId Message ${warn.warnMsg}")
            }

            override fun onRangeStart(taskId: String, start: Int, end: Int) {
                Timber.w("TaskId: $taskId Start $start End: $end")
            }

            override fun onAudioAvailable(
                s: String,
                mlTtsAudioFragment: MLTtsAudioFragment,
                i: Int,
                pair: Pair<Int, Int>,
                bundle: Bundle
            ) {
                Timber.d(s)
            }

            override fun onEvent(taskId: String, eventName: Int, bundle: Bundle?) {
                when (eventName) {
                    MLTtsConstants.EVENT_PLAY_STOP -> if (ttsListener != null) {
                        handler.post { ttsListener?.onSpeechEnded() }
                    }
                }
            }
        }
        mlCNTtsEngine = MLTtsEngine(mlConfigs)
        mlCNTtsEngine.setTtsCallback(callback)
    }

    private fun createENTtsEngine() {
        val mlConfigs = MLTtsConfig()
            .setLanguage(MLTtsConstants.TTS_EN_US)
            .setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_EN)
            .setSpeed(1f)
            .setVolume(1f)
        val callback: MLTtsCallback = object : MLTtsCallback {
            override fun onError(taskId: String, err: MLTtsError) {
                if (ttsListener != null) {
                    handler.post { ttsListener?.onError(err) }
                }
            }

            override fun onWarn(taskId: String, warn: MLTtsWarn) {
                Timber.w("TaskId: $taskId Message ${warn.warnMsg}")
            }

            override fun onRangeStart(taskId: String, start: Int, end: Int) {
                Timber.d("TaskId: $taskId Start: $start End: $end")
            }

            override fun onAudioAvailable(
                s: String,
                mlTtsAudioFragment: MLTtsAudioFragment,
                i: Int,
                pair: Pair<Int, Int>,
                bundle: Bundle
            ) {
                Timber.d(s)
            }

            override fun onEvent(taskId: String, eventName: Int, bundle: Bundle?) {
                when (eventName) {
                    MLTtsConstants.EVENT_PLAY_STOP -> if (ttsListener != null) {
                        handler.post { ttsListener?.onSpeechEnded() }
                    }
                }
            }
        }
        mlENTtsEngine = MLTtsEngine(mlConfigs)
        mlENTtsEngine.setTtsCallback(callback)
    }

    fun stop() {
        //mlENTtsEngine.stop()
        //mlCNTtsEngine.stop()
    }

    fun shutdown() {
        //mlENTtsEngine.shutdown()
        //mlCNTtsEngine.shutdown()
    }

    fun setTtsListener(ttsListener: TTSListener) {
        this.ttsListener = ttsListener
    }

    interface TTSListener {
        fun onSpeechEnded()
        fun onError(err: MLTtsError)
    }
}