package ca.joshstagg.slate.config.colorlist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ca.joshstagg.slate.R
import ca.joshstagg.slate.config.items.ConfigColor

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class SlateConfigColorListAdapter(
    private val configColor: ConfigColor,
    private val listener: OnColorSelectedListener
) : RecyclerView.Adapter<ColorListViewHolder>() {

    override fun getItemCount(): Int = configColor.colorValues.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorListViewHolder =
        ColorListViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.config_color_list_row,
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ColorListViewHolder, position: Int) {
        holder.bind(configColor.colorValues[position], configColor.colorNames[position], listener)
    }

    override fun onViewRecycled(holder: ColorListViewHolder?) {
        super.onViewRecycled(holder)
        holder?.recycle()
    }
}