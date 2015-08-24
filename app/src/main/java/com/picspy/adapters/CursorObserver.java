package com.picspy.adapters;

import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.util.Log;

/**TODO document
 * TODO delete, not used
 * Created by BrunelAmC on 8/22/2015.
 */
public class CursorObserver extends ContentObserver {
    private static String TAG = "CursorObserver";
    private Loader<Cursor> loader;

    public CursorObserver(Handler handler,  Loader<Cursor> loader) {
        super(handler);
        Log.e(TAG, ":::: CursorObserver");

        this.loader = loader;
    }

    @Override
    public boolean deliverSelfNotifications() {
        Log.e(TAG, ":::: deliverSelfNotifications");
        return true;
    }

    @Override
    public void onChange(boolean selfChange) {
        Log.e(TAG, ":::: onChange");

        if (null != loader) {
            loader.onContentChanged();
        }
        super.onChange(selfChange);
    }
}
