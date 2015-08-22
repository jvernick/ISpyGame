package com.picspy.views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dreamfactory.api.UserApi;
import com.dreamfactory.client.ApiException;
import com.dreamfactory.model.Login;
import com.dreamfactory.model.Session;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.picspy.firstapp.R;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity for user login
 */
public class LoginActivity extends FragmentActivity {
    private EditText  email_text, pass_text;
    private Button login_button;
    private ProgressDialog progressDialog;
    private View button_view = null;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("keyhas", "tset");
        Log.d("keyhas",this.getPackageName());

        //facebook login setup
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        setContentView(R.layout.activity_login);
        LoginButton facebook_button = (LoginButton) findViewById(R.id.facebook_login_button);
        facebook_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //TODO store the access token? or understand how facebook sdk stores it.
                AccessToken s = loginResult.getAccessToken();
                //for debuging
                Toast.makeText(LoginActivity.this, s.getExpires().toString(),
                        Toast.LENGTH_LONG).show();

                Toast.makeText(LoginActivity.this, "Facebook Login Successful",
                        Toast.LENGTH_SHORT).show();
                showResults(button_view);
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Login attempt canceled.",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException e) {
                Toast.makeText(LoginActivity.this, "Login attempt failed.", Toast.LENGTH_LONG).show();
            }
        });

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(getText(R.string.loading_message));

        email_text = (EditText) findViewById(R.id.user_email);
        pass_text = (EditText) findViewById(R.id.user_password);
        login_button = (Button) findViewById(R.id.button_login);

        email_text.addTextChangedListener(textWatcher);
        pass_text.addTextChangedListener(textWatcher);

        //check if fields are empty
        checkFieldsForEmptyValues();
    }

    //for facebok login
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
    //Validates the input To disable button if any field is empyt
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3){}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3)  {}

        @Override
        public void afterTextChanged(Editable editable) {
            checkFieldsForEmptyValues();
        }
    };

    /**
     * Checks fields for empty values. Enables and dislpays login button when all fields
     * are not empty. Otherwise disales and hides login button
     */
    private void checkFieldsForEmptyValues(){
        String s1 = email_text.getText().toString();
        String s2 = pass_text.getText().toString();

        if (s1.equals("") || s2.equals("")) {   //disables and greys out the button
            login_button.setEnabled(false);
            login_button.setVisibility(View.INVISIBLE);
            login_button.getBackground().setColorFilter(0xff888888, PorterDuff.Mode.MULTIPLY);
        } else {                                //enables and ungreys out the button
            login_button.setEnabled(true);
            login_button.setVisibility(View.VISIBLE);
            login_button.getBackground().clearColorFilter();
        }
    }

    /**
     * Validates password length
     * @return true if the length is appropriate, otherwise false
     */
    public boolean isValidPassword() {
        String pass = pass_text.getText().toString();;
        return pass_text.length() >= 6;
    }


    /**
     * Validates email with regex
     * @return true if email is valid, otherwise false
     */
    private boolean isValidEmail() {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        String email = email_text.getText().toString();
        return email.matches(EMAIL_PATTERN);
    }

    /**
     * Validates fields and peforms login
     * @param view View from button click
     */
    public void login(View view) {
        Boolean email_state = isValidEmail();
        Boolean pass_state = isValidPassword();
        button_view = view;

        if (email_state && pass_state) {
            LoginTask loginTask = new LoginTask();
            loginTask.execute();
        } else {
            if (!email_state) {
                email_text.setError("Invalid Email");
            }
            if (!pass_state) {
                pass_text.setError("Invalid Password");
            }
        }
    }

    /**
     * Starts activity to reset password
     * @param view View from button click
     */
    public void forgotPassword(View view) {
        Log.d("login","sigup clicked");
        //TODO change to password reset activity after it has been implemented
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Starts the main activity after user logs in
     * @param view View from button click
     */
    private void showResults(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Class to run network transaction in background to login user.
     */
    private class LoginTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                Session session = loginSercice();
                if (session != null) {
                    PrefUtil.putString(getApplicationContext(), AppConstants.SESSION_ID,
                            session.getSession_id());
                    PrefUtil.putInt(getApplicationContext(), AppConstants.USER_ID,
                            Integer.parseInt(session.getId()));
                } else {
                    return "FAILURE";
                }
            } catch (ApiException e) {
                return e.getMessage();
            }
            return "SUCCESS";
        }

        @Override
        // Calls method to create new activity that displays registration response
        protected void onPostExecute(String message) {
            Log.d("login", "network done");
            progressDialog.cancel();
            if (message.equals("SUCCESS")) { //successful
                showResults(button_view);
            } else { //error, exception thrown
                String errorMsg;
                try {
                    JSONObject jObj = new JSONObject(message);
                    JSONArray jArray = jObj.getJSONArray("error");
                    JSONObject obj = jArray.getJSONObject(0);
                    errorMsg = obj.getString("message");

                    //TODO Challenge!! match the displayname error with a regex
                    if (errorMsg.matches("^A registered user already exists(.*)")) {
                        errorMsg = "Email already taken.";
                    } else if (errorMsg.trim().contains("Display Name")) {
                        errorMsg = "Display Name taken";
                    }
                } catch (JSONException e) { //message is from exception
                    //TODO customize message if an exception was thrown?
                    errorMsg = message;
                }
                
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
                //TODO modify error presentation format and possibly the error message
                alertDialog.setTitle("Message").setMessage(errorMsg).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }
        }

        /**
         * Attmep user login
         * @return Returns a session when login is successful
         * @throws ApiException when an Api error occurs
         */
        private Session loginSercice() throws ApiException {
            UserApi userApi = new UserApi();
            userApi.addHeader("X-DreamFactory-Application-Name", AppConstants.APP_NAME);
            Login login = new Login();
            login.setEmail(email_text.getText().toString());
            login.setPassword(pass_text.getText().toString());
            Session session = userApi.login(login);
            if (session == null) return null; //should never occur TODO verify and handle this
            return session;
        }
    }
}