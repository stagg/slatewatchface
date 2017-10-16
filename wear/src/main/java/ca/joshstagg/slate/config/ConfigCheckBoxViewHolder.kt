package ca.joshstagg.slate.config

import android.content.SharedPreferences
import android.view.View
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import ca.joshstagg.slate.R

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class ConfigCheckBoxViewHolder(itemView: View, private val sharedPreferences: SharedPreferences)
    : ConfigViewHolder<ConfigCheckBox>(itemView), CompoundButton.OnCheckedChangeListener {

    private val checkBox: CheckBox = itemView.findViewById(R.id.config_checkbox)
    private val title: TextView = itemView.findViewById(R.id.config_title)
    private val summary: TextView = itemView.findViewById(R.id.config_summary)

    private lateinit var item: ConfigCheckBox


    override fun bind(item: ConfigCheckBox) {
        this.item = item
        checkBox.isChecked = sharedPreferences.getBoolean(item.key, item.default)
        checkBox.setOnCheckedChangeListener(this)
        title.text = item.title
        setIcon()
        setSummary()
        itemView.setOnClickListener { checkBox.isChecked = !checkBox.isChecked }
    }

    private fun setIcon() {
        if (checkBox.isChecked) {
            item.onIcon
        } else {
            item.offIcon
        }.let {
            checkBox.setButtonDrawable(it)
        }
    }

    private fun setSummary() {
        summary.text = if (checkBox.isChecked) {
            item.onText
        } else {
            item.offText
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        sharedPreferences.edit().putBoolean(item.key, isChecked).apply()
        setIcon()
        setSummary()
    }

    override fun recycle() {
        checkBox.setOnClickListener(null)
    }
}