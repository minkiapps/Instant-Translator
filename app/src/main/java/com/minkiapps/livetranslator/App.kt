package com.minkiapps.livetranslator

import android.app.Application
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.mlsdk.common.MLApplication
import timber.log.Timber

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        if(BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val config = AGConnectServicesConfig.fromContext(this)
        MLApplication.getInstance().apiKey = config.getString("client/api_key")
    }
}