package com.picspy.utils;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides constants that are used throughout the app and as keys to shared preferences.
 * Add vlues as needed.
 * Created by BrunelAmC on 6/9/2015.
 */
public class AppConstants {
    //The following are keys for shared preferences
    public static final String SESSION_ID = "session_id";
    public static final String USER_ID = "user_id";
    public static final String APP_NAME = "picspy";
    public static final String API_KEY = "api_key";
    public static final String PICSPY_API_KEY =
            "6eba9ba30d039fe36e4eb9f85531078a0d79a8be684df57685c0af59058318dd";
    //TODO change following constants as required (specifically just change the IP during testing)
    public static final String DSP_URL = "http://192.168.0.22:8080/rest";
    public static final String USERS_TABLE_NAME = "users";
    public static final String CONTAINER_NAME = "applications";
    public static final String FOLDER_NAME = "picspy/challenges";

    public static final String FRIENDS_TABLE_NAME = "friends";
    public static final String USER_CHALLENGES_TABLE_NAME = "user_challenges";
    public static final String CHALLENGES_TABLE_NAME = "challenges";

    public static final String MAX_USER_CHALLENGE_ID = "max_user_challenge_id";
    public static final String LAST_LOGIN_DATE = "last_login_date";
    public static final String MAX_FRIEND_RECORD_ID = "max_friend_record_id";

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



///////////////////////////////////NEW /FOR Version 2.0////////////////////////////////////////////
    public static Map<String, String> dspHeaders(Context context) {
        Map<String,String> headers = new HashMap<>();
        headers.put("X-DreamFactory-API-Key", PrefUtil.getString(context, API_KEY , PICSPY_API_KEY));
        headers.put("X-DreamFactory-Session-Token", PrefUtil.getString(context, SESSION_TOKEN));
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        return headers;
    }

    public static final String DSP_URL_2 = "http://192.168.0.13:8081/api/v2/";
    //session
    public static final String SESSION_TOKEN= "session_token";
    public static final String USER_NAME = "username";
    public static final int SESSION_TTL = 30;
    //matching regex when connection to server cannot be made
    public static final String CONNECTION_ERROR = ".*failed to connect to.*";

}