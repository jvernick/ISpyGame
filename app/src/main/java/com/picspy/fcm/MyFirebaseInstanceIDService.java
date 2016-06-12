package com.picspy.fcm;

import android.content.Context;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.picspy.models.UserRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;
import com.picspy.utils.UserRequests;
import com.picspy.utils.VolleyRequest;

/**
 * Created by BrunelAmC on 4/23/2016.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is also called
     * when the InstanceID token is initially generated, so this is where
     * you retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);
        PrefUtil.putString(this, AppConstants.FCM_TOKEN, refreshedToken);
        sendRegistrationToServer(refreshedToken, this);
    }
    // [END refresh_token]


    /**
     * Sends the token to the server for persistent storage and use
     * @param token The new token.
     */
    public static void sendRegistrationToServer(String token, final Context context) {
        String username = PrefUtil.getString(context, AppConstants.USER_NAME);
        final int id = PrefUtil.getInt(context, AppConstants.USER_ID);
        final UserRequests.AddUserModel addUserModel = new UserRequests.AddUserModel(id, username, token);
        //final Context context = this;
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

        UserRequests addUserRequest = UserRequests.updateGcmReg(context,
                addUserModel, responseListener, errorListener);
        VolleyRequest.getInstance(context.getApplicationContext()).addToRequestQueue(addUserRequest);
    }

}