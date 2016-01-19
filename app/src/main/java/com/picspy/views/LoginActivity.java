package com.picspy.views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.picspy.firstapp.R;
import com.picspy.utils.Accounts;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;
import com.picspy.utils.RegistrationRequests;
import com.picspy.utils.VolleyRequest;

import java.util.Calendar;
import java.util.Date;

/**
 * Activity for user login
 */
public class LoginActivity extends FragmentActivity {
    private static final Object CANCEL_TAG = "cancel";
    private static final String TAG = "LoginActivity";
    private EditText edtEmail, edtPaswd;
    private Button login_button;
    private ProgressDialog progressDialog;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                startMain();
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

        edtEmail = (EditText) findViewById(R.id.user_email);
        edtPaswd = (EditText) findViewById(R.id.user_password);
        login_button = (Button) findViewById(R.id.button_login);

        edtEmail.addTextChangedListener(textWatcher);
        edtPaswd.addTextChangedListener(textWatcher);

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
        String s1 = edtEmail.getText().toString();
        String s2 = edtPaswd.getText().toString();

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
        String pass = edtPaswd.getText().toString();;
        return edtPaswd.length() >= 6;
    }


    /**
     * Validates email with regex
     * @return true if email is valid, otherwise false
     */
    private boolean isValidEmail() {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        String email = edtEmail.getText().toString();
        return email.matches(EMAIL_PATTERN);
    }

    /**TODO change error messages to string resources
     * Validates fields and peforms login
     * @param view View from button click
     */
    public void login(View view) {
        Boolean email_state = isValidEmail();
        Boolean pass_state = isValidPassword();

        if (email_state && pass_state) {
            progressDialog.show();
            apiLogin();
        } else {
            if (!email_state) {
                edtEmail.setError("Invalid Email");
            }
            if (!pass_state) {
                edtPaswd.setError("Invalid Password");
            }
        }
    }

    /**
     * Starts activity to reset password
     * @param view View from button click
     */
    public void forgotPassword(View view) {
        //TODO change to password reset activity after it has been implemented
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    /**
     * Starts the main activity after user logs in
     */
    private void startMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * attempts to login the user to the server.
     */
    private void apiLogin() {
        RegistrationRequests.LoginModel loginModel = new RegistrationRequests.LoginModel(
                edtEmail.getText().toString(), edtPaswd.getText().toString(), true);

        final Context context = this;
        Response.Listener<RegistrationRequests.LoginApiResponse> responseListener = new Response.Listener<RegistrationRequests.LoginApiResponse>() {
            @Override
            public void onResponse(RegistrationRequests.LoginApiResponse response) {
                if (response.getId() != 0) {
                    Accounts.checkNewAccount(context, response.getId());
                    PrefUtil.putString(context, AppConstants.SESSION_TOKEN, response.getSessionToken());
                    PrefUtil.putInt(context, AppConstants.USER_ID, response.getId());
                    PrefUtil.putString(context, AppConstants.USER_NAME, response.getUsername());
                    PrefUtil.putLong(context, AppConstants.LAST_LOGIN_DATE, Calendar.getInstance().getTimeInMillis());
                    startMain();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    String err = (error.getMessage() == null)? "error message null": error.getMessage();
                    Log.d(TAG, err);
                    error.printStackTrace();
                    String errorMsg = err;
                    progressDialog.cancel();

                    //TODO  match error
                    if (err.matches("^A registered user already exists(.*)")) {
                        errorMsg = "Email already taken.";
                    } else if (err.trim().contains("Display Name")) {
                        errorMsg = "Display Name taken";
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
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
        };

        RegistrationRequests loginRequest = RegistrationRequests.login(this,
                loginModel, responseListener, errorListener);
        if (loginRequest != null) loginRequest.setTag(CANCEL_TAG);
        VolleyRequest.getInstance(this.getApplicationContext()).addToRequestQueue(loginRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //cancel all pending register/login/addUser tasks
        if (VolleyRequest.getInstance(this.getApplicationContext()) != null) {
            VolleyRequest.getInstance(this.getApplication()).getRequestQueue().cancelAll(CANCEL_TAG);
        }
    }

}
