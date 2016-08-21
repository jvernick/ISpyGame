package com.picspy.views;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
public class LoginActivity extends ActionBarActivity {
    private static final String CANCEL_TAG = "loginUser";
    private static final String TAG = "LoginActivity";
    private EditText edtEmail, edtPaswd;
    private Button btnLogin;
    private ProgressDialog progressDialog;

    public enum FIELD_STATUS {EMPTY, TOO_SHORT, INVALID, VALID}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage(getText(R.string.login_loading));

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

        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkFieldsForEmptyValues();
                edtEmail.setError(null);
            }
        });
        edtPaswd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkFieldsForEmptyValues();
                edtPaswd.setError(null);
            }
        });

        // Setting toolbar as the ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.tx_login));
        //toolbar.setTitleTextColor(getResources().getColor(R.color.primary_dark));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_black);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            //handling back button click
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks fields for empty values. Enables and displays login button when all fields
     * are not empty. Otherwise disables and hides login button
     */
    private void checkFieldsForEmptyValues() {
        String s1 = edtEmail.getText().toString();
        String s2 = edtPaswd.getText().toString();

        if (s1.equals("") || s2.equals("")) {
            btnLogin.setEnabled(false);
        } else {
            btnLogin.setEnabled(true);
        }
    }

    /**
     * Validates password length
     *
     * @return appropriate field status
     */
    public static FIELD_STATUS validatePassword(String password) {
        if (password.length() == 0) {
            return FIELD_STATUS.EMPTY;
        } else if (password.length() < 6) {
            return FIELD_STATUS.TOO_SHORT;
        }

        return FIELD_STATUS.VALID;
    }

    /**
     * Displays the appropriate error on the password field if the password is invalid
     *
     * @param edtPaswd Field to be modified
     * @return true if password field is valid, otherwise false
     */
    private boolean isValidPassword(EditText edtPaswd) {
        FIELD_STATUS status = validatePassword(edtPaswd.getText().toString());

        switch (status) {
            case EMPTY:
                edtPaswd.setError(getString(R.string.required_field));
                return false;
            case TOO_SHORT:
                edtPaswd.setError(getString(R.string.password_too_short));
                return false;
            case VALID:
                return true;
            default:
                return false;
        }
    }

    /**
     * Validates email with regex
     *
     * @return appropriate field status
     */
    public static FIELD_STATUS validateEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        if (email.length() == 0) {
            return FIELD_STATUS.EMPTY;
        } else if (email.matches(EMAIL_PATTERN)) {
            return FIELD_STATUS.VALID;
        }

        return FIELD_STATUS.INVALID;
    }

    /**
     * Displays the appropriate error on the email field if the email is invalid
     *
     * @param edtEmail Field to be modified
     * @return true if email field is valid, otherwise false
     */
    private boolean isValidEmail(EditText edtEmail) {
        FIELD_STATUS status = validateEmail(edtEmail.getText().toString());

        switch (status) {
            case EMPTY:
                edtEmail.setError(getString(R.string.required_field));
                return false;
            case INVALID:
                edtEmail.setError(getString(R.string.email_invalid));
                return false;
            case VALID:
                return true;
            default:
                return false;
        }
    }

    /**
     * TODO change error messages to string resources
     * Validates fields and performs login
     *
     * @param view View from button click
     */
    public void login(View view) {
        boolean emailValid = isValidEmail(edtEmail);
        boolean pwdValid = isValidPassword(edtPaswd);

        if (!emailValid) {
            edtEmail.requestFocus();
        } else if (!pwdValid) {
            edtPaswd.requestFocus();
        }

        if (emailValid && pwdValid) {
            progressDialog.show();
            apiLogin();
        }
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
                    String err = (error.getMessage() == null) ? "An error occurred" : error.getMessage();
                    Log.d(TAG, err);
                    String errorMsg = err;

                    if (err.matches(AppConstants.CONNECTION_ERROR) || err.matches(AppConstants.TIMEOUT_ERROR)) {
                        errorMsg = getString(R.string.network_error_message);
                    } else if (err.matches(".*Received authentication challenge is null.*")) {
                        errorMsg = getString(R.string.invalid_login_message);
                    }

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
                    alertDialog.setTitle(getString(R.string.login_error_title));
                    alertDialog.setMessage(errorMsg).setCancelable(false)
                               .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
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
