package ca.joshstagg.slate.config

import android.graphics.Color
import ca.joshstagg.slate.ACCENT_COLOR_STRING_DEFAULT
import ca.joshstagg.slate.AMBIENT_COLOR_STRING_DEFAULT
import ca.joshstagg.slate.INTERACTIVE_SMOOTH_UPDATE_RATE_MS
import ca.joshstagg.slate.INTERACTIVE_UPDATE_RATE_MS


internal data class Config(
    val accentColor: Int = Color.parseColor(ACCENT_COLOR_STRING_DEFAULT),
    val ambientColor: Int = Color.parseColor(AMBIENT_COLOR_STRING_DEFAULT),
    val smoothMovement: Boolean = false,
    val notificationDot: Boolean = true,
    val background: Boolean = true
) {
    val updateRate: Long = if (smoothMovement) {
        INTERACTIVE_SMOOTH_UPDATE_RATE_MS
    } else {
        INTERACTIVE_UPDATE_RATE_MS
    }
}
