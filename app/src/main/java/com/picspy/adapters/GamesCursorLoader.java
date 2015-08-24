package com.picspy.adapters;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**
 * Cursor loader to get games from sqlite in background
 * Created by BrunelAmC on 8/22/2015.
 */
public class GamesCursorLoader extends AsyncTaskLoader<Cursor> {

    private static String TAG ="GamesCursorLoader";

    private Context context;
    private DatabaseHandler dbHandler;

    //Default constructor
    public GamesCursorLoader(Context context, DatabaseHandler dbHandler) {
        super(context);
        this.context = context;
        this.dbHandler = dbHandler;
    }

    @Override
    public Cursor loadInBackground() {
        Log.e(TAG, ":::: loadInBackground");

       return dbHandler.getAllGames();
    }

}
