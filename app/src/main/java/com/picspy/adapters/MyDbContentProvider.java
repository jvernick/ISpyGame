package com.picspy.adapters;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by BrunelAmC on 1/5/2016.
 */
public class MyDbContentProvider extends ContentProvider {
    public static final String PROVIDER_NAME = "com.picspy.contentprovider.cursor";


    private static final String GAMES_PATH = "games";
    /**
     * A uri to do operations on cust_master table. A content provider is identified by its uri
     */
    public static final Uri GAMES_URI = Uri.parse("content://" + PROVIDER_NAME + "/" + GAMES_PATH);
    private static final String FRIENDS_PATH = "friends";
    public static final Uri FRIENDS_URI = Uri.parse("content://" + PROVIDER_NAME + "/" + FRIENDS_PATH);

    /**
     * Constants to identify the requested operation
     */
    private static final int GAMES = 10;
    private static final int FRIENDS = 20;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(PROVIDER_NAME, GAMES_PATH, GAMES);
        sURIMatcher.addURI(PROVIDER_NAME, FRIENDS_PATH, FRIENDS);
    }

    DatabaseHandler databaseHandler;

    @Override
    public boolean onCreate() {
        databaseHandler = DatabaseHandler.getInstance(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case GAMES:
                return databaseHandler.getAllGames();
            case FRIENDS:
                return databaseHandler.getAllFriends();
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
