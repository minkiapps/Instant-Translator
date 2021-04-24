package com.minkiapps.livetranslator.migration

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.minkiapps.livetranslator.tooltip.ScanRectTooltip
import com.minkiapps.livetranslator.tooltip.ScanRectTooltip.Companion.PREF_MAX_SHOW_NO
import timber.log.Timber

object MigrationManager {

    private const val CURRENT_VERSION = 1
    private const val VERSION_NONE = -1
    private const val PREF_LAST_MIGRATION_MANAGER_VERSION = "PREF_LAST_MIGRATION_MANAGER_VERSION"

    fun migrate(context: Context) {
        val defaultPrefs = PreferenceManager.getDefaultSharedPreferences(context)

        if (isNewInstalled(context)) {
            saveMigratedVersion(defaultPrefs)
            return
        }

        val lastVersion = getLastVersion(defaultPrefs)
        Timber.d("Migration from last version: $lastVersion")
        if (CURRENT_VERSION >= 1 && lastVersion == VERSION_NONE) {
            defaultPrefs.edit(commit = true) { //decrease max shown by 1 so it will trigger a new tooltip
                putInt(PREF_MAX_SHOW_NO, ScanRectTooltip.MAX_SHOW_DRAGGING_NO - 1)
            }
        }

        saveMigratedVersion(defaultPrefs)
    }

    private fun isNewInstalled(context: Context): Boolean {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return info.firstInstallTime == info.lastUpdateTime
    }

    private fun saveMigratedVersion(defaultPrefs: SharedPreferences) {
        defaultPrefs.edit {
            putInt(PREF_LAST_MIGRATION_MANAGER_VERSION, CURRENT_VERSION)
        }
    }

    private fun getLastVersion(defaultPrefs: SharedPreferences): Int {
        return defaultPrefs.getInt(PREF_LAST_MIGRATION_MANAGER_VERSION, VERSION_NONE)
    }
}