package com.picspy.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.picspy.models.GameRecord;
import com.picspy.models.GamesRecord;
import com.picspy.models.RecordsRequest;
import com.picspy.models.UserChallengesRecord;

import org.json.JSONException;
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


    /**
     * Creates game on server and stores challenge info.
     * Called after challenge picture has been successfully sent to server.
     * @param context Context from calling activity
     * @param listener response listener
     * @param errorListener error listener
     * @return A {@link ChallengesRequests} to add to request queue
     */
    public static ChallengesRequests createGame(Context context, GameRecord gameRecord,
                                   final Response.Listener<GameRecord> listener,
                                   Response.ErrorListener errorListener) {
        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                GamesRecord result = gson.fromJson(response.toString(), GamesRecord.class);
                listener.onResponse(result.getOnlyResource());
            }
        };

        try {
            RecordsRequest<GameRecord> gamesRecord = new RecordsRequest<>();
            gamesRecord.addResource(gameRecord);
            JSONObject jsonRequest;
            jsonRequest = new JSONObject(gson.toJson(gamesRecord, new TypeToken<RecordsRequest<GameRecord>>(){}.getType()));
            String url = DspUriBuilder.buildUri(DspUriBuilder.CHALLENGES_TABLE, null);

            return new ChallengesRequests(context, Method.POST, url, jsonRequest, jsonObjectListener, errorListener);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Gets all new available games.
     * @param listener Response listener
     * @param errorListener Error listener
     * @return A {@link ChallengesRequests} to add to request queue
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

        //TODO can limit number of returns using limit and then offset
        HashMap<String,String> params = new HashMap<>();
        String filter = "(user_id=" + PrefUtil.getInt(context, AppConstants.USER_ID) + ") AND (id>"
                + PrefUtil.getInt(context, AppConstants.MAX_USER_CHALLENGE_ID, 0) + ")";
        params.put("filter", filter);
        params.put("related", "challenges_by_challenge_id, users_by_sender");

        String url = DspUriBuilder.buildUri(DspUriBuilder.USER_CHALLENGES_TABLE, params);
        Log.d(TAG, "jsonRequest path: " + url);

        return  new ChallengesRequests(context, Method.GET, url, null, jsonObjectListener, errorListener);
    }

    /**
     * Gets leaderboard games from server.
     * TODO modify method to get WEEKLY/DAILY leaderboard instead of overall Leaderboard
     * //TOdO is context needed
     * @param listener Response listener
     * @param errorListener Error listener
     * @return A {@link ChallengesRequests} to add to request queue
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

        //TODO can limit number of returns using limit and then offset
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

    public static ChallengesRequests submitChallengeResult(Context context, int
            recordId, HashMap<String,String>
            params, final Response.Listener<UserChallengesRecord> listener, Response.ErrorListener
            errorListener) {

        Response.Listener<JSONObject> jsonObjectListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                UserChallengesRecord result = gson.fromJson(response.toString(), UserChallengesRecord.class);
                Log.d(TAG, "RecordResponse: " + result.toString());
                listener.onResponse(result);
            }
        };

        String url = DspUriBuilder.buildDeleteByIdUri(DspUriBuilder.USER_CHALLENGES_TABLE, recordId, params);

        return new ChallengesRequests(context, Method.DELETE, url, null, jsonObjectListener, errorListener);
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return AppConstants.dspHeaders(context);
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError){
        return VolleyRequest.parseNetworkError(volleyError);
    }

    /**
     * Class representing and storing server Table column names
     * STATIC, do not change
     */
    public static class GAME_LABEL {
        public static final String HINT = "hint";
        public static final String SELECTION = "selection";
        public static final String GUESSES = "guess";
        public static final String TIME = "time";
        public static final String LEADERBOARD = "leaderboard";
        public static final String FRIENDS = "friends";
        //extra for sendChallengeActivity
        public static final String FILE_NAME = "file_name";
        public static final String FILE_NAME_PATH = "file_path";
    }
}
