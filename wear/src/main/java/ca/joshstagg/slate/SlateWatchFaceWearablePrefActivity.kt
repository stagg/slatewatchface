package ca.joshstagg.slate

import android.os.Bundle

import preference.WearPreferenceActivity

/**
 * Slate ca.joshstagg.slate
 * Copyright 2017  Josh Stagg
 */
class SlateWatchFaceWearablePrefActivity : WearPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        Slate.instance?.configService?.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        Slate.instance?.configService?.disconnect()
    }
}
