package ca.joshstagg.slate.config

import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
abstract class ConfigViewHolder<in T : ConfigItem<*>>(itemView: View)
    : RecyclerView.ViewHolder(itemView) {

    abstract fun bind(item : T)

    abstract fun recycle()
}