package com.picspy.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.picspy.models.FriendRecord;
import com.picspy.models.FriendsRecord;
import com.picspy.models.RecordsRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by BrunelAmC on 1/19/2016.
 */
public class FriendsRequests extends JsonObjectRequest{

    private static final String TAG = "FriendsReq";
    private static Gson gson = new Gson();
    private Context context;
    private boolean isPatchTunnel = false;

    /**
     * Creates a new request.
     *
     * @param isPatchTunnel This defines an post tunnel for a patch request
     * @param method        the HTTP method to use
     * @param url           URL to fetch the JSON from
     * @param jsonRequest   A {@link JSONObject} to post with the request. Null is allowed and
*                      indicates no parameters will be posted along with request.
     * @param listener      Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    private FriendsRequests(Context context, boolean isPatchTunnel, int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        Log.d(TAG, url + "\nJson: " + jsonRequest);
        this.context = context;
        this.isPatchTunnel = isPatchTunnel;
    }

    public static FriendsRequests getFriends(Context context,int maxFriendRecordId, final Response.Listener<FriendsRecord> listener, Response.ErrorListener errorListener) {
        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                FriendsRecord result = gson.fromJson(response.toString(), FriendsRecord.class);
                Log.d(TAG, "RecordsResponse" + result.toString());
                listener.onResponse(result);
            }
        };

        //TODO and server-side limit to only friends
        HashMap<String,String> params = new HashMap<>();
        String filter ="(status=" + 0 + ") AND (id> " + maxFriendRecordId  +")";
        params.put("filter", filter);
        params.put("related", "*");
        String url = DspUriBuilder.buildUri(DspUriBuilder.FRIENDS_TABLE, params);

        return new FriendsRequests(context, false, Method.GET, url, null, jsonObjectListener, errorListener);
    }

    public static FriendsRequests sendFriendRequest(Context context,int friend_2_id, final Response.Listener<FriendRecord> listener, Response.ErrorListener errorListener) {
        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                FriendsRecord result = gson.fromJson(response.toString(), FriendsRecord.class);
                Log.d(TAG, "RecordsResponse" + result.toString());
                listener.onResponse(result.getOnlyResource());
            }
        };

        int userId = PrefUtil.getInt(context, AppConstants.USER_ID);
        RecordsRequest<FriendModel> request = new RecordsRequest<>();
        request.addResource(new FriendModel(friend_2_id, userId, userId));

        JSONObject jsonRequest;
        try {
            jsonRequest = new JSONObject(gson.toJson(request, new TypeToken<RecordsRequest<FriendModel>>(){}.getType()));
            HashMap<String, String> params = new HashMap<>();
            // TODO may not be needed. Username obtained from display_name
            params.put("uname", PrefUtil.getString(context, AppConstants.USER_NAME));
            String url = DspUriBuilder.buildUri(DspUriBuilder.FRIENDS_TABLE, params);

            return new FriendsRequests(context, false, Method.POST, url, jsonRequest, jsonObjectListener, errorListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static FriendsRequests removeFriend(Context context, int friend_2_id, final Response.Listener<FriendRecord> listener, Response.ErrorListener errorListener) {
        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                FriendsRecord result = gson.fromJson(response.toString(), FriendsRecord.class);
                listener.onResponse(result.getOnlyResource());
            }
        };

        int userId = PrefUtil.getInt(context, AppConstants.USER_ID);
        RecordsRequest<FriendModel> request = new RecordsRequest<>();
        request.addResource(new FriendModel(friend_2_id, userId, null));

        HashMap<String,String> params = new HashMap<>();
        String filter;
        if (friend_2_id < userId) {
            filter = "(friend_1=" + friend_2_id + ") AND (friend_2=" + userId + ")";
        } else  {
            filter = "(friend_1=" + userId + ") AND (friend_2=" + friend_2_id + ")";
        }
        params.put("filter", filter);
        String url = DspUriBuilder.buildUri(DspUriBuilder.FRIENDS_TABLE, params);

        return new FriendsRequests(context, false, Method.DELETE, url, null, jsonObjectListener, errorListener);
    }

    public static FriendsRequests getFriendRequests ( Context context, final Response.Listener<FriendsRecord> listener, Response.ErrorListener errorListener) {
        Log.d(TAG, "getFriendRequests");
        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                FriendsRecord result = gson.fromJson(response.toString(), FriendsRecord.class);
                listener.onResponse(result);
            }
        };

        HashMap<String,String> params = new HashMap<>();
        String filter ="(status!=" + PrefUtil.getInt(context, AppConstants.USER_ID, 8) + ") AND (status!=" + 0 + ")" ;
        params.put("filter", filter);
        params.put("related", "*");
        String url = DspUriBuilder.buildUri(DspUriBuilder.FRIENDS_TABLE, params);

        return new FriendsRequests(context, false, Method.GET, url, null, jsonObjectListener, errorListener);
    }

    /** ToDO combine sendRequest, acceptRequest and removeFriend into one method)*/
    public static FriendsRequests acceptFriendRequest(Context context, int friend_id, final Response.Listener<FriendRecord> listener, Response.ErrorListener errorListener) {
        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                FriendsRecord result = gson.fromJson(response.toString(), FriendsRecord.class);
                listener.onResponse(result.getOnlyResource());
            }
        };

        int userId = PrefUtil.getInt(context, AppConstants.USER_ID);
        RecordsRequest<FriendModel> request = new RecordsRequest<>();
        request.addResource(new FriendModel(friend_id, userId, 0));


        JSONObject jsonRequest;
        try {
            jsonRequest = new JSONObject(gson.toJson(request, new TypeToken<RecordsRequest<FriendModel>>(){}.getType()));
            String url = DspUriBuilder.buildUri(DspUriBuilder.FRIENDS_TABLE, null);

            return new FriendsRequests(context, true, Method.POST, url, jsonRequest, jsonObjectListener, errorListener);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static FriendsRequests getStats(Context context, int friend_id, final Response.Listener<FriendRecord> listener, Response.ErrorListener errorListener) {
        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                FriendsRecord result = gson.fromJson(response.toString(), FriendsRecord.class);
                listener.onResponse(result.getOnlyResource());
            }
        };

        int userId = PrefUtil.getInt(context, AppConstants.USER_ID);
        RecordsRequest<FriendModel> request = new RecordsRequest<>();
        request.addResource(new FriendModel(friend_id, userId, 0));

        String filter;
        String related;
        int user_id = PrefUtil.getInt(context, AppConstants.USER_ID);
        if (user_id < friend_id) {
            related = "users_by_friend_2";
            filter = "(friend_1=" + user_id + ") AND (friend_2=" + friend_id + ")";
        } else {
            related = "users_by_friend_1";
            filter = "(friend_1=" + friend_id + ") AND (friend_2=" + user_id + ")";
        }

        HashMap<String,String> params = new HashMap<>();
        params.put("filter", filter);
        params.put("related", related);
        String url = DspUriBuilder.buildUri(DspUriBuilder.FRIENDS_TABLE, params);

        return new FriendsRequests(context, false, Method.GET, url, null, jsonObjectListener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> temp = AppConstants.dspHeaders(context);
        if (isPatchTunnel) temp.put("X-HTTP-METHOD", "PATCH");
        return temp;
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError){
        return VolleyRequest.parseNetworkError(volleyError);
    }


    /**
     * Model for posting data to server. Used for sending friend requests and removing friends.
     */
    private static class FriendModel {
        private Integer friend_1;
        private Integer friend_2;
        private Integer status;

        /**
         * Creates a new record in the table and that represents a friend request
         * @param friend_1 Current userID
         * @param friend_2 Friend_2 id
         * @param status  The current user's user_id
         */
        public FriendModel(int friend_1, int friend_2, Integer status) {
            if (friend_1 < friend_2) {
                this.friend_1 = friend_1;
                this.friend_2 = friend_2;
            } else {

                this.friend_1 = friend_2;
                this.friend_2 = friend_1;
            }
            this.status = status;
        }

        @Override
        public String toString() {
            return "FriendModel{" +
                    "friend_1=" + friend_1 +
                    ", friend_2=" + friend_2 +
                    ", status=" + status +
                    '}';
        }
    }
}
