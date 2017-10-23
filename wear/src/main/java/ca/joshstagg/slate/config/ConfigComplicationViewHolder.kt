package ca.joshstagg.slate.config

import android.content.SharedPreferences
import android.view.View
import ca.joshstagg.slate.Constants
import ca.joshstagg.slate.R

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class ConfigComplicationViewHolder(itemView: View, private val sharedPrefs: SharedPreferences)
    : ConfigViewHolder<ConfigComplication>(itemView), SharedPreferences.OnSharedPreferenceChangeListener {

    private val preview: WatchFacePreviewView = itemView.findViewById(R.id.config_watch_preview)

    override fun bind(item: ConfigComplication) {
        sharedPrefs.registerOnSharedPreferenceChangeListener(this)
        preview.invalidate()
    }

    override fun recycle() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key in listOf(Constants.KEY_NOTIFICATION_DOT, Constants.KEY_SECONDS_COLOR)) {
            preview.invalidate()
        }
    }

}