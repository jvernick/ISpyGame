package com.picspy.utils;

/**
 * Provides constants that are used throughout the app and as keys to shared preferences.
 * Add vlues as needed.
 * Created by BrunelAmC on 6/9/2015.
 */
public class AppConstants {
    //The following are keys for shared prefrecnes
    public static final String SESSION_ID = "session_id";
    public static final String USER_ID = "user_id";
    public static final String APP_NAME = "picspy";
    public static final String FRIENDS_TABLE_NAME = "friends";
    //TODO change following constants as required (specifically just change the IP during testing)
    public static final String DSP_URL = "http://10.48.10.54:8080/rest";
    public static final String USERS_TABLE_NAME = "users";

    // To prevent someone from accidentally instantiating the AppConstants class,
    // give it an empty constructor.
    private AppConstants() {};
}
