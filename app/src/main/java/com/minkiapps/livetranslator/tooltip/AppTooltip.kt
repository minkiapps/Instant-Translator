package com.minkiapps.livetranslator.tooltip

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.minkiapps.livetranslator.R
import it.sephiroth.android.library.xtooltip.ClosePolicy
import it.sephiroth.android.library.xtooltip.Tooltip
import kotlinx.android.synthetic.main.activity_main.*

class AppTooltip(private val prefs : SharedPreferences) {

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

    fun buildDefaultToolTip(context: Context,
                            tooltipText : String,
                            anchorView : View,
                            xOff : Int = 0,
                            yOff : Int = 0) : Tooltip{
        return Tooltip.Builder(context)
            .anchor(anchorView, xOff, yOff, true)
            .text(tooltipText)
            .showDuration(2500L)
            .closePolicy(ClosePolicy.TOUCH_NONE)
            .floatingAnimation(Tooltip.Animation.DEFAULT)
            .create()
    }

    companion object {
        const val MAX_SHOW_DRAGGING_NO = 2
        private const val SHOW_INTERVAL = 48 * 3600

        const val PREF_LAST_SHOWN_EPOCH = "PREF_LAST_SHOWN_EPOCH"
        const val PREF_MAX_SHOW_NO = "PREF_MAX_SHOW_NO"
    }
}