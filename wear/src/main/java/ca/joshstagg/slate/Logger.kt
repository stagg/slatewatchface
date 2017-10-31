package ca.joshstagg.slate

import timber.log.Timber

/**
 * Weather ca.joshstagg.weather.library
 * Copyright 2017 Josh Stagg
 */
internal object Logger {

    fun d(tag: String, message: String) {
        Timber.tag(tag)
        Timber.d(message)
    }

}
