package ca.joshstagg.slate

internal class Config {

    // Config
    var accentColor = Constants.ACCENT_COLOR_DEFAULT
    var updateRate = Constants.INTERACTIVE_UPDATE_RATE_MS
        private set
    var isSmoothMovement = Constants.SMOOTH_MOVEMENT_DEFAULT
        private set

    fun setSmoothMode(smoothMode: Boolean) {
        updateRate = if (smoothMode) Constants.INTERACTIVE_SMOOTH_UPDATE_RATE_MS else Constants.INTERACTIVE_UPDATE_RATE_MS
        isSmoothMovement = smoothMode
    }
}
