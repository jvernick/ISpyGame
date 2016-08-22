package com.picspy.utils;

import android.content.Context;
import android.util.Log;

import com.picspy.adapters.DatabaseHandler;

/**
 * Api for clearing app data (sqlite)
 */
public class Accounts {
    public static void checkNewAccount(Context context, int id) {
        int currentId = PrefUtil.getInt(context, AppConstants.USER_ID);
        //-1 means empty == first user
        if (currentId != -1 && currentId != id) {
            context.getSharedPreferences(PrefUtil.PREF_NAME, 0).edit().clear().commit();
            DatabaseHandler.getInstance(context).clearDatabase();
            Log.d("Accounts", "new account, clearing all data");
        }
    }
}
