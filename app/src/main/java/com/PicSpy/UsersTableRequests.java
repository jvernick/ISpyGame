package com.picspy;

import android.content.Context;
import android.util.Log;

import com.dreamfactory.api.DbApi;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.picspy.models.DbApiRequest;
import com.picspy.models.UsersRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by BrunelAmC on 8/7/2015.
 */
public class UsersTableRequests {
    private DbApi dbApi;
    private final static String TAG = "UsersTableRequests";

    public UsersTableRequests(Context context) {
        String session_id = PrefUtil.getString(context, AppConstants.SESSION_ID);
        dbApi = new DbApi();
        dbApi.addHeader("X-DreamFactory-Application-Name", AppConstants.APP_NAME);
        dbApi.addHeader("X-DreamFactory-Session-Token", session_id);
        dbApi.setBasePath(AppConstants.DSP_URL);
    }

    public String addUser(int id, String username) {
        try {
            AddUser request = new AddUser(id, username );
            UsersRecord record = dbApi.createRecord(UsersRecord.class, AppConstants.USERS_TABLE_NAME, "123", request, null, null, null, null);
            if ( record != null) {
                Log.d(TAG, record.toString());
                return "SUCCESS";
            } else {
                return "FAILED";
            }
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            //Attempting to return only the message part of the error message
            try {
                JSONObject jObj = new JSONObject(e.getMessage());
                JSONArray jArray = jObj.getJSONArray("error");
                JSONObject obj = jArray.getJSONObject(0);
                return obj.getString("message");
            } catch (JSONException ex) { //message is from exception
                //TODO customize message if an exception was thrown while getting messeage?
                //Currently ignoring JSONException and returning full error message
                return e.getMessage();
            }
        }
    }

    private class AddUser extends  DbApiRequest  {
        //user_id
        @JsonProperty
        private int id;
        //dispaly_name
        @JsonProperty
        private String username;

        public AddUser(int id, String username) {
            this.id = id;
            this.username = username;
        }

        @Override
        public String toString() {
            return "FriendRecord {" +
                    "\n" + "id: "  + id +
                    "\n" + " username: "  + username +
                    "\n}";
        }
    }
}
