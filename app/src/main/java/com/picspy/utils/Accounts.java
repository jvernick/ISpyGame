package com.picspy.utils;

import android.content.Context;
import android.util.Log;

/**
 * Created by BrunelAmC on 1/16/2016.
 */
public class Accounts {

    public static void checkNewAccount(Context context, int id) {
        int currentId = PrefUtil.getInt(context, AppConstants.USER_ID);
        //-1 means empty == first user
        if (currentId != -1 && currentId != id) {
            /*TODO
            * -clear database
            * -clear anything that needs clearing
             */
            Log.d("Accounts", "new account, clearing all data");
        }

    }
}
