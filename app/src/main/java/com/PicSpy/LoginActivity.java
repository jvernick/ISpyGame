package com.picspy;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dreamfactory.api.UserApi;
import com.dreamfactory.client.ApiException;
import com.dreamfactory.model.Login;
import com.dreamfactory.model.Session;
import com.picspy.firstapp.R;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;

public class LoginActivity extends Activity {
    private EditText  email_text, pass_text;
    private Button login_button;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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

    private  void checkFieldsForEmptyValues(){
        String s1 = email_text.getText().toString();
        String s2 = pass_text.getText().toString();

        if (s1.equals("") || s2.equals("")) { //disables and greys out the button
            login_button.setEnabled(false);
            login_button.getBackground().setColorFilter(0xff888888, PorterDuff.Mode.MULTIPLY);
        } else { //enables and ungreys out the button
            login_button.setEnabled(true);
            login_button.getBackground().clearColorFilter();
        }
    }

    public void login(View view) {
        LoginTask loginTask = new LoginTask();
        loginTask.execute();
    }

    /* Class to run network transaction in background on a new thread. This is required*/
    private class LoginTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                String session_id = loginSercice();
                PrefUtil.putString(getApplicationContext(), AppConstants.SESSION_ID, session_id);
            } catch (ApiException e) {
                return e.getMessage();
            }
            return null;
        }

        @Override
        /* Calls method to create new activity that displays registration response
         * TODO: overwite to open different activities based on the result
         */
        protected void onPostExecute(String message) {
            Log.d("login", "network done");
            progressDialog.cancel();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(LoginActivity.this);
                //TODO modify error presentation format and possibly the error message
                alertDialog.setTitle("Message").setMessage(message).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
            alertDialog.show();
        }

        @Override
        protected void onPreExecute() {
            progressDialog.show();
        }


        private String loginSercice() throws ApiException {
            UserApi userApi = new UserApi();
            userApi.addHeader("X-DreamFactory-Application-Name", AppConstants.APP_NAME);
            Login login = new Login();
            login.setEmail(email_text.getText().toString());
            login.setPassword(pass_text.getText().toString());
            Session session = userApi.login(login);
            return session.getSession_id();
        }
    }
}
