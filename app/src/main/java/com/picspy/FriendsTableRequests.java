package com.picspy;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.dreamfactory.api.DbApi;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.picspy.models.DbApiRequest;
import com.picspy.models.FriendRecord;
import com.picspy.models.FriendsRecord;
import com.picspy.models.StoredProcRequest;
import com.picspy.models.StoredProcResponse;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides methods to access the friends database for CRUD operations. All methods within this
 * class must be called from an AsyncTask, since they do network access
 * Created by BrunelAmC on 7/15/2015.
 */
public class FriendsTableRequests {
    private int user_id;
    private String session_id;
    private DbApi dbApi;
    private final static String TAG = "FriendsTableRequests";

    /**
     * Constructor initializes the required settings and values from shared preferences
     * @param context The contexts that calls the method. This is needed to access the shared
     *  preferences
     */
    public FriendsTableRequests(Context context) {
        user_id = PrefUtil.getInt(context, AppConstants.USER_ID);
        session_id = PrefUtil.getString(context, AppConstants.SESSION_ID);
        dbApi = new DbApi();
        dbApi.addHeader("X-DreamFactory-Application-Name", AppConstants.APP_NAME);
        dbApi.addHeader("X-DreamFactory-Session-Token", session_id);
        dbApi.setBasePath(AppConstants.DSP_URL);
    }

