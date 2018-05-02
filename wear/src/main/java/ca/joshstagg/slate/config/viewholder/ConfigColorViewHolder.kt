package ca.joshstagg.slate.config.viewholder

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import ca.joshstagg.slate.R
import ca.joshstagg.slate.config.colorlist.SlateConfigColorListActivity
import ca.joshstagg.slate.config.items.ConfigColor

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class ConfigColorViewHolder(itemView: View, private val sharedPreferences: SharedPreferences) :
    ConfigViewHolder<ConfigColor>(itemView), SharedPreferences.OnSharedPreferenceChangeListener {

    private val colorView: ImageView = itemView.findViewById(R.id.config_color)
    private val title: TextView = itemView.findViewById(R.id.config_title)
    private val summary: TextView = itemView.findViewById(R.id.config_summary)

    private lateinit var item: ConfigColor

    override fun bind(item: ConfigColor) {
        this.item = item
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        itemView.setOnClickListener {
            val context = it.context
            val intent = Intent(context, SlateConfigColorListActivity::class.java)
            intent.putExtra("ITEM", item)
            context.startActivity(intent)
        }
        setColor()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == item.key) {
            setColor()
        }
    }

    private fun setColor() {
        val color = sharedPreferences.getString(item.key, item.default)
        var colorIndex = item.colorValues.indexOf(color)
        if (colorIndex < 0) {
            colorIndex = 0
        }
        colorView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
        title.text = item.title
        summary.text = item.colorNames[colorIndex]
    }


    override fun recycle() {
        itemView.setOnClickListener(null)
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}