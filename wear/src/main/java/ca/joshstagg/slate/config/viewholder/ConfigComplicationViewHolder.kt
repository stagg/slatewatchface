package ca.joshstagg.slate.config.viewholder

import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.view.View
import ca.joshstagg.slate.*
import ca.joshstagg.slate.config.WatchFacePreviewView
import ca.joshstagg.slate.config.items.ConfigComplication


/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class ConfigComplicationViewHolder(itemView: View, private val sharedPrefs: SharedPreferences) :
    ConfigViewHolder<ConfigComplication>(itemView),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val preview: WatchFacePreviewView = itemView.findViewById(R.id.config_watch_preview)
    private val handler: Handler = Handler(Looper.myLooper()) {
        previewInvalidate()
        true
    }

    override fun bind(item: ConfigComplication) {
        sharedPrefs.registerOnSharedPreferenceChangeListener(this)
        preview.postInvalidate()
        handler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 300)
    }

    override fun recycle() {
        handler.removeMessages(MSG_UPDATE_TIME)
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key in listOf(KEY_NOTIFICATION_DOT, KEY_SECONDS_COLOR, KEY_BACKGROUND)) {
            previewInvalidate()
        }
    }

    private fun previewInvalidate() {
        preview.postInvalidate()
        handler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, 1000)
    }
}