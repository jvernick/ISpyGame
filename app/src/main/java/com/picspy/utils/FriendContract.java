package com.picspy.utils;

import android.provider.BaseColumns;

/**
 * Created by BrunelAmC on 8/5/2015.
 * This class stores public constants for accessing the sqlite database. Each inner class defines
 * constants for a database table
 */
public class FriendContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public FriendContract() {}

    /* Inner class that defines the friends table contents */
    public static abstract class FriendEntry implements BaseColumns {
        public static final String TABLE_NAME = "friends";
        public static final String COLUMN_NAME_USERNAME = "username";
        public static String COlUMN_NAME_NULLABLE = null;
    }
}
