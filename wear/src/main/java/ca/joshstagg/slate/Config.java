package ca.joshstagg.slate;

class Config {

    // Config
    private int mAccentColor = Constants.COLOR_DEFAULT;
    private long mUpdateRate = Constants.INTERACTIVE_UPDATE_RATE_MS;
    private boolean mSmoothMovement = Constants.SMOOTH_MOVEMENT_DEFAULT;

    int getAccentColor() {
        return mAccentColor;
    }

    void setAccentColor(int accentColor) {
        mAccentColor = accentColor;
    }

    long getUpdateRate() {
        return mUpdateRate;
    }

    boolean isSmoothMovement() {
        return mSmoothMovement;
    }

    void setSmoothMode(boolean smoothMode) {
        mUpdateRate = smoothMode ? Constants.INTERACTIVE_SMOOTH_UPDATE_RATE_MS : Constants.INTERACTIVE_UPDATE_RATE_MS;
        mSmoothMovement = smoothMode;
    }
}
