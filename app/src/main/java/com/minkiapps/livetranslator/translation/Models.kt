package com.minkiapps.livetranslator.translation

import com.huawei.hms.mlsdk.translate.local.MLLocalTranslatorModel
import com.huawei.hms.mlsdk.tts.MLTtsConstants
import com.huawei.hms.mlsdk.tts.MLTtsLocalModel

data class OfflineModel(val translatorModel : MLLocalTranslatorModel,
                        val ttsModel : MLTtsLocalModel)

val mlLangModels = arrayOf(
    OfflineModel(MLLocalTranslatorModel
        .Factory("en")
        .create(),
        MLTtsLocalModel.Factory(MLTtsConstants.TTS_SPEAKER_OFFLINE_EN_US_FEMALE_EAGLE).create()
    ), //English
    OfflineModel(MLLocalTranslatorModel
        .Factory("zh")
        .create(),
        MLTtsLocalModel.Factory(MLTtsConstants.TTS_SPEAKER_OFFLINE_ZH_HANS_FEMALE_EAGLE).create()
    ), //Chinese
    OfflineModel(MLLocalTranslatorModel
        .Factory("de")
        .create(),
        MLTtsLocalModel.Factory(MLTtsConstants.TTS_SPEAKER_OFFLINE_DE_DE_FEMALE_BEE).create()
    ) //German
)

val translationList : List<Translation> = buildTranslationList()

private fun buildTranslationList() : List<Translation> {
    val translations : MutableList<Translation> = mutableListOf()

    for (item1 in mlLangModels) {
        for (item2 in mlLangModels) {
            if (item1 != item2) {
                translations.add(Translation(
                    item1.translatorModel.languageCode,
                    item2.translatorModel.languageCode)
                )
            }
        }
    }

    return translations
}