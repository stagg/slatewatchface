package ca.joshstagg.slate.config

import android.content.res.ColorStateList
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import ca.joshstagg.slate.R

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class ColorListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val colorView: ImageView = itemView.findViewById(R.id.config_color)
    private val title: TextView = itemView.findViewById(R.id.config_title)

    fun bind(value: String, name: String, listener: OnColorSelectedListener) {
        title.text = name
        colorView.backgroundTintList = ColorStateList.valueOf(Color.parseColor(value))

        itemView.setOnClickListener { listener.onColorSelected(value) }
    }

    fun recycle() {
        itemView.setOnClickListener(null)
    }
}