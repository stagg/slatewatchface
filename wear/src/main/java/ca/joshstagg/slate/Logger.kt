package ca.joshstagg.slate

import timber.log.Timber

/**
 * Weather ca.joshstagg.weather.library
 * Copyright 2017 Josh Stagg
 */
internal object Logger {

    fun v(tag: String, message: String) {
        Timber.v(tag, message)
    }

    fun v(tag: String, message: String, e: Throwable) {
        Timber.v(tag, message, e)
    }

    fun d(tag: String, message: String) {
        Timber.d(tag, message)
    }

    fun d(tag: String, message: String, e: Throwable) {
        Timber.d(tag, message, e)
    }

    fun i(tag: String, message: String) {
        Timber.i(tag, message)
    }

    fun i(tag: String, message: String, e: Throwable) {
        Timber.i(tag, message, e)
    }

    fun w(tag: String, message: String) {
        Timber.w(tag, message)
    }

    fun w(tag: String, message: String, e: Throwable) {
        Timber.w(tag, message, e)
    }

    fun e(tag: String, message: String) {
        Timber.e(tag, message)
    }

    fun e(tag: String, message: String, e: Throwable) {
        Timber.e(tag, message, e)
    }
}
