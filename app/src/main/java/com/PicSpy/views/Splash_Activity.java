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


public class Splash_Activity extends Activity {
    private Button splash_login, splash_signup;

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

    public void splashLogin(View view) {
        Intent intent = new Intent(Splash_Activity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void splashSignUp(View view) {
        Intent intent = new Intent(Splash_Activity.this, RegisterActivity.class);
        startActivity(intent);
    }



    private class StartMyApp extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            boolean validSession = false;
            String oldSessionKey = PrefUtil.getString(getApplicationContext(),
                    AppConstants.SESSION_ID, null);
            Log.d("Splash", "after getting old session" + oldSessionKey);
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
            } else { //refresh session if possible
                try {
                    UserApi userApi = new UserApi();
                    userApi.addHeader("X-DreamFactory-Application-Name", AppConstants.APP_NAME);
                    userApi.addHeader("X-DreamFactory-Session-Token", oldSessionKey);
                    Session session = userApi.getSession();
                    if (session != null && session.getId() != null) {
                        Log.d("sp", "session not null");
                        PrefUtil.putString(getApplicationContext(), AppConstants.SESSION_ID, session.getSession_id());
                        //TODO temp, remove line below
                        PrefUtil.putInt(getApplicationContext(), AppConstants.USER_ID, Integer.parseInt(session.getId()));
                    } else {
                        return false; //This is needed since we haven't figured out a way to have infinite session duration
                    }
                } catch (Exception e) { //TODO log exception for debugging? Network error or expired session
                    e.printStackTrace();
                    Log.d("SplashActivity", e.getMessage());
                    //PrefUtil.putString(getApplicationContext(), AppConstants.SESSION_ID, "");
                }
                validSession = true;
            }
            return validSession;
        }
        @Override
        //TODO add response listener to finish activity
        protected void onPostExecute(Boolean isValidSession) {
            if (isValidSession) {
                Intent intent = new Intent(Splash_Activity.this, MainActivity.class);
                startActivity(intent);
            } else {
                splash_login.setEnabled(true);
                splash_login.setVisibility(View.VISIBLE);
                splash_signup.setEnabled(true);
                splash_signup.setVisibility(View.VISIBLE);
            }
        }
    }
}
