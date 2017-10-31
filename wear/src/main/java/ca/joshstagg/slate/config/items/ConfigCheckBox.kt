package ca.joshstagg.slate.config.items

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class ConfigCheckBox(key: String, title: String, default: Boolean,
                     val onText: String, val offText: String,
                     val onIcon: Int, val offIcon: Int)
    : ConfigItem<Boolean>(key, title, default)