package ca.joshstagg.slate.config.items

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class ConfigComplication(
    key: String,
    title: String,
    default: IntArray
) : ConfigItem<IntArray>(key, title, default)