    /**
     * Queries the database and attempts to add a friend
     * @param friend_2_id display_name of friend to add
     * @return "SUCCESS" on success and "FAILEd" or Error message on failure
     */
    public String sendFriendRequest(int friend_2_id) {
        try {
            AddFriendModel request = new AddFriendModel(user_id,friend_2_id,user_id );
            FriendRecord record = dbApi.createRecord(FriendRecord.class, AppConstants.FRIENDS_TABLE_NAME, "123", request, null, null, null, null);
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

    //TODO how to stop people from confirming requests that they sent?
    //only the implementation stops this. That is friend_2_id must be the sender

    /**
     * Accepts a friend request by changing the status int to 0. Before changing, status indicates
     * which user sent the friend request. Don't forget to tadd the friend to the local database
     * @param friend_2_id Confirm request from freind with this friend_id
     * @return "SUCCESS" on success and "FAILEd" or Error message on failure
     */
    public String acceptFriendRequest(int friend_2_id) {
        try {
            AddFriendModel request = new AddFriendModel(user_id,friend_2_id, 0);
            FriendRecord record = dbApi.updateRecord(FriendRecord.class, AppConstants.FRIENDS_TABLE_NAME, "0", request, "updated", null, null, null);
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

    /**
     * Gets all the available friend requests
     * @return A list fof all friend requests
     */
    public FriendsRecord getFriendRequests() {
        try {
            // "(`friend_1` = " + user_id + " OR `friend_2` = " + user_id + "): This filter is already implicitly defined
            String filter =" `status` != " + user_id + " AND `status` != " + 0 ;
            //TODO does limiting fields actually improve speed and peformance?
            FriendsRecord temp = dbApi.getRecordsByFilter(FriendsRecord.class, AppConstants.FRIENDS_TABLE_NAME, filter, null, null, null, null, false, false, null);
            Log.d(TAG, temp.toString());
            return temp;
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            //Attempting to return only the message part of the error message
            try {
                JSONObject jObj = new JSONObject(e.getMessage());
                JSONArray jArray = jObj.getJSONArray("error");
                JSONObject obj = jArray.getJSONObject(0);
                return null;
            } catch (JSONException ex) { //message is from exception
                //TODO customize message if an exception was thrown while getting messeage?
                //Currently ignoring JSONException and returning full error message
                ex.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Updates the stats for a user after a game.
     * @param friend_2_id friend_id whose game stats should be updated
     * @param result true if current friend won, false otherwise
     * @return "SUCCESS" on success and "FAILEd" or Error message on failure
     * @deprecated TODO: deprecate. Implement this in the backend to prevent cheating
     */
    public String updateStats(int friend_2_id, boolean result) {
        try {
            StoredProcRequest request = new StoredProcRequest();
            request.addParam(new com.picspy.models.StoredProcParam("my_id", user_id));
            if (user_id < friend_2_id) {
                request.addParam(new com.picspy.models.StoredProcParam("friend_1_id", user_id));
                request.addParam(new com.picspy.models.StoredProcParam("friend_2_id", friend_2_id));
            } else {
                request.addParam(new com.picspy.models.StoredProcParam("friend_1_id", friend_2_id));
                request.addParam(new com.picspy.models.StoredProcParam("friend_2_id", user_id));
            }
            int temp_result = result? 1: 0;
            request.addParam(new com.picspy.models.StoredProcParam("result", temp_result));
            StoredProcResponse response = dbApi.callStoredProcWithParams(StoredProcResponse.class,"update_game_stats", request, null);

            Log.d(TAG, response.toString());
            if (response.getReturn_val().equals("1")) {
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

    public String removeFriend(int friend_2_id) {
        try {
            FriendRequest request1 = new FriendRequest(user_id,friend_2_id );
            FriendsRequest request = new FriendsRequest();
            request.addRecord(request1);
            FriendsRecord record = dbApi.deleteRecords(FriendsRecord.class, AppConstants.FRIENDS_TABLE_NAME, request, null, null, null, false, false, null, null,null);
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

    /**
     * Gets the stats for a given user. This includes their individual
     * game stats, and stats for games between both users
     * @param friend_2_id
     * @return
     */
    public FriendRecord getStats(int friend_2_id) {
        try {
            String filter;
            String related;
            if (user_id < friend_2_id) {
                related = "users_by_friend_2";
                filter = "`friend_1` = " + user_id + " AND `friend_2` = " + friend_2_id;
            } else {
                related = "users_by_friend_1";
                filter = "`friend_1` = " + friend_2_id + " AND `friend_2` = " + user_id;
            }
           //TODO does limiting fields actually improve speed and peformance?
           // String fields = "friend_1_won, friend_2_won, friend_1_lost, friend_2_lost";
            // fields must be null to enable related record
            String fields = null;
            FriendsRecord temp = dbApi.getRecordsByFilter(FriendsRecord.class,
                    AppConstants.FRIENDS_TABLE_NAME, filter, null, null,
                    null, fields, false, false, related);
            Log.d(TAG,temp.toString());
            //TODO handle null properly
            if (temp != null ) {
                //session not valid
                //TODO handle invalid session
                if (temp.getRecord().size() == 0) {
                    return null;
                } else return temp.getRecord().get(0);
            }

            return null;

        } catch (Exception e) {
            Log.d(TAG, e.toString());
            Log.d(TAG, e.getMessage());
            //Attempting to return only the message part of the error message
            try {
                JSONObject jObj = new JSONObject(e.getMessage());
                JSONArray jArray = jObj.getJSONArray("error");
                JSONObject obj = jArray.getJSONObject(0);
                return null;
            } catch (JSONException ex) { //message is from exception
                //TODO customize message if an exception was thrown while getting messeage?
                //Currently ignoring JSONException and returning full error message
                ex.printStackTrace();
                return null;
            }
        }
    }


    /**
     * Private class containing basic fields for all api calls. All classes to be used in
     * request must extend this class.
     * Alsop provides model for retrieving records
     */
    private class FriendRequest extends DbApiRequest {
        @JsonProperty("friend_1")
        private int friend_1;
        @JsonProperty("friend_2")
        private int friend_2;

        /**
         * Constructor reorders parameters so that friend_1 is always less than friend_2
         * parameters can be provided in any order
         * @param friend_1 friend_1/friend_2 id
         * @param friend_2 friend_2/friend_1 id
         */
        private FriendRequest (int friend_1,  int friend_2){
            if (friend_1 < friend_2) {
                this.friend_1 = friend_1;
                this.friend_2 = friend_2;
            } else {
                this.friend_1 = friend_2;
                this.friend_2 = friend_1;
            }
        }

        /**
         * Non-ordering method that serves as alternate constructor needed for sending
         * new friend request since friend_1 must be the id of the requestor
         * @param f1 The friend id of the current user
         * @param f2 The Id of the friend to be added
         */
        private void friendRequestAdd(int f1, int f2) {
            this.friend_1 = f1;
            this.friend_2 = f2;
        }

        @Override
        public String toString() {
            return "AddFriendModel{" +
                    "\n" + "  friend_1: " + friend_1 +
                    "\n" + "  friend_2: " + friend_2;
        }
    }

    /**
     * Model for retrieving multiple records from the friends database
     */
    public class FriendsRequest extends DbApiRequest {
        @JsonProperty("record")
        private List<FriendRequest> record = new ArrayList<>();

        public List<FriendRequest> getRecord() {
            return record;
        }

        public void setRecord(List<FriendRequest> record) {
            this.record = record;
        }

        public void addRecord(FriendRequest request) {
            this.record.add(request);
        }

        @Override
        public String toString() {
            return "FriendsRecord {" +
                    "\n" + "  record: " + record +
                    "\n}";
        }
    }

    /**
     * Model for adding new friends. Needs to be separate since
     * the status field must be provided.
     */
    private class AddFriendModel  extends FriendRequest{
        @JsonProperty("status")
        private int status;

        /**
         * Creates a new record in the table and that represents a friend request
         * @param friend_1 The current user's user_id
         * @param friend_2 The user_id of the desired friend
         * @param status  The current user's user_id
         */
        public AddFriendModel(int friend_1, int friend_2, int status) {
            super(friend_1, friend_2);
            super.friendRequestAdd(friend_1, friend_2);
            this.status = status;
        }

        @Override
        public String toString() {
            String str =  super.toString();
            return str.concat("\n" + "  status: " + status + "\n}");
        }
    }

}
