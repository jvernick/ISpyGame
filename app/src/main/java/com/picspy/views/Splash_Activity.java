package com.picspy.views;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.dreamfactory.api.UserApi;
import com.dreamfactory.model.Session;
import com.picspy.firstapp.R;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;

/**
 * Splashcreen activity that displays logo and determines whether to proceed
 * to main screen or to prompt for user login/registration. Pauses for 2 seconds
 * to display logo on startup
 * Created by BrunelAmC on 6/9/2015.
 */
public class Splash_Activity extends Activity {
    private Button splash_login, splash_signup;
    private static final int SLEEP_TIME = 2000;
    private static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //TODO: Temporary. for testing login screen (always enabling it)
        //PrefUtil.removeString(getApplicationContext(), AppConstants.SESSION_ID);
        splash_login = (Button) findViewById(R.id.splash_login);
        splash_signup = (Button) findViewById(R.id.splash_signup);

        //TODO change true in both lines below to false
        splash_login.setEnabled(false);
        splash_signup.setEnabled(false);
        new StartMyApp().execute();
    }

    /**
     * Starts the login activity
     * @param view View to be used, from button click
     */
    public void splashLogin(View view) {
        Intent intent = new Intent(Splash_Activity.this, LoginActivity.class);
        startActivity(intent);
    }

    /**
     * Starts the register activity
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
        Log.d("Splash","Starting Main");
        Intent intent = new Intent(Splash_Activity.this, MainActivity.class);
        startActivity(intent);
        //TODO uncomment after all testing is complete so that one never returns to splasy activity
        //finish();
    }

    /**
     * Inner Asynctask to Attempt login by refreshing session
     */
    private class StartMyApp extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            String oldSessionKey = PrefUtil.getString(getApplicationContext(),
                    AppConstants.SESSION_ID, null);

            //sleep to display logo
            try {
                Thread.sleep(SLEEP_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // checks if sesison_id already exists
            if (oldSessionKey == null) { //User has never logged in before
                return false;
            } else { //User has logged in before. Start main activity
                //TODO session may have expired
                startMain();
                //refresh session if possible
                try {
                    UserApi userApi = new UserApi();
                    userApi.addHeader("X-DreamFactory-Application-Name", AppConstants.APP_NAME);
                    userApi.addHeader("X-DreamFactory-Session-Token", oldSessionKey);
                    Session session = userApi.getSession();
                    if (session != null && session.getId() != null) { //session not null
                        Log.d(TAG, "session not null");
                        PrefUtil.putString(getApplicationContext(), AppConstants.SESSION_ID,
                                session.getSession_id());
                        PrefUtil.putInt(getApplicationContext(), AppConstants.USER_ID,
                                Integer.parseInt(session.getId()));
                    } else { //prompts for login/register if session refresh failed
                        return false; //This is needed since we haven't figured out a way
                        // to have infinite session duration
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, e.getMessage());
                    //PrefUtil.putString(getApplicationContext(), AppConstants.SESSION_ID, "");
                }
                return true;
            }
        }

        @Override
        //TODO add response listener to finish activity
        protected void onPostExecute(Boolean isOldSession) {
            //if there has been no previous session.
            if (!isOldSession) {
                splash_login.setEnabled(true);
                splash_login.setVisibility(View.VISIBLE);
                splash_signup.setEnabled(true);
                splash_signup.setVisibility(View.VISIBLE);
            }
        }
    }
}
