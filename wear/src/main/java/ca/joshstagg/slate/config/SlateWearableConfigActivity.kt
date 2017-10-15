package ca.joshstagg.slate.config

import android.os.Bundle
import ca.joshstagg.slate.R
import ca.joshstagg.slate.Slate

import preference.WearPreferenceActivity

/**
 * Slate ca.joshstagg.slate
 * Copyright 2017  Josh Stagg
 */
class SlateWearableConfigActivity : WearPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        Slate.instance.configService.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        Slate.instance.configService.disconnect()
    }
}
