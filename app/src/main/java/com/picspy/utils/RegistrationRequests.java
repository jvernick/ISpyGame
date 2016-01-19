package com.picspy.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by BrunelAmC on 1/13/2016.
 */
public class RegistrationRequests extends JsonObjectRequest {
    private static final String TAG = "RegistrationReq";
    private static final Gson gson = new Gson();
    private Context context;

    public enum Type {LOGIN, REGISTER, REFREH}
    private Type type;

    /**
     * Creates a new request.
     *
     * @param context       the application context
     * @param method        the HTTP method to use
     * @param url           URL to fetch the JSON from
     * @param jsonRequest   A {@link JSONArray} to post with the request. Null is allowed and
     *                      indicates no parameters will be posted along with request.
     * @param listener      Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public RegistrationRequests(Context context, Type type, int method, String url,
                                JSONObject jsonRequest, Response.Listener<JSONObject> listener,
                                Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.context = context;
        this.type = type;
    }

    public static RegistrationRequests register(Context context,
                                                final RegisterModel request,
                                                final Response.Listener<RegisterApiResponse> listener,
                                                Response.ErrorListener errorListener)  {

        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                listener.onResponse(gson.fromJson(response.toString(), RegisterApiResponse.class));
            }
        };

        JSONObject jsonRequest;
        try {
            jsonRequest = new JSONObject(gson.toJson(request, RegisterModel.class));
            String path = AppConstants.DSP_URL_2 + "user/register";
            return  new RegistrationRequests(context, Type.REGISTER, Method.POST, path,
                    jsonRequest, jsonObjectListener, errorListener);
        } catch (JSONException e) {
            e.printStackTrace();
            return  null;
        }

    }

    public static RegistrationRequests login(Context context,
                                                final LoginModel request,
                                                final Response.Listener<LoginApiResponse> listener,
                                                Response.ErrorListener errorListener)  {

        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                listener.onResponse(gson.fromJson(response.toString(), LoginApiResponse.class));
            }
        };

        JSONObject jsonRequest;
        try {
            jsonRequest = new JSONObject(gson.toJson(request, LoginModel.class));
            String path = AppConstants.DSP_URL_2 + "user/session";
            return  new RegistrationRequests(context, Type.LOGIN, Method.POST, path,
                    jsonRequest, jsonObjectListener, errorListener);
        } catch (JSONException e) {
            e.printStackTrace();
            return  null;
        }

    }

    public static RegistrationRequests refresh (Context context,
                                             final LoginModel request,
                                             final Response.Listener<LoginApiResponse> listener,
                                             Response.ErrorListener errorListener)  {

        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                listener.onResponse(gson.fromJson(response.toString(), LoginApiResponse.class));
            }
        };

        JSONObject jsonRequest;
        try {
            jsonRequest = new JSONObject(gson.toJson(request, LoginModel.class));
            String path = AppConstants.DSP_URL_2 +"user/session";
            return  new RegistrationRequests(context, Type.REFREH, Method.PUT, path,
                    jsonRequest, jsonObjectListener, errorListener);
        } catch (JSONException e) {
            e.printStackTrace();
            return  null;
        }

    }

    /**
     * Returns a list of extra HTTP headers to go along with this request. Can
     * throw {@link AuthFailureError} as authentication may be required to
     * provide these values.
     *
     * @throws AuthFailureError In the event of auth failure
     */
    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> temp = new HashMap<>(AppConstants.dspHeaders(context));
        if (type == Type.REFREH) temp.put("X-DreamFactory-Session-Token",
                PrefUtil.getString(context, AppConstants.SESSION_TOKEN));
        return  temp;
    }


    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError){
        return VolleyRequest.parseNetworkError(volleyError);
    }

    public static class RegisterModel{
        private String email;
        private String new_password;
        private String name;

        public RegisterModel() {
        }

        public RegisterModel(String email, String password, String username) {
            this.email = email;
            this.new_password = password;
            this.name = username;

        }

        @Override
        public String toString() {
            return "RegisterModel{" +
                    "email='" + email + '\'' +
                    ", new_password='" + new_password + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public static class RegisterApiResponse implements ApiResponse {
        private boolean success;

        public RegisterApiResponse() {
        }

        public boolean isSuccess() {
            return success;
        }

        @Override
        public String toString() {
            return "RegisterApiResponse{" +
                    "success=" + success +
                    '}';
        }
    }


    public static class LoginModel {
        private String email;
        private String password;
        private boolean remember_me;

        public LoginModel() {
        }

        public LoginModel(String email, String password, boolean remember_me) {
            this.email = email;
            this.password = password;
            this.remember_me = remember_me;
        }

        @Override
        public String toString() {
            return "LoginModel{" +
                    "email='" + email + '\'' +
                    ", password='" + password + '\'' +
                    ", remember_me=" + remember_me +
                    '}';
        }
    }

    public static class LoginApiResponse {
        private Integer id;
        private String name;
        private String session_token;

        public LoginApiResponse() {
        }

        public int getId() {
            return id;
        }

        public String getUsername() {
            return name;
        }

        public String getSessionToken() {
            return session_token;
        }

        @Override
        public String toString() {
            return "LoginApiResponse{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", session_token='" + session_token + '\'' +
                    '}';
        }
    }
}

