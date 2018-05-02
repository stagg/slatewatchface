package ca.joshstagg.slate.config.items

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class ConfigSwitch(
    key: String, title: String, default: Boolean,
    val onText: String, val offText: String
) : ConfigItem<Boolean>(key, title, default)