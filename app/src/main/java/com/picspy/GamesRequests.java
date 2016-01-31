package com.picspy;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.dreamfactory.api.DbApi;
import com.dreamfactory.api.FilesApi;
import com.dreamfactory.client.ApiException;
import com.dreamfactory.model.FileRequest;
import com.dreamfactory.model.FileResponse;
import com.picspy.models.GamesRecord;
import com.picspy.models.UserChallengesRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.ChallengesRequests;
import com.picspy.utils.PrefUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by BrunelAmC on 8/20/2015.
 * Class contains methods for handling game operations.  All methods within this
 * class must be called from an AsyncTask, since they do network access
 */
public class GamesRequests {
    private static final Integer LEADERBOARD_LIMIT = 10;
    private String session_id;
    private DbApi dbApi;
    private FilesApi fileApi;
    private int user_id;
    private final static String TAG = "GameRequests";
    private Context context;

    /**
     * Constructor initializes the required settings and values from shared preferences
     * @param context The contexts that calls the method. This is needed to access the shared
     *  preferences
     * @param  fileUpload Determmines whether or not files are to be uplaoded
     */
    public GamesRequests(Context context, Boolean fileUpload) {
        if (!fileUpload) {
            session_id = PrefUtil.getString(context, AppConstants.SESSION_ID);
            user_id = PrefUtil.getInt(context, AppConstants.USER_ID);
            dbApi = new DbApi();
            dbApi.addHeader("X-DreamFactory-Application-Name", AppConstants.APP_NAME);
            dbApi.addHeader("X-DreamFactory-Session-Token", session_id);
            dbApi.setBasePath(AppConstants.DSP_URL);
        } else {
            session_id = PrefUtil.getString(context, AppConstants.SESSION_ID);
            fileApi = new FilesApi();
            fileApi.addHeader("X-DreamFactory-Application-Name", AppConstants.APP_NAME);
            fileApi.addHeader("X-DreamFactory-Session-Token", session_id);
            fileApi.setBasePath(AppConstants.DSP_URL);
        }
        this.context = context;
    }

    /**
     * Creates new game on backend
     * @param imageFileName  file name
     * @param imageAbsolutePath absolute file path
     * @param params Challenge parameters including hint, time etc
     * @return "Success" on successful upload. Returns error message when
     * exception is thrown, otherwise returns "Failed" on error
     * TODO veryfiy return type
     */
    public String createGame(String imageFileName, String imageAbsolutePath, ChallengeParams params) {
        // where to upload on server
        String containerName = AppConstants.CONTAINER_NAME;
        String filePathOnServer = AppConstants.FOLDER_NAME + "/";

        FileRequest request = new FileRequest();
        request.setName(imageFileName);  // this will be stored file name on server
        request.setPath(imageAbsolutePath);
        try {
            FileResponse resp = fileApi.createFile(containerName, filePathOnServer, false,
                    request, params.getParams());
            Log.d(TAG, resp.toString());
            return (resp == null)? "Failed" : "Success";
        } catch (ApiException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
            return e.getMessage();
        }
    }

    /**
     * Gets all the available games. Make sure to add games to the database after getting them.
     * @return A list fof all pending games
     * @throws ApiException on error
     * TODO consider rethrowing exception so that caller handles it
     */
    public UserChallengesRecord getGamesInfo() throws ApiException{
        //Get only records that changed between now and the most recent call
        //TODO this idea only works if deleting the shared preferences is always simultaenos with deleting the database. Confirm
        String filter = " `user_id` = " + user_id  + " AND `id` > " + PrefUtil.getInt(context,
                AppConstants.MAX_USER_CHALLENGE_ID, 0);

        UserChallengesRecord result = dbApi.getRecordsByFilter(UserChallengesRecord.class,
                AppConstants.USER_CHALLENGES_TABLE_NAME, filter, null, null, null, null,
                false, false, "*");
        if (result != null) Log.d(TAG, result.toString());
        return result;
    }

