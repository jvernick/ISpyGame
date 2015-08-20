package com.picspy.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Class for working with Shared Preferences. Provides methods to access, create and update
 * Shared Preferences
 * Created by BrunelAmC on 6/9/2015.
 */
public class PrefUtil {
    static public final class Prefs {
        public static SharedPreferences get(Context context) {
            return context.getSharedPreferences("_picspy_pref", 0);
        }
    }

    /**
     * Gets a String from the shared preferences
     * @param context The context that invokes this call
     * @param key The key for accessing the required value
     * @return  The value for the desired key
     */
    public static String getString(Context context, String key) {
        SharedPreferences settings = Prefs.get(context);
        return settings.getString(key, "");
    }

    /**
     * Gets a String from the shared preferences
     * @param context The context that invokes this call
     * @param key The key for accessing the required value
     * @param defaultString Value returned when the key is not found
     * @return  The value for the desired key
     */
    public static String getString(Context context, String key, String defaultString) {
        SharedPreferences settings = Prefs.get(context);
        return settings.getString(key, defaultString);
    }

    /**
     * Adds a String to the shared preferences
     * @param context The context that invokes this call
     * @param key The key for accessing the required value
     * @param value The value to be added to the shared preferences
     */
    static public synchronized void putString(Context context, String key, String value) {
        SharedPreferences settings = Prefs.get(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Adds an Int to the shared preferences
     * @param context The context that invokes this call
     * @param key The key for accessing the required value
     * @param value The integer value to be added to the shared preferences
     */
    static public synchronized void putInt(Context context, String key, int value) {
        SharedPreferences settings = Prefs.get(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * Gets an int from the shared preferences
     * @param context The context that invokes this call
     * @param key The key for accessing the required value
     * @return  The integer value for the desired key
     */
    public static int getInt (Context context, String key) {
        SharedPreferences settings = Prefs.get(context);
        return settings.getInt(key, 0);
    }

    /**
     * Gets a String from the shared preferences
     * @param context The context that invokes this call
     * @param key The key for accessing the required value
     * @param defautlInt Value returned when the key is not found
     * @return  The integer value for the desired key
     */
    public static int getInt (Context context, String key, int defautlInt) {
        SharedPreferences settings = Prefs.get(context);
        return settings.getInt(key, defautlInt);
    }

    /**
     * Removes a key-value pair from shared preferences
     * @param context the context that invokes this call
     * @param key the key for the key-value pair to be removed
     */
    static public synchronized void removeValue(Context context, String key) {
        SharedPreferences settings = Prefs.get(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        editor.apply();
    }
}
