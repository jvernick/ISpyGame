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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.picspy.firstapp.R;
import com.picspy.utils.Accounts;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;
import com.picspy.utils.RegistrationRequests;
import com.picspy.utils.VolleyRequest;

import java.util.Calendar;

import static com.picspy.views.Splash_Activity.verifyFCMToken;

/**
 * Activity for user login
 */
public class LoginActivity extends FragmentActivity {
    private static final String CANCEL_TAG = "loginUser";
    private static final String TAG = "LoginActivity";
    private EditText edtEmail, edtPaswd;
    private Button btnLogin;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(getText(R.string.loading_message));

        edtEmail = (EditText) findViewById(R.id.user_email);
        edtPaswd = (EditText) findViewById(R.id.user_password);
        btnLogin = (Button) findViewById(R.id.button_login);

        edtPaswd.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btnLogin.performClick();
                    return true;
                }
                return false;
            }
        });

        edtEmail.addTextChangedListener(textWatcher);
        edtPaswd.addTextChangedListener(textWatcher);

        //check if fields are empty
        checkFieldsForEmptyValues();
    }

    //Validates the input To disable button if any field is empty
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

    @Override
    protected void onResume() {
        super.onResume();
        if (!edtEmail.getText().toString().matches("") &&  !edtPaswd.getText().toString().matches("")) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(btnLogin.getWindowToken(), 0);
        }
    }

    /**
     * Checks fields for empty values. Enables and dislpays login button when all fields
     * are not empty. Otherwise disables and hides login button
     */
    private void checkFieldsForEmptyValues(){
        String s1 = edtEmail.getText().toString();
        String s2 = edtPaswd.getText().toString();

        if (s1.equals("") || s2.equals("")) {   //disables and greys out the button
            btnLogin.setEnabled(false);
            btnLogin.setVisibility(View.INVISIBLE);
            btnLogin.getBackground().setColorFilter(0xff888888, PorterDuff.Mode.MULTIPLY);
        } else {                                //enables and un-greys out the button
            btnLogin.setEnabled(true);
            btnLogin.setVisibility(View.VISIBLE);
            btnLogin.getBackground().clearColorFilter();
        }
    }

    /**
     * Validates password length
     * @return true if the length is appropriate, otherwise false
     */
    public boolean isValidPassword() {
        String pass = edtPaswd.getText().toString();
        return pass.length() >= 6;
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
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        Boolean email_state = isValidEmail();
        Boolean pass_state = isValidPassword();

        if (email_state && pass_state) {
            progressDialog.show();
            apiLogin();
        } else {
            if (!email_state) {
                edtEmail.setError("Invalid Email");
                edtEmail.requestFocus();
            }
            if (!pass_state) {
                edtPaswd.setError("Invalid Password");
                edtPaswd.requestFocus();
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
        verifyFCMToken(this);
        Intent intent = new Intent(this, MainActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
                Log.d(TAG, response.toString());
                progressDialog.cancel();
                if (response.getId() != 0) {
                    Accounts.checkNewAccount(context, response.getId());
                    //stores JWT token
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
                progressDialog.cancel();
                if (error != null) {
                    String err = (error.getMessage() == null)? "An error occurred": error.getMessage();
                    Log.d(TAG, err);
                    String errorMsg = "An error occurred";

                    if (err.matches(AppConstants.CONNECTION_ERROR) || err.matches(AppConstants.TIMEOUT_ERROR)) {
                        errorMsg = "No connection to server";
                    } else if (err.matches(".*Received authentication challenge is null.*")) {
                        errorMsg = "Invalid credentials";
                    }

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
                    alertDialog.setMessage(errorMsg).setCancelable(false).setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.show();
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
        //cancel pending login task
        VolleyRequest.getInstance(this.getApplication()).getRequestQueue().cancelAll(CANCEL_TAG);
    }
}
