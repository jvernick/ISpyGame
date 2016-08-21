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
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.picspy.firstapp.R;
import com.picspy.models.UserRecord;
import com.picspy.utils.Accounts;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;
import com.picspy.utils.RegistrationRequests;
import com.picspy.utils.RegistrationRequests.RegisterApiResponse;
import com.picspy.utils.RegistrationRequests.RegisterModel;
import com.picspy.utils.UserRequests;
import com.picspy.utils.VolleyRequest;

import java.util.Calendar;

import static com.picspy.views.LoginActivity.FIELD_STATUS;
import static com.picspy.views.LoginActivity.validateEmail;
import static com.picspy.views.LoginActivity.validatePassword;
import static com.picspy.views.Splash_Activity.verifyFCMToken;


/**
 * User registration activity
 */
public class RegisterActivity extends ActionBarActivity {
    private static final String TAG = "RegisterActivity";
    private static final Object CANCEL_TAG = "registerUser";
    private EditText edtDisplayName;
    private EditText edtEmail;
    private EditText edtPaswd;
    private Button btnRegister;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Create progress box for use during API connection
        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setMessage(getText(R.string.login_loading));

        edtDisplayName = (EditText) findViewById(R.id.edit_display_name);
        edtEmail = (EditText) findViewById(R.id.edit_email);
        edtPaswd = (EditText) findViewById(R.id.edit_password);
        btnRegister = (Button) findViewById(R.id.btn_signup);

