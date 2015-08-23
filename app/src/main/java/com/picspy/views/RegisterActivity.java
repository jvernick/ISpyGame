package com.picspy.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.dreamfactory.client.ApiException;
import com.dreamfactory.client.ApiInvoker;
import com.dreamfactory.model.Register;
import com.dreamfactory.model.Session;
import com.picspy.firstapp.R;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

//TODO add all strings to string file

/**
 * User registration activity
 */
public class RegisterActivity extends Activity {
    public final static String EXTRA_MESSAGE = "com.picspy.firstapp.REGISTER_MESSAGE";
    private EditText display_name_Text, email_Text, pass1_Text, pass2_Text;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Create progress box for use during API connection
        progressDialog = new ProgressDialog(RegisterActivity.this);
        progressDialog.setMessage(getText(R.string.loading_message));
    }


	 //TODO: Limit password characters with regex?
    /**
     * Validates password to have: min length of 6 and
     * verifies that both entered passwords match.
     * @return "valid" if email is valid otherwise error message depending on problem
     */
    public String isValidPassword() {
        String pass1 = pass1_Text.getText().toString();
        String pass2 = pass2_Text.getText().toString();
        if (pass1.equals(pass2)) {
            if (pass1.length() >= 6){
                return "valid";
            } else {
                return "invalid_length";
            }
        }
        return "invalid_match";
    }

    /**
     * Validates email with regex
     * @return true if email is valid otherwise false
     */
    private boolean isValidEmail() {
        String emailPattern = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		String email = email_Text.getText().toString();
		return email.matches(emailPattern);
    }

    //TODO review: added for convinience: find a better way to store/handle this
    //stores variable so that new activity can be created in new method
    View post_view = null;
    //TODO Validate display_name
    public void signUp(View view) {
        display_name_Text = (EditText) findViewById(R.id.edit_display_name);
        email_Text = (EditText) findViewById(R.id.edit_email);
        pass1_Text = (EditText) findViewById(R.id.edit_password1);
        pass2_Text = (EditText) findViewById(R.id.edit_password2);
        post_view = view;

		//TODO try .matches("") instead of .length == 0
		//TODO add filter for valid characters?
        if (display_name_Text.getText().toString().trim().length() == 0) {
			display_name_Text.setError("Must enter Display Name");
		} else if (!isValidEmail()) {
            email_Text.setError("Invalid Email");
            email_Text.requestFocus();
        } else if (isValidPassword().equals("invalid_length")) {
            pass1_Text.setError("Must be at least 6 characters");
            pass1_Text.requestFocus();
            pass2_Text.setText("");
            pass2_Text.setText("");
        } else if (isValidPassword().equals("invalid_match")) {
            pass1_Text.setError("Passwords do not match");
            pass1_Text.requestFocus();
            pass2_Text.setText("");
            pass2_Text.setText("");
        } else {
            RegisterTask registerTask = new RegisterTask();
            registerTask.execute();
        }
    }

    /**
     * Starts the main activity after a user is succesfuly registered
     * @param view view from button click
     */
    private void showResults(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_MESSAGE, "Welcome!!\n Account successfuly created");
        startActivity(intent);
    }

    /* Class to run network transaction in background on a new thread. This is required*/
    private class RegisterTask extends AsyncTask<Void, Void, String> {
        @Override
        /* show the progress dialog (Please wait)*/
        protected void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return registerService();
            } catch (Exception e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String message) {
            progressDialog.cancel();
            //if request was successfull
           if (message.equals("SUCCESS")) { //call successful: build new intent
                //TODO: Update: Should go to main page
               showResults(post_view);
            } else {                        //handles and display error
               String errorMsg;
               try {
                   JSONObject jObj = new JSONObject(message);
                   JSONArray jArray = jObj.getJSONArray("error");
                   JSONObject obj = jArray.getJSONObject(0);
                   errorMsg = obj.getString("message");

                   //TODO Challenge!! match the displayname error with a regex
                   if (errorMsg.matches("^Invalid user name and password combination")) {
                       errorMsg = "Email already taken.";
                   } else if (errorMsg.trim().contains("Display Name")) {
					   errorMsg = "Display Name taken";
				   }
               } catch (JSONException e) { //message is from exception
                   //TODO customize message if an exception was thrown?
                   errorMsg = message;
               }
               AlertDialog.Builder alertDialog = new AlertDialog.Builder(RegisterActivity.this);
               //TODO modify error presentation format and possibly the error message
               alertDialog.setTitle("Error").setMessage(errorMsg).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.cancel();
                   }
               });
               alertDialog.show();
           }
        }



        /**
         * Method to call the register service
         * @return "SUCCESS" on success otherwise error message
         * @throws ApiException Throws ApiException when an Api error occurs
         */
        private String registerService() throws ApiException {
            ////////////////////ALways include/////////////////////////
            String appName = AppConstants.APP_NAME;
            String dsp_url = AppConstants.DSP_URL;
            ApiInvoker invoker  = new ApiInvoker();
            invoker.addDefaultHeader("X-DreamFactory-Application-Name", appName);

            // create path and map variables //SET accordingly
            String serviceName = "user";
            String endPoint = "register";
            String path = "/" + serviceName + "/" + endPoint + "/";

            // query params
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("login","true");
            Map<String, String> headerParams = new HashMap<>();
            String contentType = "application/json";

            Register register = new Register();
            register.setEmail(email_Text.getText().toString());
            register.setNew_password(pass1_Text.getText().toString());
            register.setDisplay_name(display_name_Text.getText().toString());
            /*Set other fields later in user settings/profile*/

            String response = invoker.invokeAPI(dsp_url, path, "POST", queryParams, register, headerParams, contentType);
            if(response != null){
                Session session = (Session) ApiInvoker.deserialize(response, "", Session.class);
                PrefUtil.putString(getApplicationContext(), AppConstants.SESSION_ID, session.getSession_id());
                PrefUtil.putInt(getApplicationContext(), AppConstants.USER_ID, Integer.parseInt(session.getId()));
                return "SUCCESS";
            }
            else {
                return "FAIlED: unknown error";
            }
        }

    }
}

