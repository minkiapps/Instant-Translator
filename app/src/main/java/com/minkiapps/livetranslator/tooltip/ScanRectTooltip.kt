package com.minkiapps.livetranslator.tooltip

import android.content.Context
import androidx.core.content.edit
import androidx.preference.PreferenceManager

class ScanRectTooltip(context : Context) {

    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun shouldShowTooltip() : Boolean {
        val maxShown = prefs.getInt(PREF_MAX_SHOW_NO, 0)
        if(maxShown >= MAX_SHOW_DRAGGING_NO) {
            return false
        }

        val lastShownEpoch = prefs.getLong(PREF_LAST_SHOWN_EPOCH, 0L)
        if(System.currentTimeMillis() / 1000 - lastShownEpoch < SHOW_INTERVAL) {
            return false
        }

        prefs.edit {
            putInt(PREF_MAX_SHOW_NO, maxShown + 1)
            putLong(PREF_LAST_SHOWN_EPOCH, System.currentTimeMillis() / 1000)
        }

        return true
    }

    companion object {
        private const val MAX_SHOW_DRAGGING_NO = 3
        private const val SHOW_INTERVAL = 24 * 3600

        private const val PREF_LAST_SHOWN_EPOCH = "PREF_LAST_SHOWN_EPOCH"
        private const val PREF_MAX_SHOW_NO = "PREF_MAX_SHOW_NO"
    }
}