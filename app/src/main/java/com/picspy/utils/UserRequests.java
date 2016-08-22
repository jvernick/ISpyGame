package com.picspy.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.picspy.models.RecordsRequest;
import com.picspy.models.UserRecord;
import com.picspy.models.UsersRecord;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides API for requesting User information
 */
public class UserRequests extends JsonObjectRequest {
    private static final String TAG = "UserReq";
    private static final Gson gson = new Gson();
    private Context context;
    private Type type;

    public enum Type {
        ADD, FIND, UPDATE
    }

    /**
     * Creates a new request.
     *
     * @param method             the HTTP method to use
     * @param path               URL to fetch the JSON from
     * @param jsonRequest        A {@link JSONObject} to post with the request. Null is allowed and
     *                           indicates no parameters will be posted along with request.
     * @param jsonObjectListener Listener to receive the JSON response
     * @param errorListener      Error listener, or null to ignore errors.
     */
    private UserRequests(Context context, Type type, int method, String path, JSONObject jsonRequest, Response.Listener<JSONObject> jsonObjectListener, Response.ErrorListener errorListener) {
        super(method, path, jsonRequest, jsonObjectListener, errorListener);
        Log.d(TAG, path + "\njson: " + jsonRequest);
        this.context = context;
        this.type = type;
    }

    /**
     * Adds the user to the user table on the server
     *
     * @param context       Calling activity context
     * @param userModel     Model containing data to post
     * @param listener      Response listener
     * @param errorListener Error listener
     * @return Returns an instance of a Request that is added to the request queue
     */
    public static UserRequests addUser(Context context, AddUserModel userModel,
                                       final Response.Listener<UserRecord> listener,
                                       Response.ErrorListener errorListener) {
        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "JSONResponse: " + response.toString());
                UsersRecord temp = gson.fromJson(response.toString(), UsersRecord.class);
                listener.onResponse(temp.getOnlyResource());
            }
        };

        RecordsRequest<AddUserModel> request = new RecordsRequest<>();
        request.addResource(userModel);

        JSONObject jsonRequest;
        try {
            jsonRequest = new JSONObject(gson.toJson(request, new TypeToken<RecordsRequest<AddUserModel>>() {
            }.getType()));

            String url = DspUriBuilder.buildUri(DspUriBuilder.USERS_TABLE, null);
            return new UserRequests(context, Type.ADD, Method.POST, url,
                    jsonRequest, jsonObjectListener, errorListener);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Adds the user to the user table on the server
     *
     * @param context       Calling activiyt context
     * @param userModel     Model containing data to post
     * @param listener      Response listener
     * @param errorListener Error listener
     * @return Returns an instance of a Request that is added to the request queue
     */
    public static UserRequests updateGcmReg(Context context, AddUserModel userModel,
                                            final Response.Listener<UserRecord> listener,
                                            Response.ErrorListener errorListener) {
        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "JSONResponse: " + response.toString());
                UsersRecord temp = gson.fromJson(response.toString(), UsersRecord.class);
                listener.onResponse(temp.getOnlyResource());
            }
        };

        RecordsRequest<AddUserModel> request = new RecordsRequest<>();
        request.addResource(userModel);

        JSONObject jsonRequest;
        try {
            jsonRequest = new JSONObject(gson.toJson(request, new TypeToken<RecordsRequest<AddUserModel>>() {
            }.getType()));

            String url = DspUriBuilder.buildUri(DspUriBuilder.USERS_TABLE, null);
            return new UserRequests(context, Type.UPDATE, Method.POST, url,
                    jsonRequest, jsonObjectListener, errorListener);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * TODO Test and replace find friend
     * Searches for a user on the server by username
     *
     * @param context       Calling activity context for getting headers
     * @param listener      Response listener
     * @param errorListener Error listener
     * @return Returns an instance of a Request that is added to the request queue
     */
    public static UserRequests findUser(Context context, String username, final Response.Listener<UserRecord> listener,
                                        Response.ErrorListener errorListener) {

        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "JSONResponse: " + response.toString());
                UsersRecord result = gson.fromJson(response.toString(), UsersRecord.class);
                listener.onResponse(result.getOnlyResource());
            }
        };

        HashMap<String, String> params = new HashMap<>();
        String filter = "username=" + username;
        params.put("filter", filter);

        String url = DspUriBuilder.buildUri(DspUriBuilder.USERS_TABLE, params);

        return new UserRequests(context, Type.FIND, Method.GET, url, null, jsonObjectListener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        {
            Map<String, String> temp = AppConstants.dspHeaders(context);
            if (type == Type.UPDATE) temp.put("X-HTTP-METHOD", "PATCH");
            return temp;
        }
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        return VolleyRequest.parseNetworkError(volleyError);
    }

    /**
     * Model to store user data that will be sent to and received from server
     */
    public static class AddUserModel {
        private Integer id;
        private String username;
        private String reg_token;

        public AddUserModel(int id, String username) {
            this.id = id;
            this.username = username;
        }

        public AddUserModel(Integer id, String username, String reg_token) {
            this.id = id;
            this.username = username;
            this.reg_token = reg_token;
        }

        public String getReg_token() {
            return reg_token;
        }

        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        @Override
        public String toString() {
            return "AddUserModel{" +
                    "\n" + "id=" + id +
                    "\n" + " username=" + username +
                    "\n" + "reg_token=" + reg_token +
                    "\n}";
        }
    }
}
