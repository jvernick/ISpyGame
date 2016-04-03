package com.picspy.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Network;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.dreamfactory.model.Login;

/**
 * Created by BrunelAmC on 1/13/2016.
 */
public class VolleyRequest {
    private static final String TAG = "VolleyRequest";
    public static VolleyRequest mInstance;
    private RequestQueue mRequestQueue;
    private static Context context;

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


    public static VolleyError parseNetworkError(VolleyError volleyError) {
        if(volleyError.networkResponse != null && volleyError.networkResponse.data != null){
            Log.d(TAG, "erorr converted: " + volleyError.networkResponse.data);
            volleyError = new VolleyError(new String(volleyError.networkResponse.data));
        }

        //TODO check for session errors and refresh JWT
        return volleyError;
    }
}
