package com.minkiapps.livetranslator.koin

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.minkiapps.livetranslator.prefs.AppPres
import com.minkiapps.livetranslator.tooltip.AppTooltip
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {

    single {
        PreferenceManager.getDefaultSharedPreferences(androidContext())
    }

    single<AppPres> { AppPres(get<SharedPreferences>()) }

    single<AppTooltip> { AppTooltip(get<SharedPreferences>()) }
}