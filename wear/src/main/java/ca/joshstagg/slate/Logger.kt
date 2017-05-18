package ca.joshstagg.slate

import android.util.Log

/**
 * Weather ca.joshstagg.weather.library
 * Copyright 2017 Josh Stagg
 */
internal object Logger {
    private val TAG: String = "Logger"

    private val PACKAGE = "ca.joshstagg.slate"
    private var set: Boolean = false
    private var isDebug: Boolean = false

    private fun isDebug(): Boolean {
        if (!set) {
            val o = getBuildConfigValue("DEBUG")
            if (o == null) {
                isDebug = false
            } else {
                isDebug = (o as Boolean)
            }
            set = true
        }
        return isDebug
    }

    /**
     * Gets a field from the project's BuildConfig. This is useful when, for example, flavors
     * are used at the project level to set custom fields.

     * @param fieldName The name of the field-to-access
     * *
     * *
     * @return The value of the field, or `null` if the field is not found.
     */
    private fun getBuildConfigValue(fieldName: String): Any? {
        try {
            val clazz = Class.forName(PACKAGE + ".BuildConfig")
            val field = clazz.getField(fieldName)
            return field.get(null)
        } catch (exception: ClassNotFoundException) {
            Log.d(TAG, exception.message)
        } catch (exception: NoSuchFieldException) {
            Log.d(TAG, exception.message)
        } catch (exception: IllegalAccessException) {
            Log.d(TAG, exception.message)
        }
        return null
    }

    fun v(tag: String, message: String) {
        if (isDebug()) {
            Log.v(tag, message)
        }
    }

    fun v(tag: String, message: String, e: Throwable) {
        if (isDebug()) {
            Log.v(tag, message, e)
        }
    }

    fun d(tag: String, message: String) {
        if (isDebug()) {
            Log.d(tag, message)
        }
    }

    fun d(tag: String, message: String, e: Throwable) {
        if (isDebug()) {
            Log.d(tag, message, e)
        }
    }

    fun i(tag: String, message: String) {
        if (isDebug()) {
            Log.i(tag, message)
        }
    }

    fun i(tag: String, message: String, e: Throwable) {
        if (isDebug()) {
            Log.i(tag, message, e)
        }

    }

    fun w(tag: String, message: String) {
        if (isDebug()) {
            Log.w(tag, message)
        }
    }

    fun w(tag: String, message: String, e: Throwable) {
        if (isDebug()) {
            Log.w(tag, message, e)
        }
    }

    fun e(tag: String, message: String) {
        if (isDebug()) {
            Log.e(tag, message)
        }
    }

    fun e(tag: String, message: String, e: Throwable) {
        if (isDebug()) {
            Log.e(tag, message, e)
        }
    }
}
