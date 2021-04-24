package com.minkiapps.livetranslator.translation

import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.text.MLLocalTextSetting
import com.huawei.hms.mlsdk.text.MLTextAnalyzer
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslateSetting
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslator
import kotlin.collections.HashMap

object TranslationFactory {

    private val recognisers : HashMap<Translation, MLTextAnalyzer> = HashMap()
    private val translators : HashMap<Translation, MLLocalTranslator> = HashMap()

    @Synchronized
    fun getRecogniser(translation : Translation) : MLTextAnalyzer{
        return recognisers.getOrPut(translation) {
            val setting = MLLocalTextSetting.Factory()
                    .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
                    .setLanguage(translation.fromLang)
                    .create()
            MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting)
        }
    }

    @Synchronized
    fun getTranslator(translation: Translation) : MLLocalTranslator {
        return translators.getOrPut(translation) {
            val factory =
                MLLocalTranslateSetting.Factory()
                    .setSourceLangCode(translation.fromLang)
                    .setTargetLangCode(translation.toLang)

            return MLTranslatorFactory.getInstance().getLocalTranslator(factory.create())
        }
    }

    @Synchronized
    fun close() {
        recognisers.forEach { it.value.release() }
        translators.forEach { it.value.stop() }
    }

}