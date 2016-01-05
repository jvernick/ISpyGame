package com.picspy.utils;

import android.graphics.Color;

import java.util.ArrayList;

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
    //TODO change following constants as required (specifically just change the IP during testing)
    public static final String DSP_URL = "http://192.168.0.17:8080/rest";
    public static final String USERS_TABLE_NAME = "users";
    public static final String CONTAINER_NAME = "applications";
    public static final String FOLDER_NAME = "picspy/challenges";

    public static final String FRIENDS_TABLE_NAME = "friends";
    public static final String USER_CHALLENGES_TABLE_NAME = "user_challenges";
    public static final String CHALLENGES_TABLE_NAME = "challenges";

    public static final String LAST_USER_CHALLENGE_ID = "last_user_challenge_id";
    public static final String LAST_FRIEND_UPDATE_TIME = "last_friend_update_time";

    public static final String FRIEND_REQUEST_COUNT = "friend_request_count";
    public static final String CHALLENGE_REQUEST_COUNT = "challenge_request_count";

    //olors for friend icon TODO do we need more colors?
    //currently choosing color by userID % array length
    public static final int[] COLOR_ARRAY_LIST = {
            0xFFEF9A9A, //red_200
            0xFFC5E1A5, //lightGreen_200
            0xFF9FA8DA, //indigo_200
            0xFFFFCC80, //orange_200
            0xFFE6EE9C, //lime_200
            0xFFCE93D8  //purple_200
    };
    // To prevent someone from accidentally instantiating the AppConstants class,
    // give it an empty constructor.
    private AppConstants() {};
}
