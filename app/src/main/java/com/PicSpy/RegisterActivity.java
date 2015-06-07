package com.picspy;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dreamfactory.client.ApiInvoker;
import com.picspy.Models.RegisterModel;
import com.picspy.firstapp.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends ActionBarActivity {

    public final static String EXTRA_MESSAGE = "amc.myfirstapp.REGISTER_MESSAGE";
    private EditText display_name_Text, email_Text, pass1_Text, pass2_Text;
    private String userID,userPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
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
    public String isValidPassword() {
        String pass1 = pass1_Text.getText().toString();
        String pass2 = pass2_Text.getText().toString();
        if (pass1.equals(pass2)) {
            if (pass1 != null && pass1.length() > 6){
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
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email_Text.getText().toString());
        return matcher.matches();
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

        if (!isValidEmail()) {
            email_Text.setError("Invalid Email");
            email_Text.requestFocus();
        } else if (isValidPassword().equals("invalid_length")) {
            pass1_Text.setError("Too Short");
            pass1_Text.requestFocus();
            pass2_Text.setText("");
            pass2_Text.setText("");
        } else if (isValidPassword().equals("invalid_match")) {
            pass1_Text.setError("Passwords Do Not Match");
            pass1_Text.requestFocus();
            pass2_Text.setText("");
            pass2_Text.setText("");
        } else {
            RegisterTask registerTask = new RegisterTask();
            registerTask.execute();

            Toast.makeText(view.getContext(), "You have created account successfully !!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //TODO: Overwrite this with needed activity
    /* Starts the next intent after user is register*/
    private void showResults(View view, String msg) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_MESSAGE, msg);
        startActivity(intent);
    }

    /* Class to run network transaction in background on a new thread. This is required*/
    class RegisterTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            String response;
            try {
                response = registerService();
            } catch (Exception e) {
                response = e.getMessage();
                //error = e.toString();
            }
            return response;
        }

        @Override
        /* Calls method to create new activity that displays registration response
         * TODO: overwite to open different activities based on the result
         */
        protected void onPostExecute(String message) {
            showResults(post_view, message);
        }

        /* Method to call the register service*/
        private String registerService() throws Exception {
            ////////////////////ALways include/////////////////////////
            //TODO store %appname% and %dsp_url% as a global permanent variable
            String appName = "admin";
            String dsp_url = "http://192.168.0.26:8080";
            ApiInvoker invoker  = new ApiInvoker();
            invoker.addDefaultHeader("X-DreamFactory-Application-Name", appName);
            // create path and map variables //SET accordingly
            String serviceName = "user";
            String endPoint = "register";
            String path = new StringBuilder("/").append("rest").append("/").append(serviceName).append("/").append(endPoint).append("/").toString();
            // query params
            Map<String, String> queryParams = new HashMap<String, String>();
            queryParams.put("login","false");
            Map<String, String> headerParams = new HashMap<String, String>();
            String contentType = "application/json";
            ////////////////////////////////////////////////////////////

            RegisterModel register = new RegisterModel();
            register.setEmail(email_Text.getText().toString());
            register.setNew_password(pass1_Text.getText().toString());
            register.setDisplay_name(display_name_Text.getText().toString());
        /*Set other fields later*/

            String response = invoker.invokeAPI(dsp_url, path, "POST", queryParams, register, headerParams, contentType);
            //TODO can create class to ocnvert response into class model

            JSONObject object = new JSONObject(response);

            return object.getString("success");
        }
    }
}

