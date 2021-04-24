package com.minkiapps.livetranslator.tooltip

import android.view.View
import it.sephiroth.android.library.xtooltip.Tooltip
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun Tooltip.showAndOnHiddenAwait(parent: View,
                                         gravity: Tooltip.Gravity) = suspendCoroutine<Unit>{ cont ->
    show(parent, gravity, false)
    doOnHidden {
        cont.resume(Unit)
    }
}