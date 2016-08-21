package com.picspy.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.picspy.firstapp.R;
import com.picspy.models.GameRecord;
import com.picspy.models.UserChallengeRecord;
import com.picspy.utils.AppConstants;
import com.picspy.utils.ChallengesRequests;
import com.picspy.utils.ChallengesRequests.GAME_LABEL;
import com.picspy.utils.FileRequest;
import com.picspy.utils.PrefUtil;
import com.picspy.utils.VolleyRequest;
import com.picspy.views.fragments.ChooseFriendsFragment;
import com.picspy.views.fragments.ConfigureChallengeFragment;

import java.io.File;
import java.util.ArrayList;

public class SendChallenge extends ActionBarActivity implements
        ConfigureChallengeFragment.F1FragmentInteractionListener,
        ChooseFriendsFragment.F2FragmentInteractionListener {

    public static final String BDL_GAME_OPTIONS = "bdl_game_options";
    public static final String BDL_PICTURE_OPTIONS = "bdl_picture_options";
    public static final String ARG_FRIEND_ID = "friend_id";
    private static final String TAG = "SendChallenge";
    private static final String CANCEL_TAG = "cancel_sendChallenge";
    private int friend_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_challenge);

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            ConfigureChallengeFragment configureFragment = ConfigureChallengeFragment.newInstance(
                    getIntent().getBundleExtra(SendChallenge.BDL_PICTURE_OPTIONS),
                    getIntent().getIntExtra(SendChallenge.ARG_FRIEND_ID, -1)
            );

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

            // Setting toolbar as the ActionBar
            //TODO back button stopped working. temp solution in onOptionsItemSelected
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationIcon(R.drawable.ic_chevron_left_white);

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, configureFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_send_challenge, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setToolbarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void startGame(Bundle gameBundle) {
        sendPicture(gameBundle);
    }


    /**
     * Uploads the image to the server and if successful, send the game info to the server
     *
     * @param bundle A bundle containing the game information
     */
    private void sendPicture(final Bundle bundle) {
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    Log.d(TAG, "onResponse: " + response);
                    sendChallengeInfo(bundle);
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    String err = (error.getMessage() == null) ? "An error occurred" : error.getMessage();
                    error.printStackTrace();
                    Log.d(TAG, err);
                    //Show toast only if there is no server connection on refresh
                    if (err.matches(AppConstants.CONNECTION_ERROR) || err.matches(AppConstants.TIMEOUT_ERROR)) {
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.view_network_error_toast,
                                (ViewGroup) findViewById(R.id.toast_layout_root));
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    }
                }
            }
        };

        String filename = bundle.getString(GAME_LABEL.FILE_NAME);
        String filepath = bundle.getString(GAME_LABEL.FILE_NAME_PATH);
        Log.d(TAG, "filename: " + filename);
        Log.d(TAG, "filepath: " + filepath);

        FileRequest createFileRequest = FileRequest.sendPicture(this,
                filename, filepath, responseListener, errorListener);
        if (createFileRequest != null) createFileRequest.setTag(CANCEL_TAG);
        VolleyRequest.getInstance(getApplicationContext()).addToRequestQueue(createFileRequest);
    }

    /**
     * Sends the challenge information to the server.
     *
     * @param bundle Bundle containing game parameters
     */
    private void sendChallengeInfo(final Bundle bundle) {
        Response.Listener<GameRecord> responseListener = new Response.Listener<GameRecord>() {
            @Override
            public void onResponse(GameRecord response) {
                if (response != null && response.getId() != 0) {
                    Toast.makeText(getApplicationContext(), "Games successfully sent", Toast.LENGTH_SHORT).show();
                    File file = new File(bundle.getString(GAME_LABEL.FILE_NAME_PATH));
                    if (file.delete()) Log.d(TAG, "picture deleted");

                    Intent intent = new Intent(SendChallenge.this, MainActivity.class);
                    //TODO use? intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error != null) {
                    String err = (error.getMessage() == null) ? "An error occurred" : error.getMessage();
                    error.printStackTrace();
                    Log.d(TAG, err);
                    //Show toast only if there is no server connection on refresh
                    if (err.matches(AppConstants.CONNECTION_ERROR) || err.matches(AppConstants.TIMEOUT_ERROR)) {
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate(R.layout.view_network_error_toast,
                                (ViewGroup) findViewById(R.id.toast_layout_root));
                        Toast toast = new Toast(getApplicationContext());
                        toast.setGravity(Gravity.BOTTOM | Gravity.FILL_HORIZONTAL, 0, 0);
                        toast.setDuration(Toast.LENGTH_LONG);
                        toast.setView(layout);
                        toast.show();
                    }
                }
            }
        };

        GameRecord gameRecord = new GameRecord();
        gameRecord.setGuess(bundle.getInt(GAME_LABEL.GUESSES));
        gameRecord.setTime(bundle.getInt(GAME_LABEL.TIME));
        gameRecord.setHint(bundle.getString(GAME_LABEL.HINT));
        gameRecord.setSelection(bundle.getString(GAME_LABEL.SELECTION));
        gameRecord.setLeaderboard(bundle.getBoolean(GAME_LABEL.LEADERBOARD));
        gameRecord.setPicture_name(bundle.getString(GAME_LABEL.FILE_NAME));
        int[] friends = bundle.getIntArray(GAME_LABEL.FRIENDS);
        ArrayList<UserChallengeRecord> challengeUsers = new ArrayList<>();

        if (friends != null) {
            for (int friend : friends) {
                challengeUsers.add(new UserChallengeRecord(friend));
            }
        }
        gameRecord.setUser_challenges_by_challenge_id(challengeUsers);
        gameRecord.setSender(PrefUtil.getInt(this, AppConstants.USER_ID));

        ChallengesRequests challengeInfoRequest = ChallengesRequests.createGame(this, gameRecord,
                responseListener, errorListener);
        if (challengeInfoRequest != null) challengeInfoRequest.setTag(CANCEL_TAG);
        VolleyRequest.getInstance(getApplicationContext()).addToRequestQueue(challengeInfoRequest);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //cancel all pending volley requests
        if (VolleyRequest.getInstance(this.getApplicationContext()) != null) {
            VolleyRequest.getInstance(this.getApplication()).getRequestQueue().cancelAll(CANCEL_TAG);
        }
    }
}
