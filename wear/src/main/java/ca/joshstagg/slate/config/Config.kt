package ca.joshstagg.slate.config

import android.graphics.Color
import ca.joshstagg.slate.Constants


data internal class Config(val accentColor: Int = Color.parseColor(Constants.ACCENT_COLOR_STRING_DEFAULT),
                           val smoothMovement: Boolean = false,
                           val notificationDot: Boolean = true) {
    val updateRate: Long = if (smoothMovement) {
        Constants.INTERACTIVE_SMOOTH_UPDATE_RATE_MS
    } else {
        Constants.INTERACTIVE_UPDATE_RATE_MS
    }
}
