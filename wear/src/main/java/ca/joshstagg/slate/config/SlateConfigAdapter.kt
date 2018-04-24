package ca.joshstagg.slate.config

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import ca.joshstagg.slate.R
import ca.joshstagg.slate.Slate
import ca.joshstagg.slate.config.items.*
import ca.joshstagg.slate.config.viewholder.*

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class SlateConfigAdapter(private val data: List<ConfigItem<*>>) : RecyclerView.Adapter<ConfigViewHolder<*>>() {

    private val sharedPrefs = Slate.instance.configService.preferences

    override fun getItemCount(): Int = data.count()

    override fun getItemViewType(position: Int): Int {
        val item = data[position]
        return when (item) {
            is ConfigSwitch -> 1
            is ConfigCheckBox -> 2
            is ConfigColor -> 3
            is ConfigComplication -> 4
            else -> 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigViewHolder<*>? {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            1 -> ConfigSwitchViewHolder(inflater.inflate(R.layout.config_switch_row, parent, false), sharedPrefs)
            2 -> ConfigCheckBoxViewHolder(inflater.inflate(R.layout.config_checkbox_row, parent, false), sharedPrefs)
            3 -> ConfigColorViewHolder(inflater.inflate(R.layout.config_color_row, parent, false), sharedPrefs)
            4 -> ConfigComplicationViewHolder(inflater.inflate(R.layout.config_complication_row, parent, false), sharedPrefs)
            else -> null
        }
    }

    override fun onBindViewHolder(holder: ConfigViewHolder<*>, position: Int) {
        when(holder) {
            is ConfigSwitchViewHolder -> holder.bind(data[position] as ConfigSwitch)
            is ConfigCheckBoxViewHolder -> holder.bind(data[position] as ConfigCheckBox)
            is ConfigColorViewHolder -> holder.bind(data[position] as ConfigColor)
            is ConfigComplicationViewHolder -> holder.bind(data[position] as ConfigComplication)
        }
    }

    override fun onViewRecycled(holder: ConfigViewHolder<*>?) {
        super.onViewRecycled(holder)
        holder?.recycle()
    }
}