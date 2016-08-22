package com.picspy.utils;

import android.net.Uri;

import java.util.HashMap;

/**
 * Provides an abstraction for request urls
 */
public class DspUriBuilder {
    public static final String CHALLENGES_TABLE = "mysql/_table/challenges";
    public static final String USERS_TABLE = "mysql/_table/users";
    public static final String FRIENDS_TABLE = "mysql/_table/friends";
    public static final String USER_CHALLENGES_TABLE = "mysql/_table/user_challenges";
    public static final String FILE_URI = "files/images/";
    public static final String LOGIN_URI = "user/session";
    public static final String REGISTRATION_URI = "user/register";

    /**
     * Constructs full url with request parameters
     * @param path url base path
     * @param params request parameters
     * @return full url
     */
    public static String buildUri(String path, HashMap<String, String> params) {
        String baseUrl = AppConstants.DSP_URL_2 + path + '?';
        if (params == null) return baseUrl;

        Uri builtUri;
        builtUri = Uri.parse(baseUrl);
        for (HashMap.Entry<String, String> entry : params.entrySet()) {
            builtUri = builtUri.buildUpon().appendQueryParameter(entry.getKey(), entry.getValue()).build();
        }

        return builtUri.toString();
    }

    /**
     * Constructs full url for image uploads
     * @param path base url path
     * @param filename image file name
     * @return full image upload url
     */
    public static String buildFileUploadUri(String path, String filename) {
        String baseUrl = AppConstants.DSP_URL_2 + path + filename + '?';
        Uri builtUri;

        builtUri = Uri.parse(baseUrl);
        builtUri.buildUpon().appendPath(filename).build();

        return builtUri.toString();
    }

    /**
     * Constructs full url for api delete by id
     * @param path base url path
     * @param id id for record to delete
     * @param params request parameters
     * @return full url
     */
    public static String buildDeleteByIdUri(String path, int id, HashMap<String, String> params) {
        String baseUrl = AppConstants.DSP_URL_2 + path + "/" + id + "?";
        if (params == null) return baseUrl;

        Uri builtUri;
        builtUri = Uri.parse(baseUrl);
        for (HashMap.Entry<String, String> entry : params.entrySet()) {
            builtUri = builtUri.buildUpon().appendQueryParameter(entry.getKey(), entry.getValue()).build();
        }

        return builtUri.toString();
    }
}
