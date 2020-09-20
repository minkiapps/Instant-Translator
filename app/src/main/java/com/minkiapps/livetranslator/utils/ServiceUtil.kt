package com.minkiapps.livetranslator.utils

import android.content.Context
import android.os.Build
import com.huawei.hms.api.ConnectionResult
import com.huawei.hms.api.HuaweiApiAvailability

fun Context.isHmsAvailable() : Boolean {
    return HuaweiApiAvailability.getInstance().isHuaweiMobileServicesAvailable(this) == ConnectionResult.SUCCESS
}

fun isHUAWEIManufacturer() : Boolean {
    return Build.MANUFACTURER.contains("HUAWEI", ignoreCase = true)
}