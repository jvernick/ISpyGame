package com.picspy;

import android.content.Context;
import android.util.Log;

import com.dreamfactory.api.DbApi;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.picspy.models.DbApiRequest;
import com.picspy.models.DbApiResponse;
import com.picspy.models.FriendRecord;
import com.picspy.models.FriendsRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by BrunelAmC on 7/15/2015.
 */
public class FriendsTableRequests {
    private int user_id;
    private String session_id;
    private DbApi dbApi;
    public FriendsTableRequests(Context context) {
        user_id = Integer.parseInt(PrefUtil.getString(context, AppConstants.USER_ID));
        session_id = PrefUtil.getString(context, AppConstants.SESSION_ID);
        dbApi = new DbApi();
        dbApi.addHeader("X-DreamFactory-Application-Name", AppConstants.APP_NAME);
        dbApi.addHeader("X-DreamFactory-Session-Token", session_id);
        dbApi.setBasePath(AppConstants.DSP_URL);
    }

    public String addFriend(String friend_2_id) {
        try {
            AddFriendModel request = new AddFriendModel(user_id, friend_2_id,user_id );
            FriendRecord record = dbApi.createRecord(FriendRecord.class, AppConstants.FRIENDS_TABLE_NAME, "123", request, null, null, null,null);
            //Log.d("s", record.toString());
            return "Success";
        } catch (Exception e) {
            Log.d("FriendsTableRequest", e.getMessage());
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

    private class AddFriendModel  extends DbApiRequest{
        @JsonProperty("friend_1")
        private int friend_1;
        @JsonProperty("friend_2")
        private String friend_2;
        @JsonProperty("status")
        private int status;

        public AddFriendModel(int friend_1, String friend_2, int status) {
            this.friend_1 = friend_1;
            this.friend_2 = friend_2;
            this.status = status;
        }

        @Override
        public String toString() {
            return "AddFriendModel{" +
                    "friend_1=" + friend_1 +
                    ", friend_2='" + friend_2 + '\'' +
                    ", status=" + status +
                    '}';
        }
    }
}
