package com.minkiapps.livetranslator.overlay

import android.graphics.RectF
import android.util.Size

interface ScannerOverlay {

    val size : Size

    val scanRect : RectF
}