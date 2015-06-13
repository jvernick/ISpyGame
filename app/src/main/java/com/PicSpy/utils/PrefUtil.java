package com.picspy.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by BrunelAmC on 6/9/2015.
 */
public class PrefUtil {
    static public final class Prefs {
        public static SharedPreferences get(Context context) {
            return context.getSharedPreferences("_picspy_pref", 0);
        }
    }

    public static String getString(Context context, String key) {
        SharedPreferences settings = Prefs.get(context);
        return settings.getString(key, "");
    }

    public static String getString(Context context, String key, String defaultString) {
        SharedPreferences settings = Prefs.get(context);
        return settings.getString(key, defaultString);
    }

    static public synchronized void putString(Context context, String key, String value) {
        SharedPreferences settings = Prefs.get(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
