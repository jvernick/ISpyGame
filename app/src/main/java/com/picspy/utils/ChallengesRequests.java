package com.picspy.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.picspy.models.Game;
import com.picspy.models.GameRecord;
import com.picspy.models.GamesRecord;
import com.picspy.models.UserChallengeRecord;
import com.picspy.models.UserChallengesRecord;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by BrunelAmC on 1/17/2016.
 */
public class ChallengesRequests  extends JsonObjectRequest{

    private static final int LEADERBOARD_LIMIT = 12;
    private static final String TAG = "ChallengesReq";
    private static final Gson gson = new Gson();
    private Context context;

    /**
     * Creates a new request.
     *
     * @param method        the HTTP method to use
     * @param path           URL to fetch the JSON from
     * @param jsonRequest   A {@link JSONObject} to post with the request. Null is allowed and
     *                      indicates no parameters will be posted along with request.
     * @param listener      Listener to receive the JSON response
     * @param errorListener Error listener, or null to ignore errors.
     */
    public ChallengesRequests(Context context, int method, String path, JSONObject jsonRequest,
                              Response.Listener<JSONObject> listener,
                              Response.ErrorListener errorListener) {
        super(method, path, jsonRequest, listener, errorListener);
        this.context = context;
    }


    public static ChallengesRequests createGame(Context context,
                                   final Response.Listener<UserChallengeRecord> listener,
                                   Response.ErrorListener errorListener) {
        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "JSONresponse: " + response.toString());
                UserChallengesRecord result = gson.fromJson(response.toString(), UserChallengesRecord.class);
                Log.d(TAG, "RecordsResponse" + result.toString());
                listener.onResponse(result.getOnlyResource());
            }
        };



        String url = DspUriBuilder.buildUri(DspUriBuilder.USER_CHALLEGES_TABLE, null);
        Log.d(TAG, "jsonRequest path: " + url);

        return  new ChallengesRequests(context, Method.POST, url, null, jsonObjectListener, errorListener);
    }

    /**
     * Gets all new available games.
     * @param listener Response listener
     * @param errorListener Error listener
     * @return  A list fof all new pending games
     */
    public static ChallengesRequests getGamesInfo(Context context,
                             final Response.Listener<UserChallengesRecord> listener,
                             Response.ErrorListener errorListener) {

        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "JSONresponse: " + response.toString());
                UserChallengesRecord result = gson.fromJson(response.toString(), UserChallengesRecord.class);
                Log.d(TAG, "RecordsResponse" + result.toString());
                listener.onResponse(result);
            }
        };

        //TODO can limit number of returns using limit and then ofsset
        HashMap<String,String> params = new HashMap<>();
        String filter = "(user_id=" + PrefUtil.getInt(context, AppConstants.USER_ID) + ") AND (id>"
                + PrefUtil.getInt(context, AppConstants.MAX_USER_CHALLENGE_ID, 0) + ")";
        params.put("filter", filter);
        params.put("related", "challenges_by_challenge_id");

        String url = DspUriBuilder.buildUri(DspUriBuilder.USER_CHALLEGES_TABLE, params);
        Log.d(TAG, "jsonRequest path: " + url);

        return  new ChallengesRequests(context, Method.GET, url, null, jsonObjectListener, errorListener);
    }

    /**
     * Gets leaderboard games from server.
     * TODO modify method to get WEEKLY/DAILY leaderboard instead of overall Leaderboard
     * //TOdO is context needed
     * @param listener Response listener
     * @param errorListener Error listener
     * @return  A record containing leaderboard games
     */
    public static ChallengesRequests getleaderboard(Context context, final Response.Listener<GamesRecord> listener,
                                                    Response.ErrorListener errorListener) {

        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                GamesRecord result = gson.fromJson(response.toString(), GamesRecord.class);
                Log.d(TAG, "RecordsResponse" + result.toString());
                listener.onResponse(result);
            }
        };

        //TODO can limit number of returns using limit and then ofsset
        HashMap<String,String> params = new HashMap<>();
        params.put("filter", "leaderboard=true");
        params.put("related", "users_by_sender");
        //TODo test order; and  add Limit
        params.put("order", "votes desc");
        params.put("limit", String.valueOf(LEADERBOARD_LIMIT));

        String url = DspUriBuilder.buildUri(DspUriBuilder.CHALLENGES_TABLE, params);
        Log.d(TAG, "jsonRequest path: " + url);

        return  new ChallengesRequests(context, Method.GET, url, null, jsonObjectListener, errorListener);
    }

    /**
     * gets game from server
     */
    public static void getGame() {

    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return AppConstants.dspHeaders(context);
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError){
        return VolleyRequest.parseNetworkError(volleyError);
    }
}