        edtPaswd.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    btnRegister.performClick();
                    return true;
                }
                return false;
            }
        });

        // Setting toolbar as the ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.tx_signup));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_chevron_left_black);

        edtDisplayName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                checkFieldsForEmptyValues();
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
        String s2 = edtDisplayName.getText().toString();
        String s3 = edtPaswd.getText().toString();

        if (s1.equals("") || s2.equals("") || s3.equals("")) {   //disables the button
            btnRegister.setEnabled(false);
        } else {                                //enables the button
            btnRegister.setEnabled(true);
        }
    }

    /**
     * Displays the appropriate error on the password field if the password is valid
     *
     * @param edtPaswd Field to be modified
     * @return true if password field is valid, otherwise false
     */
    public boolean isValidPassword(EditText edtPaswd) {
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
     * Displays the appropriate error on the email field if the email is invalid
     *
     * @param edtEmail Field to be modified
     * @return true if email field is valid, false otherwise
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
     * Displays the appropriate error on the username field if the username is invalid
     *
     * @param edtDisplayName UsernameField
     * @return true if username is valid, false otherwise
     */
    private boolean isValidUsername(EditText edtDisplayName) {
        boolean isValid = edtDisplayName.getText().length() != 0;

        if (!isValid) {
            edtDisplayName.setError(getString(R.string.required_field));
        }

        return isValid;
    }

    /**
     * Validates entered fields and starts the Sign Up process
     *
     * @param view Button view
     */
    public void signUp(View view) {
        boolean emailValid = isValidEmail(edtEmail);
        boolean pwdValid = isValidPassword(edtPaswd);
        boolean unameValid = isValidUsername(edtDisplayName);

        if (!unameValid) {
            edtDisplayName.requestFocus();
        } else if (!emailValid) {
            edtEmail.requestFocus();
        } else if (!pwdValid) {
            edtPaswd.requestFocus();
        }

        if (emailValid && pwdValid) {
            register();
        }
    }

    /**
     * 1st step of registration process. Registers the user, and attempts login.
     */
    private void register() {
        RegisterModel registerModel = new RegisterModel(edtEmail.getText().toString(),
                edtPaswd.getText().toString(),
                edtDisplayName.getText().toString());

        Response.Listener<RegisterApiResponse> responseListener = new Response.Listener<RegisterApiResponse>() {
            @Override
            public void onResponse(RegisterApiResponse response) {
                if (response.isSuccess()) login();
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.cancel();
                if (error != null) {
                    String errorMsg = "An error occurred";
                    String err = (error.getMessage() == null) ? errorMsg : error.getMessage();
                    Log.d(TAG, err);
                    error.printStackTrace();

                    if (err.matches(AppConstants.CONNECTION_ERROR) || err.matches(AppConstants.TIMEOUT_ERROR)) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegisterActivity.this);
                        alertDialog.setTitle(getString(R.string.signup_error_title));
                        alertDialog.setMessage(getString(R.string.network_error_message)).setCancelable(false)
                                   .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                           dialog.cancel();
                                       }
                                   });
                        alertDialog.show();
                    } else if (err.matches(".*Duplicate entry .* for key 'name_UNIQUE'.*")) {
                        edtDisplayName.setError("username taken");
                        edtDisplayName.requestFocus();
                    } else if (err.matches(".*The email has already been taken.*")) {
                        edtEmail.setError("email taken");
                        edtEmail.requestFocus();
                    } else {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegisterActivity.this);
                        alertDialog.setTitle(getString(R.string.signup_error_title));
                        alertDialog.setMessage(getString(R.string.network_error_message)).setCancelable(false)
                                   .setPositiveButton(getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
                                       @Override
                                       public void onClick(DialogInterface dialog, int which) {
                                           dialog.cancel();
                                       }
                                   });
                        alertDialog.show();
                    }
                }
            }
        };


        progressDialog.setMessage("Registering..");
        RegistrationRequests registerRequest =
                RegistrationRequests.register(this, registerModel, responseListener, errorListener);
        VolleyRequest.getInstance(this.getApplicationContext()).addToRequestQueue(registerRequest);
        progressDialog.show();
    }

    /**
     * 2nd step in user registration. Login the user and attempt user record creation on success
     */
    private void login() {
        RegistrationRequests.LoginModel loginModel = new RegistrationRequests.LoginModel(
                edtEmail.getText().toString(), edtPaswd.getText().toString(), true);

        final Context context = this;
        Response.Listener<RegistrationRequests.LoginApiResponse> responseListener = new Response.Listener<RegistrationRequests.LoginApiResponse>() {
            @Override
            public void onResponse(RegistrationRequests.LoginApiResponse response) {
                Log.d(TAG, "login: " + response.toString());
                if (response.getId() != 0) {
                    Accounts.checkNewAccount(context, response.getId());
                    //stores JWT token
                    PrefUtil.putString(context, AppConstants.SESSION_TOKEN, response.getSessionToken());
                    PrefUtil.putInt(context, AppConstants.USER_ID, response.getId());
                    PrefUtil.putString(context, AppConstants.USER_NAME, response.getUsername());
                    PrefUtil.putLong(context, AppConstants.LAST_LOGIN_DATE, Calendar.getInstance().getTimeInMillis());
                    addToUsers(response.getId(), edtDisplayName.getText().toString());
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.cancel();
                if (error != null) {
                    String err = (error.getMessage() == null) ? "error message null" : error.getMessage();
                    Log.d(TAG, err);
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegisterActivity.this);
                    alertDialog.setTitle(getString(R.string.signup_error_title));
                    alertDialog.setMessage(getString(R.string.network_error_message)).setCancelable(false)
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

        progressDialog.setMessage("Logging in..");
        RegistrationRequests loginRequest =
                RegistrationRequests.login(this, loginModel, responseListener, errorListener);
        if (loginRequest != null) loginRequest.setTag(CANCEL_TAG);
        VolleyRequest.getInstance(this.getApplicationContext()).addToRequestQueue(loginRequest);
    }

    /**
     * Third and final step of user creation. Stores the user username in the users table.
     *
     * @param id       The current user's id
     * @param username The current user's username synonymous with display_name
     */
    private void addToUsers(final int id, String username) {
        UserRequests.AddUserModel addUserModel = new UserRequests.AddUserModel(id, username);

        Response.Listener<UserRecord> responseListener = new Response.Listener<UserRecord>() {
            @Override
            public void onResponse(UserRecord response) {
                progressDialog.cancel();
                if (response != null && response.getId() == id) {
                    showResults();
                } else {
                    Log.d(TAG, "Error");
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.cancel();
                if (error != null) {
                    String err = (error.getMessage() == null) ? "error message null" : error.getMessage();
                    Log.d(TAG, err);
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegisterActivity.this);
                    alertDialog.setTitle(getString(R.string.signup_error_title));
                    alertDialog.setMessage(getString(R.string.network_error_message)).setCancelable(false)
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

        progressDialog.setMessage("Adding user..");
        UserRequests addUserRequest = UserRequests.addUser(this,
                addUserModel, responseListener, errorListener);
        if (addUserRequest != null) addUserRequest.setTag(CANCEL_TAG);
        VolleyRequest.getInstance(this.getApplicationContext()).addToRequestQueue(addUserRequest);
    }

    /**
     * Starts the main activity after a user is successfully registered
     */
    private void showResults() {
        verifyFCMToken(this);
        progressDialog.cancel();
        Toast.makeText(RegisterActivity.this, "Welcome!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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

