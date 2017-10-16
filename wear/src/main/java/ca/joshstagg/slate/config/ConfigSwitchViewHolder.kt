package ca.joshstagg.slate.config

import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import ca.joshstagg.slate.R

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class ConfigSwitchViewHolder(itemView: View, private val sharedPreferences: SharedPreferences)
    : ConfigViewHolder<ConfigSwitch>(itemView), CompoundButton.OnCheckedChangeListener {

    private val switch: Switch = itemView.findViewById(R.id.config_switch)
    private val title: TextView = itemView.findViewById(R.id.config_title)
    private val summary: TextView = itemView.findViewById(R.id.config_summary)

    private lateinit var item: ConfigSwitch
    private var switchThumb: Drawable? = null


    override fun bind(item: ConfigSwitch) {
        this.item = item
        switchThumb = switch.thumbDrawable
        switch.isChecked = sharedPreferences.getBoolean(item.key, item.default)
        switch.setOnCheckedChangeListener(this)
        title.text = item.title
        setSummary()
        itemView.setOnClickListener { switch.isChecked = !switch.isChecked }
    }

    private fun setSummary() {
        summary.text = if (switch.isChecked) {
            item.onText
        } else {
            item.offText
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        sharedPreferences.edit().putBoolean(item.key, isChecked).apply()
        setSummary()
    }

    override fun recycle() {
        switch.setOnClickListener(null)
        switch.thumbDrawable = switchThumb
    }
}