    /**
     * Gets leaderboard games from server.
     * TODO modify method to get WEEKLY/DAILY leaderboard instead of overall Leaderboard
     * @return A record containing leaderboard games
     * @throws ApiException on error
     */
    public GamesRecord getLeaderboardGames() throws ApiException {
        //only challenges users want public
        //TODO added for testing
        String filter = " `leaderboard` = true";
        //String filter = " `leaderboard` = true";

        //related record to get username
        String related = "users_by_sender";
        //order by votes in descending, descending
        String order = "votes desc";
        return dbApi.getRecordsByFilter(GamesRecord.class,
                AppConstants.CHALLENGES_TABLE_NAME, filter, LEADERBOARD_LIMIT, null, order, null,
                false, false, related);
    }
    
    /**
     * This class is used to appropriately model and store information
     * about a challenge (metadata) when creating a game. It provides a method that returns
     * the appropriate data type
     */
    public static class ChallengeParams {
        private HashMap<String,String> params;

        /**
         * Constructor initializes all the challenge parameters. Hence no getter and setters.
         * The values for time, guess are set to the default values of 3 and 5 if out of bounds
         * @param selection A string that represents the correct area of the picture (solution)
         * @param hint A hint for solving the challenge
         * @param guess The number of guesses allowed (1 - 5)
         * @param time The time limit (5 - 30 secs)
         * @param leaderboard True if challenge is elected for leaderboard, otherwise false
         * @param friends list of recipients as friend_ids
         * @deprecated  DO NOT USE
         */
        public ChallengeParams(String selection, String hint, int guess, int time,
                               boolean leaderboard, int[] friends) {
            params = new HashMap<>();
            params.put(ChallengesRequests.GAME_LABEL.SELECTION, selection);
            params.put(ChallengesRequests.GAME_LABEL.HINT, hint);
            guess = (guess < 1 || guess > 5)? 3: guess;
            params.put(ChallengesRequests.GAME_LABEL.GUESSES, String.valueOf(guess));
            time = (time < 5 || time > 30) ? 5 : time;
            params.put(ChallengesRequests.GAME_LABEL.TIME, String.valueOf(time));
            params.put(ChallengesRequests.GAME_LABEL.LEADERBOARD, String.valueOf(leaderboard));
            //TODO since this has brackets around it, javascript may already interpret it as an array. Verify and test
            params.put(ChallengesRequests.GAME_LABEL.FRIENDS, Arrays.toString(friends));
        }

        public ChallengeParams(Bundle bundle) {
            params = new HashMap<>();
            params.put(ChallengesRequests.GAME_LABEL.SELECTION, "sel too long");//bundle.getString(GAME_LABEL.SELECTION));
            params.put(ChallengesRequests.GAME_LABEL.HINT, bundle.getString(ChallengesRequests.GAME_LABEL.HINT));
            params.put(ChallengesRequests.GAME_LABEL.GUESSES, String.valueOf(bundle.getInt(ChallengesRequests.GAME_LABEL.GUESSES)));
            params.put(ChallengesRequests.GAME_LABEL.TIME, String.valueOf(bundle.getInt(ChallengesRequests.GAME_LABEL.TIME)));
            params.put(ChallengesRequests.GAME_LABEL.LEADERBOARD,
                    String.valueOf(bundle.getBoolean(ChallengesRequests.GAME_LABEL.LEADERBOARD)));
            String s = Arrays.toString(bundle.getIntArray(ChallengesRequests.GAME_LABEL.FRIENDS));
            s = s.replace('[', ' ');
            s = s.replace(']', ' ');
            s = s.replaceAll("\\s+","");
            params.put(ChallengesRequests.GAME_LABEL.FRIENDS, s);
        }

        /**
         * @return returns the parameters as a map
         */
        public HashMap<String, String> getParams() {
            return params;
        }

        @Override
        public String toString() {
            String s = "{ \n";
            for (Map.Entry<String,String> entry: params.entrySet()) {
                s += "\t" + entry.getKey() + ": " + entry.getValue() + "\n";
            }

            return s + "}";
        }
    }

}
