package com.picspy.views;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.picspy.firstapp.R;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;
import com.picspy.utils.RegistrationRequests;
import com.picspy.utils.VolleyRequest;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static com.picspy.fcm.MyFirebaseInstanceIDService.sendRegistrationToServer;

/**
 * Splashcreen activity that displays logo and determines whether to proceed
 * to main screen or to prompt for user login/registration. Pauses for 2 seconds
 * to display logo on startup
 * Created by BrunelAmC on 6/9/2015.
 */
public class Splash_Activity extends Activity {
    private static final String CANCEL_TAG = "cancelJWTRefresh";
    private static final int SLEEP_TIME = 1000;
    private static final String TAG = "SplashActivity";
    ProgressBar progressSpinner;
    private Button btn_login, btn_signup;
    private View buttons;

    /**
     * Computes the number of days since the last login
     *
     * @param lastLoginDate the last login date
     * @return number of days since last login
     */
    public static int daysSinceLastLogin(Long lastLoginDate) {
        Long difference = Calendar.getInstance().getTimeInMillis() - lastLoginDate;
        return (int) TimeUnit.MILLISECONDS.toDays(difference);
    }

    public static void verifyFCMToken(Context context) {
        boolean tokenSent = PrefUtil.getBoolean(context, AppConstants.SENT_TOKEN_TO_SERVER, false);
        String fcmToken = PrefUtil.getString(context, AppConstants.FCM_TOKEN, null);

        if (!tokenSent && fcmToken != null) sendRegistrationToServer(fcmToken, context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        btn_login = (Button) findViewById(R.id.splash_login);
        btn_signup = (Button) findViewById(R.id.splash_signup);
        buttons = findViewById(R.id.buttons);
        progressSpinner = (ProgressBar) findViewById(R.id.splash_progressSpinner);
        progressSpinner.setVisibility(View.GONE);

        //TODO change true in both lines below to false
        //btn_login.setEnabled(false);
        //btn_signup.setEnabled(false);
        buttons.setVisibility(View.GONE);
        determineAction();
    }

    /**
     * Determine whether or not main activity should be displayed and whether to refresh JWT
     */
    private void determineAction() {
        // if there is no user
        if (PrefUtil.getInt(getApplicationContext(), AppConstants.USER_ID, -1) == -1) {
            enableButtons();
        } else {
            int daysDiff = daysSinceLastLogin(PrefUtil.getLong(getApplicationContext(),
                    AppConstants.LAST_LOGIN_DATE));
            Log.d(TAG, "days since last login: " + daysDiff);
            if (daysDiff >= AppConstants.SESSION_TTL - 5) {
                refreshJWTToken(getApplicationContext());
            } else {
                startMain();
            }
        }
    }

    /**
     * enables and makes login and signup buttons visible
     */
    private void enableButtons() {
        btn_login.setEnabled(true);
        btn_login.setVisibility(View.VISIBLE);
        btn_signup.setEnabled(true);
        btn_signup.setVisibility(View.VISIBLE);
        buttons.setVisibility(View.VISIBLE);
    }

    /**
     * Starts the login activity
     *
     * @param view View to be used, from button click
     */
    public void splashLogin(View view) {
        Intent intent = new Intent(Splash_Activity.this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Starts the register activity
     *
     * @param view View to be used, from button click
     */
    public void splashSignUp(View view) {
        Intent intent = new Intent(Splash_Activity.this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Starts the main activity
     */
    public void startMain() {
        verifyFCMToken(this);
        Log.d("Splash", "Starting Main");
        Intent intent = new Intent(Splash_Activity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Refreshes the JWT for forever sessions
     */
    public void refreshJWTToken(final Context applicationContext) {
        //TODO configure JWT Refresh
        //TODO reset days since last login
        Log.d(TAG, "Refreshing JWT");
        Response.Listener<RegistrationRequests.LoginApiResponse> responseListener = new Response.Listener<RegistrationRequests.LoginApiResponse>() {
            @Override
            public void onResponse(RegistrationRequests.LoginApiResponse response) {
                progressSpinner.setVisibility(View.GONE);
                if (response != null) { //TODO check that it matches regex
                    PrefUtil.putString(applicationContext, AppConstants.SESSION_TOKEN, response.getSessionToken());
                    PrefUtil.putLong(applicationContext, AppConstants.LAST_LOGIN_DATE, Calendar.getInstance().getTimeInMillis());
                    Log.d(TAG, response.toString());
                    startMain();
                } else {
                    splashLogin(null);
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressSpinner.setVisibility(View.GONE);
                if (error.getMessage() != null)
                    Log.d(TAG, error.getMessage());
                splashLogin(null);
            }
        };

        RegistrationRequests jwtRefresh = RegistrationRequests.refreshJwtToken(applicationContext,
                responseListener, errorListener);
        jwtRefresh.setTag(CANCEL_TAG);
        VolleyRequest.getInstance(applicationContext).addToRequestQueue(jwtRefresh);
        progressSpinner.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        VolleyRequest.getInstance(this.getApplication()).getRequestQueue().cancelAll(CANCEL_TAG);
    }
}
