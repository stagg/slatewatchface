package ca.joshstagg.slate.config

import ca.joshstagg.slate.Constants


internal class Config {
    // Config
    var accentColor = Constants.ACCENT_COLOR_DEFAULT
    var smoothMovement = Constants.SMOOTH_MOVEMENT_DEFAULT
    val updateRate: Long
        get() {
            return if (smoothMovement) {
                Constants.INTERACTIVE_SMOOTH_UPDATE_RATE_MS
            } else {
                Constants.INTERACTIVE_UPDATE_RATE_MS
            }
        }
}
