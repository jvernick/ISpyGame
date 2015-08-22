package com.picspy.adapters;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

/**TODO Document
 * Created by BrunelAmC on 8/22/2015.
 */
public class GamesCursorLoader extends AsyncTaskLoader<Cursor> {

    private static String TAG ="GamesCursorLoader";

    private Context context;
    private DatabaseHandler dbHandler;

    public GamesCursorLoader(Context context, DatabaseHandler dbHandler) {
        super(context);
        this.context = context;
        this.dbHandler = dbHandler;
    }

    @Override
    protected void onStartLoading() {
        Log.e(TAG, ":::: onStartLoading");

        super.onStartLoading();
    }

    @Override
    public Cursor loadInBackground() {
        Log.e(TAG, ":::: loadInBackground");

       return dbHandler.getAllGames();
    }

    @Override
    public void deliverResult(Cursor data) {
        Log.e(TAG, ":::: deliverResult");

        super.deliverResult(data);
    }


    @Override
    protected void onStopLoading() {
        Log.e(TAG, ":::: onStopLoading");

        super.onStopLoading();
    }
}
