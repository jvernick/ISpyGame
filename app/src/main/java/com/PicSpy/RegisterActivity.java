package com.picspy;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.dreamfactory.client.ApiInvoker;
import com.dreamfactory.model.Register;
import com.picspy.firstapp.R;
import com.picspy.utils.AppConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO add all strings to string file
public class RegisterActivity extends ActionBarActivity {

    public final static String EXTRA_MESSAGE = "amc.myfirstapp.REGISTER_MESSAGE";
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Validates password to have: minlength of 8 and
        verifies that both entered passwords match.
     */
	 //TODO: Limit characters with regex?
    public String isValidPassword() {
        String pass1 = pass1_Text.getText().toString();
        String pass2 = pass2_Text.getText().toString();
        if (pass1.equals(pass2)) {
            if (pass1 != null && pass1.length() >= 6){
                return "valid";
            } else {
                return "invalid_length";
            }
        }
        return "invalid_match";
    }

    /* Validates email with regex */
    private boolean isValidEmail() {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
		/* trying new stuff below		
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email_Text.getText().toString());
        return matcher.matches();*/
		String email = email_Text.getText().toString();
		return email.matches(EMAIL_PATTERN);
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

    //TODO: Overwrite this with needed activity
    /* Starts the next intent after user is register*/
    private void showResults(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_MESSAGE, "Welcome!!\n Account successfuly created");
        startActivity(intent);
    }

    /* Class to run network transaction in background on a new thread. This is required*/
    private class RegisterTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String response;
            try {
                response = registerService();
            } catch (Exception e) {
                response = e.getMessage();
            }
            return response;
        }

        @Override
        /* Calls method to create new activity that displays registration response
         * TODO: overwite to open different activities based on the result
         */
        protected void onPostExecute(String message) {
            progressDialog.cancel();
            //if request was successfull
           if (message.equals("true")) { //call successful: build new intent
                //TODO: Update: Should go to main page
               showResults(post_view);
            } else {
               String errorMsg = "";
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

        @Override
        /* show the progress dialog (Please wait)*/
        protected void onPreExecute() {
            progressDialog.show();
        }

        /* Method to call the register service*/
        private String registerService() throws Exception {
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
            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("login","false");
            Map<String, String> headerParams = new HashMap<String, String>();
            String contentType = "application/json";
            ////////////////////////////////////////////////////////////

            Register register = new Register();
            register.setEmail(email_Text.getText().toString());
            register.setNew_password(pass1_Text.getText().toString());
            register.setDisplay_name(display_name_Text.getText().toString());
            /*Set other fields later*/

            String response = invoker.invokeAPI(dsp_url, path, "POST", queryParams, register, headerParams, contentType);
            //TODO can create class to ocnvert response into class model
            //TODO line below throws an exception that is unhandled
            JSONObject object = new JSONObject(response);

            return object.getString("success");
        }

    }
}

