package ca.joshstagg.slate;

import android.util.Log;

import java.lang.reflect.Field;

/**
 * Weather ca.joshstagg.weather.library
 * Copyright 2014  Josh Stagg
 */
public class Logger {

    public static final String PACKAGE = "ca.joshstagg.slate";

    public static boolean set;
    public static boolean isDebug;

    public static boolean isDebug(){
        if (!set) {
            Object o = getBuildConfigValue("DEBUG");
            if (o == null) {
                isDebug = false;
            } else {
                isDebug = (Boolean) o;
            }
            set = true;
        }
        return isDebug;
    }

    /**
     * Gets a field from the project's BuildConfig. This is useful when, for example, flavors
     * are used at the project level to set custom fields.
     * @param fieldName     The name of the field-to-access
     * @return              The value of the field, or {@code null} if the field is not found.
     */
    private static Object getBuildConfigValue(String fieldName) {
        try {
            Class<?> clazz = Class.forName(PACKAGE + ".BuildConfig");
            Field field = clazz.getField(fieldName);
            return field.get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignored) {
        }
        return null;
    }

    public static void v(String tag, String message) {
        if (isDebug()) Log.v(tag, message);
    }

    public static void v(String tag, String message, Throwable e) {
        if (isDebug()) {
            Log.v(tag, message, e);
        }
    }

    public static void d(String tag, String message) {
        if (isDebug()) Log.d(tag, message);
    }

    public static void d(String tag, String message, Throwable e) {
        if (isDebug()) {
            Log.d(tag, message, e);
        }
    }

    public static void i(String tag, String message) {
        if (isDebug()) {
            Log.i(tag, message);
        }
    }

    public static void i(String tag, String message, Throwable e) {
        if (isDebug()) {
            Log.i(tag, message, e);
        }

    }

    public static void w(String tag, String message) {
        if (isDebug()) Log.w(tag, message);
    }

    public static void w(String tag, String message, Throwable e) {
        if (isDebug()) {
            Log.w(tag, message, e);
        }
    }

    public static void e(String tag, String message) {if (isDebug()) Log.e(tag, message);}

    public static void e(String tag, String message, Throwable e) {
        if (isDebug()) {
            Log.e(tag, message, e);
        }
    }
}
