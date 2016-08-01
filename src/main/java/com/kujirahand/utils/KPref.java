package com.kujirahand.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.prefs.Preferences;

/**
 * Created by kujira on 2016/05/20.
 */
public class KPref {
    static SharedPreferences pref = null;
    public static void init(Context ctx) {
        pref = PreferenceManager.getDefaultSharedPreferences(ctx);
    }
    public static SharedPreferences getPref() {
        return pref;
    }
    public static int getInt(String key, int def) {
        return pref.getInt(key, def);
    }
    public static void setInt(String key, int value) {
        pref.edit().putInt(key, value).commit();
    }
    public static String getStr(String key, String def) {
        return pref.getString(key, def);
    }
    public static void setStr(String key, String value) {
        pref.edit().putString(key, value).commit();
    }
}
