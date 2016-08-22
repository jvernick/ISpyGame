package com.picspy.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.Arrays;

/**
 * Provides single instance for making network requests with Volley
 */
public class VolleyRequest {
    private static final String TAG = "VolleyRequest";
    public static VolleyRequest mInstance;
    private static Context context;
    private RequestQueue mRequestQueue;

    private VolleyRequest(Context context) {
        VolleyRequest.context = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized VolleyRequest getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyRequest(context);
        }

        return mInstance;
    }

    /**
     * Parses Error object and converts it to reasonable network errors.
     * @param volleyError Received error from volley request
     * @return New error object with modified error message as appropriate
     */
    public static VolleyError parseNetworkError(VolleyError volleyError) {
        if (volleyError instanceof TimeoutError) {
            volleyError = new VolleyError(AppConstants.TIMEOUT_ERROR);
        }
        if (volleyError.networkResponse != null && volleyError.networkResponse.data != null) {
            Log.d(TAG, "error converted: " + Arrays.toString(volleyError.networkResponse.data));
            volleyError = new VolleyError(new String(volleyError.networkResponse.data));
        }
        //TODO check for session errors and refresh JWT
        return volleyError;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
