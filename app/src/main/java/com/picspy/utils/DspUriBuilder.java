package com.picspy.utils;

import android.net.Uri;

import java.util.HashMap;

/**
 * Created by BrunelAmC on 1/17/2016.
 */
public class DspUriBuilder {
    //tables
    public static final String CHALLENGES_TABLE = "mysql/_table/challenges";
    public static final String USERS_TABLE = "mysql/_table/users";
    public static final String FRIENDS_TABLE = "mysql/_table/friends";
    public static final String USER_CHALLENGES_TABLE = "mysql/_table/user_challenges";
    public static final String FILE_URI = "files/images/";

    public static String buildUri(String path, HashMap<String,String> parmas) {
        String baseUrl = AppConstants.DSP_URL_2 + path + '?';
        Uri builtUri;

        if (parmas == null) return baseUrl;

        builtUri = Uri.parse(baseUrl);
        for (HashMap.Entry<String, String> entry: parmas.entrySet()) {
            builtUri = builtUri.buildUpon().appendQueryParameter(entry.getKey(), entry.getValue()).build();
        }

        return  builtUri.toString();
    }

    //TODO Rename method?
    public static String buildFileUploadUri(String path, String filename) {
        String baseUrl = AppConstants.DSP_URL_2 + path + filename + '?';
        Uri builtUri;

        builtUri = Uri.parse(baseUrl);
        builtUri.buildUpon().appendPath(filename).build();

        return  builtUri.toString();
    }

    public static String buildDeleteByIdUri(String path, int id, HashMap<String, String> parmas) {
        String baseUrl = AppConstants.DSP_URL_2 + path + "/" + id + "?";
        Uri builtUri;

        if (parmas == null) return baseUrl;

        builtUri = Uri.parse(baseUrl);
        for (HashMap.Entry<String, String> entry: parmas.entrySet()) {
            builtUri = builtUri.buildUpon().appendQueryParameter(entry.getKey(), entry.getValue()).build();
        }

        return  builtUri.toString();
    }
}
