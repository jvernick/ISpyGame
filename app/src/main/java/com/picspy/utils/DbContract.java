package com.picspy.utils;

import android.provider.BaseColumns;

/**
 * Created by BrunelAmC on 8/5/2015.
 * This class stores public constants for accessing the sqlite database. Each inner class defines
 * constants for a database table
 */
public class DbContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DbContract() {}

    /* Inner class that defines the friends table contents */
    public static abstract class FriendEntry implements BaseColumns {
        public static final String TABLE_NAME = "friends";
        public static final String COLUMN_NAME_USERNAME = "username";
    }

     /* Inner class that defines the friends table contents */
    public static abstract class GameEntry implements BaseColumns {
         public static final String TABLE_NAME = "games";
         public static final String COLUMN_NAME_SEL = "selection";
         public static final String COLUMN_NAME_HINT = "hint";
         public static final String COLUMN_NAME_TIME = "time";
         public static final String COLUMN_NAME_GUESS = "guess";
         public static final String COLUMN_NAME_VOTE = "vote";
         public static final String COLUMN_NAME_PICTURE = "picture_name";
         public static final String COLUMN_NAME_SENDER_ID = "sender";
         public static final String COLUMN_NAME_SENDER_NAME = "sender_name";
         public static final String COLUMN_NAME_CREATED = "created";
     }

}
