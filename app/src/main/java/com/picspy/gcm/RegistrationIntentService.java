package com.picspy.gcm;

import android.app.AlertDialog;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;
import com.picspy.firstapp.R;
import com.picspy.models.UserRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;
import com.picspy.utils.UserRequests;
import com.picspy.utils.VolleyRequest;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by BrunelAmC on 4/22/2016.
 */
public class RegistrationIntentService extends IntentService{
    // abbreviated tag name
    private static final String TAG = "RegIntentService";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * TAG Used to name the worker thread, important only for debugging.
     */
    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Make a call to Instance API
        InstanceID instanceID = InstanceID.getInstance(this);
        String senderId = getResources().getString(R.string.gcm_sender_id);
        try {
            // request token that will be used by the server to send push notifications
            String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
            Log.d(TAG, "GCM Registration Token: " + token);

            PrefUtil.putLong(this, AppConstants.LAST_TOK_DATE, Calendar.getInstance().getTimeInMillis());
            PrefUtil.putString(this, AppConstants.GCM_TOKEN, token);

            // pass along this data
            sendRegistrationToServer(token);
        } catch (IOException e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            e.printStackTrace();
        }
    }

    /**
     * Sends the token to the server for persistent storage and use
     * @param token Gcm registration token
     */
    private void sendRegistrationToServer(String token) {
        String username = PrefUtil.getString(this, AppConstants.USER_NAME);
        final int id = PrefUtil.getInt(this, AppConstants.USER_ID);
        final UserRequests.AddUserModel addUserModel = new UserRequests.AddUserModel(id, username, token);
        final Context context = this;
        Response.Listener<UserRecord> responseListener = new Response.Listener<UserRecord>() {
            @Override
            public void onResponse(UserRecord response) {
                if (response != null && response.getId() == id) {
                    PrefUtil.putBoolean(context, AppConstants.SENT_TOKEN_TO_SERVER, true);
                    Log.d(TAG, "RegToken successfully sent to server");
                } else {
                    Log.d(TAG, "An error occurred");
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    PrefUtil.putBoolean(context, AppConstants.SENT_TOKEN_TO_SERVER, false);
                    String err = (error.getMessage() == null)? "error message null" : error.getMessage();
                    Log.d(TAG, err);
                }
            }
        };

        UserRequests addUserRequest = UserRequests.updateGcmReg(this,
                addUserModel, responseListener, errorListener);
        VolleyRequest.getInstance(this.getApplicationContext()).addToRequestQueue(addUserRequest);
    }

}
