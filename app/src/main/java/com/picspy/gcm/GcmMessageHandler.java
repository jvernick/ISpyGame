package com.picspy.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.picspy.firstapp.R;
import com.picspy.utils.AppConstants;
import com.picspy.utils.PrefUtil;
import com.picspy.views.ChallengesActivity;
import com.picspy.views.FindFriendsActivity;
import com.picspy.views.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by BrunelAmC on 4/23/2016.
 */
public class GcmMessageHandler extends GcmListenerService {
    public static final int FRIEND_NOTIFICATION_ID = 6001;
    public static final int CHALLENGE_NOTIFICATION_ID = 6002;
    public static final int APP_NOTIFICATION_ID = 6003;

    public static final String TAG_SENDER = "sender";
    public static final String TAG_SENDER_ID = "senderId";
    public static final String TAG_MESSAGE = "message";
    public static final String TAG_TYPE = "type";
    public static final String TAG_TITLE = "Picspy";

    public static final String TYPE_FRIEND = "friendRequest";
    public static final String TYPE_CHALLENGE = "challengeRequest";

    private static final String TAG = "GcmMessHandler";
    public static final String GCM_NOTF = "gcnNotification";


    @Override
    public void onMessageReceived(String serverOrigin, Bundle data) {
        JSONObject message;
        String type = null;
        String sender = null;
        String notificationMessage = "";
        int senderId = 0, count = 0;

        try {
            message = new JSONObject(data.getString(TAG_MESSAGE));
            Log.d(TAG, message.toString());
            type = message.getString(TAG_TYPE);
            senderId = message.getInt(TAG_SENDER_ID);
            sender = message.getString(TAG_SENDER);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (type == null) type = "";

        switch (type) {
            case TYPE_FRIEND:
                notificationMessage = "Friend request from " + sender;
                count = 1 + PrefUtil.getInt(this, AppConstants.FRIEND_REQUEST_COUNT, 0);
                PrefUtil.putInt(this, AppConstants.FRIEND_REQUEST_COUNT, count);
                if (count > 1) notificationMessage = count + "  Friend Requests!";
                break;
            case TYPE_CHALLENGE:
                notificationMessage = "New challenge from " + sender;
                count = 1 + PrefUtil.getInt(this, AppConstants.CHALLENGE_REQUEST_COUNT, 0);
                PrefUtil.putInt(this, AppConstants.CHALLENGE_REQUEST_COUNT, count);
                if (count > 1) notificationMessage = count + "  New Challenges!";
                break;
            default:
                break;
        }

        createNotification(TAG_TITLE, notificationMessage, type);
    }



    /**
     * Creates notification based on title and body received
     * @param title Notification title. Usually the app name
     * @param message Notification message
     * @param type Notification type used to determine Icon
     */
    private void createNotification(String title, String message, String type) {
        int icon, notificationId;
        Intent resultIntent;
        switch (type) {
            case TYPE_FRIEND:
                icon = R.drawable.ic_add_friend;
                resultIntent = new Intent(this, FindFriendsActivity.class);
                notificationId = FRIEND_NOTIFICATION_ID;
                break;
            case TYPE_CHALLENGE:
                icon = R.drawable.ic_challenge_lime; // TODO change icon to white
                resultIntent = new Intent(this, ChallengesActivity.class);
                notificationId = CHALLENGE_NOTIFICATION_ID;
                break;
            default:
                resultIntent = new Intent(this, MainActivity.class);
                icon = R.drawable.ic_launcher;
                notificationId = APP_NOTIFICATION_ID;
                break;
        }

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        resultIntent.putExtra(GCM_NOTF, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, resultIntent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);
        NotificationManager mNotificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationId, mBuilder.build());
    }

}