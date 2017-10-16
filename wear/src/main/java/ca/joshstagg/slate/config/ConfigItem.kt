package ca.joshstagg.slate.config

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
abstract class ConfigItem<out T>(val key: String, val title: String, val default: T)