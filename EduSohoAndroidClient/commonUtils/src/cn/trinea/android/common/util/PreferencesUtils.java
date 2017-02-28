package cn.trinea.android.common.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtils {

    public static boolean putString(String prefName, Context context, String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        return editor.commit();
    }

    public static String getString(String prefName, Context context, String key) {
        return getString(prefName, context, key, null);
    }

    public static String getString(String prefName, Context context, String key, String defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return settings.getString(key, defaultValue);
    }

    public static boolean putInt(String prefName, Context context, String key, int value) {
        SharedPreferences settings = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    public static int getInt(String prefName, Context context, String key) {
        return getInt(prefName, context, key, -1);
    }

    public static int getInt(String prefName, Context context, String key, int defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return settings.getInt(key, defaultValue);
    }

    public static boolean putLong(String prefName, Context context, String key, long value) {
        SharedPreferences settings = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);
        return editor.commit();
    }

    public static long getLong(String prefName, Context context, String key) {
        return getLong(prefName, context, key, -1);
    }

    public static long getLong(String prefName, Context context, String key, long defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return settings.getLong(key, defaultValue);
    }

    public static boolean putFloat(String prefName, Context context, String key, float value) {
        SharedPreferences settings = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        return editor.commit();
    }

    public static float getFloat(String prefName, Context context, String key) {
        return getFloat(prefName, context, key, -1);
    }

    public static float getFloat(String prefName, Context context, String key, float defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return settings.getFloat(key, defaultValue);
    }

    public static boolean putBoolean(String prefName, Context context, String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public static boolean getBoolean(String prefName, Context context, String key) {
        return getBoolean(prefName, context, key, false);
    }

    public static boolean getBoolean(String prefName, Context context, String key, boolean defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
        return settings.getBoolean(key, defaultValue);
    }
}
