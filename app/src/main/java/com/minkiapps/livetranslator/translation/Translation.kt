package com.minkiapps.livetranslator.translation

import android.content.Context
import com.minkiapps.livetranslator.R

data class Translation(val fromLang : String, val toLang : String)

fun Translation.toUIString(context: Context) : String {
    return "${context.languageCodeUIText(fromLang)} \u2964 ${context.languageCodeUIText(toLang)}"
}

private fun Context.languageCodeUIText(lngCode : String) : String{
    return when (lngCode) {
        "en" -> getString(R.string.language_english)
        "zh" -> getString(R.string.language_chinese)
        "de" -> getString(R.string.language_german)
        else -> ""
    }
}