package com.picspy;

import android.content.Context;
import android.util.Log;

import com.dreamfactory.api.DbApi;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.picspy.models.DbApiRequest;
import com.picspy.models.UserRecord;
import com.picspy.models.UsersRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * two ways:
 * 1)from insert and delete scripts in the system user table,
 * 2)from calls to the friends table using related data
 * Assumes usernames will never be changed*/

 /**
 * This class provides methods for accessing the UsersTable
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

     /**
      * Searches for a user with the given username on the server
      * @param username username to be searched
      * @return UserRecord if user found, otherwise null
      */
     public UserRecord findUser(String username) {
         try {
             String filter =" `username` = \"" + username + " \"";
             UsersRecord record = dbApi.getRecordsByFilter(UsersRecord.class, AppConstants.USERS_TABLE_NAME, filter, null, null, null, null, false, false, null);
             if (record != null) {
                 Log.d(TAG, record.toString());
                 List<UserRecord> records= record.getResource();
                 if(records != null && records.size() == 1) {
                     return records.get(0);
                 }
             } else {
                 Log.d(TAG, "RecordNull");
                 return null;
             }
         } catch (Exception e) {
             Log.d(TAG, e.getMessage());
             //Attempting to return only the message part of the error message
             try {
                 JSONObject jObj = new JSONObject(e.getMessage());
                 JSONArray jArray = jObj.getJSONArray("error");
                 JSONObject obj = jArray.getJSONObject(0);
             } catch (JSONException ex) { //message is from exception
                 //Currently ignoring JSONException and returning full error message
             }
         }
         return null;
     }


     public String addUser(int id, String username) {
         try {
            AddUser request = new AddUser(id, username );
            UserRecord record = dbApi.createRecord(UserRecord.class,
                    AppConstants.USERS_TABLE_NAME, "123", request, null, null, null, null);
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

     private class Resource{

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
