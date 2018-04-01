package ca.joshstagg.slate.config.viewholder

import android.content.SharedPreferences
import android.view.View
import ca.joshstagg.slate.KEY_NOTIFICATION_DOT
import ca.joshstagg.slate.KEY_SECONDS_COLOR
import ca.joshstagg.slate.R
import ca.joshstagg.slate.config.WatchFacePreviewView
import ca.joshstagg.slate.config.items.ConfigComplication
import java.util.concurrent.TimeUnit

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class ConfigComplicationViewHolder(itemView: View, private val sharedPrefs: SharedPreferences) :
    ConfigViewHolder<ConfigComplication>(itemView),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val preview: WatchFacePreviewView = itemView.findViewById(R.id.config_watch_preview)

    override fun bind(item: ConfigComplication) {
        sharedPrefs.registerOnSharedPreferenceChangeListener(this)
        previewInvalidate()
    }

    override fun recycle() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key in listOf(KEY_NOTIFICATION_DOT, KEY_SECONDS_COLOR)) {
            previewInvalidate()
        }
    }

    private fun previewInvalidate() {
        preview.postInvalidate()
        preview.postDelayed({ preview.postInvalidate() }, TimeUnit.SECONDS.toMillis(1))
    }
}