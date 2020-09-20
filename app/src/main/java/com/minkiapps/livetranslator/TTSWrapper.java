package com.minkiapps.livetranslator;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;

import com.huawei.hms.mlsdk.tts.MLTtsAudioFragment;
import com.huawei.hms.mlsdk.tts.MLTtsCallback;
import com.huawei.hms.mlsdk.tts.MLTtsConfig;
import com.huawei.hms.mlsdk.tts.MLTtsConstants;
import com.huawei.hms.mlsdk.tts.MLTtsEngine;
import com.huawei.hms.mlsdk.tts.MLTtsError;
import com.huawei.hms.mlsdk.tts.MLTtsWarn;

import timber.log.Timber;

public class TTSWrapper { //TODO need to find out why this has to be inside a java class

    private MLTtsEngine mlCNTtsEngine;
    private MLTtsEngine mlENTtsEngine;
    private TTSListener ttsListener;

    private final Handler handler = new Handler(Looper.getMainLooper());

    public TTSWrapper() {
        createCNTtsEngine();
        createENTtsEngine();
    }

    public void speakEnglish(final String text) {
        mlENTtsEngine.speak(text, MLTtsEngine.QUEUE_FLUSH);
    }

    public void speakChinese(final String text) {
        mlCNTtsEngine.speak(text, MLTtsEngine.QUEUE_FLUSH);
    }

    private void createCNTtsEngine() {
        MLTtsConfig mlConfigs = new MLTtsConfig()
                .setLanguage(MLTtsConstants.TTS_ZH_HANS)
                .setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_ZH)
                .setSpeed(1f)
                .setVolume(1f);
        MLTtsCallback callback = new MLTtsCallback() {
            @Override
            public void onError(String taskId, MLTtsError err) {
                if(ttsListener != null) {
                    handler.post(() -> ttsListener.onError(err));
                }
            }

            @Override
            public void onWarn(String taskId, MLTtsWarn warn) {
                Timber.w("TaskId: " + taskId + " Message " + warn.getWarnMsg());
            }

            @Override
            public void onRangeStart(String taskId, int start, int end) {
                Timber.d("TaskId: " + taskId + " Start: " + start + " End: " + end);
            }

            @Override
            public void onAudioAvailable(String s, MLTtsAudioFragment mlTtsAudioFragment, int i, Pair<Integer, Integer> pair, Bundle bundle) {
                Timber.d(s);
            }

            @Override
            public void onEvent(String taskId, int eventName, Bundle bundle) {
                switch (eventName) {
                    case MLTtsConstants.EVENT_PLAY_STOP:
                        if(ttsListener != null) {
                            handler.post(() -> ttsListener.onSpeechEnded());
                        }
                        break;
                }
            }
        };
        mlCNTtsEngine = new MLTtsEngine(mlConfigs);
        mlCNTtsEngine.setTtsCallback(callback);
    }

    private void createENTtsEngine() {
        MLTtsConfig mlConfigs = new MLTtsConfig()
                .setLanguage(MLTtsConstants.TTS_EN_US)
                .setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_EN)
                .setSpeed(1f)
                .setVolume(1f);
        MLTtsCallback callback = new MLTtsCallback() {
            @Override
            public void onError(String taskId, MLTtsError err) {
                if(ttsListener != null) {
                    handler.post(() -> ttsListener.onError(err));
                }
            }

            @Override
            public void onWarn(String taskId, MLTtsWarn warn) {
                Timber.w("TaskId: " + taskId + " Message " + warn.getWarnMsg());
            }

            @Override
            public void onRangeStart(String taskId, int start, int end) {
                Timber.d("TaskId: " + taskId + " Start: " + start + " End: " + end);
            }

            @Override
            public void onAudioAvailable(String s, MLTtsAudioFragment mlTtsAudioFragment, int i, Pair<Integer, Integer> pair, Bundle bundle) {
                Timber.d(s);
            }

            @Override
            public void onEvent(String taskId, int eventName, Bundle bundle) {
                switch (eventName) {
                    case MLTtsConstants.EVENT_PLAY_STOP:
                        if(ttsListener != null) {
                            handler.post(() -> ttsListener.onSpeechEnded());
                        }
                        break;
                }
            }
        };
        mlENTtsEngine = new MLTtsEngine(mlConfigs);
        mlENTtsEngine.setTtsCallback(callback);
    }

    public void stop() {
        mlENTtsEngine.stop();
        mlCNTtsEngine.stop();
    }

    public void shutdown() {
        mlENTtsEngine.shutdown();
        mlCNTtsEngine.shutdown();
    }

    public void setTtsListener(TTSListener ttsListener) {
        this.ttsListener = ttsListener;
    }

    interface TTSListener {
        void onSpeechEnded();
        void onError(MLTtsError err);
    }
}
