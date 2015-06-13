package com.picspy;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;

import com.dreamfactory.api.UserApi;
import com.dreamfactory.model.Session;
import com.picspy.firstapp.R;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;

import java.util.Objects;


public class Splash_Activity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new StartMyApp().execute();
    }


    private class StartMyApp extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean validSession = false;
            String oldSessionKey = PrefUtil.getString(getApplicationContext(),
                    AppConstants.SESSION_ID, null);
            Log.d("Splash", "after getting old sesion" + oldSessionKey);
            if (oldSessionKey == null) {
                // show splash for 2 secs and go to login
                try {
                    Log.d("SplasH","sleeping");
                    Thread.sleep(2000);
                    Log.d("SplasH", "waking up");
                } catch (InterruptedException e) {
                    //TODO: where does this print to? should not print to screen
                    e.printStackTrace();
                    Log.d("splash", e.getMessage());
                }
            } else { // previously logged in, check if still valid
                try {
                    UserApi userApi = new UserApi();
                    userApi.addHeader("X-DreamFactory-Applicatin-Name", AppConstants.APP_NAME);
                    userApi.addHeader("X-DreamFactory-Session-Token", oldSessionKey);
                    Session session = userApi.getSession();
                    if (session != null) {
                        Log.d("sp", "session not null");
                        PrefUtil.putString(getApplicationContext(), AppConstants.SESSION_ID, session.getSession_id());
                        validSession = true;
                    }
                } catch (Exception e) { //TODO log exception for debugging?
                    Log.d("SplashActivity", e.getMessage());
                    PrefUtil.putString(getApplicationContext(), AppConstants.SESSION_ID, "");
                }
            }
            return validSession;
        }
        @Override
        protected void onPostExecute(Boolean isValidSession) {
            Log.d("s","shoudl print");
            if (isValidSession) {
                Intent intent = new Intent(Splash_Activity.this, MainActivity.class);
                startActivity(intent);
            } else { //TODO change registeractivity to lgoin activity
                Intent intent = new Intent(Splash_Activity.this, RegisterActivity.class);
                startActivity(intent);
            }
            finish();
        }
    }
}
