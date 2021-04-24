package com.minkiapps.livetranslator.prefs

import android.content.SharedPreferences
import androidx.core.content.edit
import com.minkiapps.livetranslator.translation.Translation

private const val PREFS_LAST_TRANSLATION = "PREFS_LAST_TRANSLATION"
private const val PREFS_ALL_MODELS_DOWNLOADED = "PREFS_ALL_MODELS_DOWNLOADED"

class AppPres(private val sharedPreferences: SharedPreferences) {

    fun saveTranslation(translation: Translation) {
        sharedPreferences.edit {
            putString(PREFS_LAST_TRANSLATION, "${translation.fromLang}_${translation.toLang}")
        }
    }

    fun getLastTranslation() : Translation {
        return sharedPreferences.getString(PREFS_LAST_TRANSLATION, null).toTranslation()
    }

    fun isAllModelsDownloaded() = sharedPreferences.getBoolean(PREFS_ALL_MODELS_DOWNLOADED, false)

    fun setAllModelsDownloaded() = sharedPreferences.edit {
        putBoolean(PREFS_ALL_MODELS_DOWNLOADED, true)
    }

    private fun String?.toTranslation() : Translation {
        return when(this) {
            "en_zh" -> Translation("en", "zh")
            "zh_en" -> Translation("zh", "en")
            "en_de" -> Translation("en", "de")
            "de_en" -> Translation("de", "en")
            "de_zh" -> Translation("de", "zh")
            "zh_de" -> Translation("zh", "de")
            else -> Translation("en", "zh")
        }
    }
}
